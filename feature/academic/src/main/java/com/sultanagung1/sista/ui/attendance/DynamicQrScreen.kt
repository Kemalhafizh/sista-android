package com.sultanagung1.sista.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import kotlinx.coroutines.delay

sealed interface QrDisplayState {
    data object Loading : QrDisplayState
    data class Content(val qrPayload: String) : QrDisplayState
    data class Error(val message: String) : QrDisplayState
}

@Composable
fun DynamicQrScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    var countdown by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        viewModel.loadDynamicQr()
        while (true) {
            delay(1000)
            if (countdown > 1) {
                countdown--
            } else {
                countdown = 30
                viewModel.loadDynamicQr()
            }
        }
    }

    val displayState = remember(uiState.dynamicQrResult, uiState.isLoading, uiState.errorMessage) {
        val result = uiState.dynamicQrResult
        val error = uiState.errorMessage
        
        if (uiState.isLoading && result == null) {
            QrDisplayState.Loading
        } else if (result == null && error != null) {
            QrDisplayState.Error(error)
        } else if (result != null) {
            QrDisplayState.Content(result.qrPayload)
        } else {
            QrDisplayState.Loading
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "QR Presensi Dinamis (TOTP)",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SulaoneCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kode Presensi Siswa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Berputar otomatis untuk mencegah tangkapan layar joki",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // QR Holographic Placeholder Stage
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate900),
                        contentAlignment = Alignment.Center
                    ) {
                        when (displayState) {
                            is QrDisplayState.Loading -> {
                                ShimmerSkeleton(
                                    modifier = Modifier.size(160.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            is QrDisplayState.Content -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QR Code",
                                        tint = Gold400,
                                        modifier = Modifier.size(160.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = displayState.qrPayload.take(16),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = Emerald300
                                    )
                                }
                            }
                            is QrDisplayState.Error -> {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    SulaoneErrorBanner(
                                        message = displayState.message,
                                        onRetry = {
                                            countdown = 30
                                            viewModel.loadDynamicQr()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Rotating Countdown Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { countdown / 30f },
                            modifier = Modifier.size(28.dp),
                            color = if (countdown > 5) Emerald700 else AccentRose,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Memperbarui dalam $countdown detik",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (countdown > 5) MaterialTheme.colorScheme.onSurface else AccentRose
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SulaoneButton(
                text = "Perbarui QR Sekarang",
                onClick = {
                    countdown = 30
                    viewModel.loadDynamicQr()
                },
                icon = Icons.Default.Refresh,
                containerColor = Emerald700
            )
        }
    }
}
