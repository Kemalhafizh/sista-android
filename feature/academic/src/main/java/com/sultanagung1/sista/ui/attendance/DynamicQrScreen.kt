package com.sultanagung1.sista.ui.attendance

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness
import kotlinx.coroutines.delay

sealed interface QrDisplayState {
    data object Loading : QrDisplayState
    /** [qrToken] is drawn as a QR; the gate scanner sends it to `mobile/attendance/verify-qr`. */
    data class Content(val qrToken: String, val freshness: QrFreshness) : QrDisplayState
    data class Error(val message: String) : QrDisplayState
}

/**
 * What to draw, from the view model state and a monotonic clock. A QR that
 * failed to refresh stays up, dimmed, until a minute after it expired
 * ([ClassSessionRules.qrFreshness]); after that it is no longer shown.
 */
fun dynamicQrDisplayState(state: AttendanceUiState, nowMs: Long): QrDisplayState {
    val token = state.dynamicQrResult?.qrToken
    val freshness = ClassSessionRules.qrFreshness(state.dynamicQrExpiresAtMs, nowMs)
    return when {
        token != null && freshness != QrFreshness.UNAVAILABLE && freshness != QrFreshness.LOADING ->
            QrDisplayState.Content(token, freshness)
        state.errorMessage != null && !state.isLoading -> QrDisplayState.Error(state.errorMessage)
        token != null && freshness == QrFreshness.UNAVAILABLE ->
            QrDisplayState.Error("QR kedaluwarsa dan belum bisa diperbarui. Periksa koneksi internet.")
        else -> QrDisplayState.Loading
    }
}

/**
 * FASE 77 follow-up: the student's rotating gate QR. It used to draw a QR
 * *icon* with the first 16 characters of the token as text — nothing a scanner
 * could read — and the model expected `qr_payload`, which the server never sends.
 */
@Composable
fun DynamicQrScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var nowMs by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = SystemClock.elapsedRealtime()
            delay(500)
        }
    }

    // Refresh only while visible; the server's expiry drives the timing.
    LifecycleStartStopEffect(
        onStart = viewModel::startDynamicQrRotation,
        onStop = viewModel::stopDynamicQrRotation
    )

    val displayState = dynamicQrDisplayState(uiState, nowMs)
    val showingQr = displayState is QrDisplayState.Content
    // Bright and awake for the gate scanner; no screenshots to forward to a friend.
    KeepScreenAwake(enabled = showingQr, maxBrightness = true)
    SecureWindowEffect(enabled = true)

    val secondsLeft = uiState.dynamicQrExpiresAtMs
        ?.let { (((it - nowMs) + 999) / 1000).toInt().coerceAtLeast(0) } ?: 0
    val rotation = GateQrDefaults.ROTATION_SECONDS

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "QR Presensi Dinamis",
                subtitle = "Tunjukkan ke scanner gerbang / guru",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SulaoneCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Kode Presensi Siswa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Berganti setiap $rotation detik, jadi tangkapan layar tidak bisa dipakai orang lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val display = displayState) {
                            is QrDisplayState.Loading -> ShimmerSkeleton(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            is QrDisplayState.Content -> SulaoneQrCode(
                                payload = display.qrToken,
                                contentDescription = "Kode QR presensi. Tunjukkan ke scanner gerbang atau guru.",
                                dimmed = display.freshness == QrFreshness.STALE,
                                modifier = Modifier.fillMaxSize()
                            )
                            is QrDisplayState.Error -> SulaoneErrorBanner(
                                message = display.message,
                                onRetry = viewModel::loadDynamicQr
                            )
                        }
                    }

                    when {
                        displayState is QrDisplayState.Content && displayState.freshness == QrFreshness.STALE -> Text(
                            text = "Mungkin kedaluwarsa — sedang memperbarui…",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentAmber
                        )
                        displayState is QrDisplayState.Content -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { secondsLeft / rotation.toFloat() },
                                modifier = Modifier.size(28.dp),
                                color = if (secondsLeft > 5) Emerald700 else AccentRose,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Berganti dalam $secondsLeft detik",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (secondsLeft > 5) MaterialTheme.colorScheme.onSurface else AccentRose
                            )
                        }
                        else -> Unit
                    }
                }
            }

            SulaoneButton(
                text = "Perbarui QR Sekarang",
                onClick = viewModel::loadDynamicQr,
                icon = Icons.Default.Refresh,
                containerColor = Emerald700
            )
        }
    }
}

/** The server rotates the gate QR every 30 s (GeofenceAttendanceService, `time() / 30`). */
private object GateQrDefaults {
    const val ROTATION_SECONDS = 30
}
