package com.sultanagung1.sista.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.sultanagung1.sista.core.designsystem.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentMode by viewModel.currentMode.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var showManualEntry by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    LaunchedEffect(isTorchOn, boundCamera) {
        boundCamera?.cameraControl?.enableTorch(isTorchOn)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val barcodeScanner = BarcodeScanning.getClient()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            // viewModel.isScanning.value (not the recomposed
                            // `isScanning` local) is read here — this analyzer
                            // lambda is created once by AndroidView's factory,
                            // so it must always read live state directly from
                            // the ViewModel rather than a captured snapshot.
                            if (mediaImage != null && viewModel.isScanning.value) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        val rawValue = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                                        if (rawValue != null) {
                                            viewModel.onCodeScanned(rawValue)
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            boundCamera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (_: Exception) {
                            // Real bind failure (e.g. no back camera on this
                            // device) — the manual-entry fallback below
                            // remains available either way.
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Izin kamera diperlukan untuk memindai kode QR/barcode.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Berikan Izin Kamera")
                }
            }
        }

        // Top Bar & Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Pemindai Kamera Multi-Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { viewModel.toggleTorch() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isTorchOn) Gold600 else Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Senter",
                        tint = if (isTorchOn) Slate950 else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ScrollableTabRow(
                selectedTabIndex = ScanMode.values().indexOf(currentMode),
                containerColor = Color.Transparent,
                contentColor = Emerald400,
                edgePadding = 0.dp,
                divider = {}
            ) {
                ScanMode.values().forEach { mode ->
                    val isSelected = mode == currentMode
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Emerald700 else Color.White.copy(alpha = 0.12f))
                            .clickable { viewModel.switchMode(mode) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Slate300
                        )
                    }
                }
            }
        }

        // Center Viewfinder Target Area
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cornerLength = 40.dp.toPx()
                val strokeW = 4.dp.toPx()
                val cornerColor = Emerald400

                drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeW)
                drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLength), strokeW)
                drawLine(cornerColor, Offset(w, 0f), Offset(w - cornerLength, 0f), strokeW)
                drawLine(cornerColor, Offset(w, 0f), Offset(w, cornerLength), strokeW)
                drawLine(cornerColor, Offset(0f, h), Offset(cornerLength, h), strokeW)
                drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cornerLength), strokeW)
                drawLine(cornerColor, Offset(w, h), Offset(w - cornerLength, h), strokeW)
                drawLine(cornerColor, Offset(w, h), Offset(w, h - cornerLength), strokeW)

                if (isScanning && !isProcessing) {
                    val laserY = h * laserProgress
                    drawLine(
                        color = Gold400,
                        start = Offset(10.dp.toPx(), laserY),
                        end = Offset(w - 10.dp.toPx(), laserY),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }

        // Bottom Instruction & Manual Entry Fallback
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    isProcessing -> "Memverifikasi kode ke server..."
                    else -> currentMode.description
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Slate200,
                textAlign = TextAlign.Center
            )

            if (isProcessing) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = Gold400, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showManualEntry) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { manualCode = it },
                        placeholder = { Text("Masukkan kode manual", color = Slate400) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate400
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualCode.isNotBlank()) {
                                viewModel.onCodeScanned(manualCode.trim())
                                manualCode = ""
                                showManualEntry = false
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim")
                    }
                }
            } else {
                TextButton(onClick = { showManualEntry = true }) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, tint = Slate300, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tidak bisa memindai? Ketik kode manual", color = Slate300, fontSize = 12.sp)
                }
            }
        }

        // Scan Result Dialog
        scanResult?.let { result ->
            AlertDialog(
                onDismissRequest = { viewModel.resetScan() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (result.isValid) Emerald700 else AccentRose,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(result.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = result.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (result.details.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            result.details.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(key, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (result.isValid) Emerald700 else AccentRose)
                    ) {
                        Text(if (result.isValid) "Selesai" else "Coba Lagi")
                    }
                }
            )
        }
    }
}
