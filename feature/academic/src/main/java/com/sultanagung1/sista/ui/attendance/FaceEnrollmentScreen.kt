package com.sultanagung1.sista.ui.attendance

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.security.BiometricAvailability
import com.sultanagung1.sista.core.security.BiometricVault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceEnrollmentScreen(
    viewModel: FaceEnrollmentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = rememberHapticFeedbackHelper()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.checkDeviceBiometric(context)
    }

    // Radar pulse animation for the fingerprint emblem
    val infiniteTransition = rememberInfiniteTransition(label = "fingerprint_glow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_radius"
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Keamanan Sidik Jari",
                subtitle = "Proteksi Akun & Login Cepat (m-Banking)",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Slate950)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Fingerprint Emblem Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Fingerprint Icon
                    Box(
                        modifier = Modifier
                            .size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Emerald500.copy(alpha = 0.15f * glowRadius),
                                radius = size.minDimension / 2f * glowRadius
                            )
                            drawCircle(
                                color = Emerald600.copy(alpha = 0.25f),
                                radius = size.minDimension / 2.3f
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Emerald800.copy(alpha = 0.6f))
                                .border(1.5.dp, Emerald400, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = Emerald300,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Autentikasi Sidik Jari",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gunakan sensor sidik jari bawaan smartphone untuk mengunci aplikasi dan login instan layaknya aplikasi m-Banking modern.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Hardware Status Card
            val deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Emerald900.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sensor Hardware HP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Perangkat: $deviceName",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Badge
                    val (badgeBg: Color, badgeText: String, badgeColor: Color) = when (uiState.biometricAvailability) {
                        BiometricAvailability.AVAILABLE -> Triple(Emerald900.copy(alpha = 0.5f), "🟢 Sensor Sidik Jari Siap & Terdaftar (Hardware Ready)", Emerald400)
                        BiometricAvailability.NOT_ENROLLED -> Triple(Gold900.copy(alpha = 0.5f), "🟡 Sensor Ada, Daftarkan Sidik Jari di Pengaturan HP", Gold400)
                        BiometricAvailability.NO_HARDWARE -> Triple(AccentRose.copy(alpha = 0.2f), "🔴 Hardware Sensor Tidak Terdeteksi", AccentRose)
                        BiometricAvailability.SECURITY_UPDATE_REQUIRED -> Triple(Gold900.copy(alpha = 0.5f), "🟡 Pembaruan Keamanan Diperlukan", Gold400)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Security Controls (m-Banking Settings)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Toggle 1: App Lock & Fast Login
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kunci Aplikasi & Login Sidik Jari",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Masuk ke SISTA langsung dengan menempelkan jari tanpa ketik password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = uiState.isBiometricLockEnabled,
                            onCheckedChange = { viewModel.toggleBiometricLock(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Emerald400,
                                checkedTrackColor = Emerald900,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = Slate800
                            )
                        )
                    }

                    HorizontalDivider(
                        color = Slate800,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Toggle 2: Sensitive Action Protection (Grades, CBT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Proteksi Rapor & Ruang Ujian",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Minta verifikasi sidik jari saat membuka berkas rapor atau memasuki ujian CBT.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = uiState.isSensitiveProtectionEnabled,
                            onCheckedChange = { viewModel.toggleSensitiveProtection(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Emerald400,
                                checkedTrackColor = Emerald900,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = Slate800
                            )
                        )
                    }
                }
            }

            // Interactive Test Button
            SulaoneButton(
                text = "Uji Sensor Sidik Jari Sekarang",
                onClick = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        BiometricVault.authenticate(
                            activity = activity,
                            title = "Verifikasi Sidik Jari SISTA",
                            subtitle = "Sentuh sensor sidik jari pada HP Anda untuk verifikasi",
                            negativeButtonText = "Batal",
                            onSuccess = {
                                haptics.tapHeavy()
                                viewModel.onFingerprintTestSuccess()
                                Toast.makeText(context, "Sidik jari berhasil diverifikasi!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                haptics.tapLight()
                                viewModel.onFingerprintTestError(error)
                            }
                        )
                    } else {
                        Toast.makeText(context, "FragmentActivity tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                },
                icon = Icons.Default.Fingerprint,
                containerColor = Emerald600,
                modifier = Modifier.fillMaxWidth()
            )

            // Result Feedback Card
            AnimatedVisibility(visible = uiState.fingerprintStatusMessage != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.fingerprintTestSuccess) Emerald900.copy(alpha = 0.4f) else Slate800
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.fingerprintTestSuccess) Emerald500 else Gold500
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.fingerprintTestSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (uiState.fingerprintTestSuccess) Emerald400 else Gold400,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.fingerprintStatusMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                    }
                }
            }

            // Bank-Grade Security Info Banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🔒 Terproteksi Enkripsi Hardware TEE & Android Keystore: Data sidik jari Anda diproses 100% oleh chip keamanan lokal smartphone dan tidak pernah disimpan maupun ditransmisikan ke server sekolah.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
