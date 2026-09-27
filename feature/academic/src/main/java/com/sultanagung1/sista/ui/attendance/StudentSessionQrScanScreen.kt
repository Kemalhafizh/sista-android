package com.sultanagung1.sista.ui.attendance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.sultanagung1.sista.core.designsystem.AccentAmber
import com.sultanagung1.sista.core.designsystem.AccentGreen
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.designsystem.ClassSessionUnavailableState
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaoneButton
import com.sultanagung1.sista.core.designsystem.SulaoneButtonVariant
import com.sultanagung1.sista.core.designsystem.SulaoneCard
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.data.model.ClassSessionRules
import java.util.concurrent.Executors

/**
 * FASE 77.5: "Presensi Kelas" — scan the QR on the teacher's screen.
 */
@Composable
fun StudentSessionQrScanScreen(
    viewModel: StudentSessionQrScanViewModel,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(StudentSessionQrScanEvent.ScreenStarted) },
        onStop = {}
    )
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                StudentSessionQrScanEffect.QrDetected -> haptics.tapLight()
                StudentSessionQrScanEffect.Recorded -> haptics.success()
                StudentSessionQrScanEffect.Refused -> haptics.errorWarning()
            }
        }
    }

    val active = state.activeSession
    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Presensi Kelas",
                subtitle = active?.let { listOfNotNull(it.subjectName, it.classroomName).joinToString(" • ") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.notDeployed) {
                ClassSessionUnavailableState(message = ClassSessionRules.NOT_DEPLOYED_MESSAGE, onRetry = null)
                return@Column
            }
            when (val phase = state.phase) {
                is StudentScanPhase.Success -> ResultPanel(
                    icon = Icons.Default.CheckCircle,
                    tint = AccentGreen,
                    title = "Presensi Berhasil!",
                    lines = listOfNotNull(
                        phase.subjectName,
                        phase.classroomName?.let { "Kelas $it" },
                        ClassSessionRules.clockOf(phase.checkedInAt)?.let { "Tercatat pukul $it WIB" }
                    ),
                    primary = "Kembali ke Beranda" to onNavigateHome
                )
                is StudentScanPhase.AlreadyRecorded -> ResultPanel(
                    icon = Icons.Default.CheckCircle,
                    tint = AccentGreen,
                    title = "Sudah Tercatat",
                    lines = listOf(phase.message),
                    primary = "Kembali ke Beranda" to onNavigateHome
                )
                is StudentScanPhase.Blocked -> ResultPanel(
                    icon = Icons.Default.Block,
                    tint = AccentRose,
                    title = "Presensi Tidak Tercatat",
                    lines = listOf(phase.message),
                    primary = "Pindai Lagi" to { viewModel.onEvent(StudentSessionQrScanEvent.ScanAgain) },
                    secondary = "Kembali" to onNavigateBack
                )
                StudentScanPhase.Scanning, StudentScanPhase.Submitting -> ScannerPanel(
                    state = state,
                    isScanning = { viewModel.uiState.value.isScanning },
                    onDetected = { viewModel.onEvent(StudentSessionQrScanEvent.CodeDetected(it)) }
                )
            }
        }
    }
}

@Composable
private fun ScannerPanel(
    state: StudentSessionQrScanState,
    isScanning: () -> Boolean,
    onDetected: (String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    LaunchedEffect(Unit) { if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    if (!state.isCheckingActive && state.activeSession == null) {
        InfoCard(
            icon = Icons.Default.Info,
            tint = AccentAmber,
            text = "Belum ada sesi kelas aktif untuk kelas Anda. Jika guru sudah menampilkan QR, Anda tetap bisa memindainya."
        )
    }

    Box(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (hasPermission) {
            SessionQrCamera(
                isScanning = isScanning,
                onDetected = onDetected,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            ScanAreaOverlay(Modifier.matchParentSize())
        } else {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                Text("Izin kamera diperlukan untuk memindai QR sesi kelas.", color = Color.White, textAlign = TextAlign.Center)
                SulaoneButton(text = "Izinkan Kamera", onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }
        }
        if (state.phase == StudentScanPhase.Submitting) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(color = Color.White)
                Text("Mencatat presensi…", color = Color.White)
            }
        }
    }

    Text(
        "Arahkan kamera ke QR yang ditampilkan di layar guru.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    state.hint?.let { hint ->
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = AccentAmber,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
    InfoCard(
        icon = Icons.Default.Warning,
        tint = AccentAmber,
        text = "QR berganti setiap 30 detik. Pindai langsung dari layar guru, bukan dari screenshot."
    )
    InfoCard(
        icon = Icons.Default.Info,
        tint = MaterialTheme.colorScheme.primary,
        text = "Tidak bisa memindai? Minta guru mengabsen Anda secara manual dari perangkat guru."
    )
}

// ImageProxy.image is CameraX's @ExperimentalGetImage API; ML Kit needs the media Image.
@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
private fun SessionQrCamera(isScanning: () -> Boolean, onDetected: (String) -> Unit, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentIsScanning by rememberUpdatedState(isScanning)
    val currentOnDetected by rememberUpdatedState(onDetected)
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
    }
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            scanner.close()
        }
    }

    AndroidView(
        modifier = modifier.semantics { contentDescription = "Kamera pemindai QR sesi kelas" },
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    // Read live state: this analyzer is created once, in the factory.
                    if (mediaImage != null && currentIsScanning()) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.let { raw ->
                                    ContextCompat.getMainExecutor(ctx).execute { currentOnDetected(raw) }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                } catch (_: Exception) {
                    // No usable back camera; the manual-attendance hint below still applies.
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@Composable
private fun ScanAreaOverlay(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val side = size.minDimension * 0.62f
        val topLeft = Offset((size.width - side) / 2, (size.height - side) / 2)
        drawRoundRect(
            color = Color.White,
            topLeft = topLeft,
            size = Size(side, side),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 18f)))
        )
    }
}

@Composable
private fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, text: String) {
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint)
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResultPanel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    lines: List<String>,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(88.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        lines.forEach { Text(it, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center) }
    }
    SulaoneButton(text = primary.first, onClick = primary.second, modifier = Modifier.fillMaxWidth())
    secondary?.let { SulaoneButton(text = it.first, onClick = it.second, variant = SulaoneButtonVariant.SecondaryOutlined, modifier = Modifier.fillMaxWidth()) }
}
