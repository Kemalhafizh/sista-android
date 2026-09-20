package com.sultanagung1.sista.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.lite.LiteModeManager
import com.sultanagung1.sista.core.motion.springPressable

@Composable
fun LiteModeSettingsScreen(
    liteModeManager: LiteModeManager? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { liteModeManager ?: LiteModeManager(context) }
    val isLiteMode by manager.isLiteMode.collectAsState()
    val isOfflineDownload by manager.isOfflineDownloadEnabled.collectAsState()
    val videoQuality by manager.videoQuality.collectAsState()

    val haptics = rememberHapticFeedbackHelper()
    val ramGb = remember { String.format("%.1f", manager.getDeviceRamGb()) }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Mode Hemat Kuota & HP Low-End",
                subtitle = "Optimasi Kinerja untuk RAM 2GB - 3GB",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Diagnostic Device Card
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Emerald50,
                    borderColor = Emerald300,
                    glowColor = EmeraldGlow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Emerald700),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kapasitas RAM Terdeteksi: $ramGb GB",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Emerald900
                            )
                            Text(
                                text = if (manager.getDeviceRamGb() <= 3.0) "Perangkat direkomendasikan mengaktifkan Lite Mode agar hemat baterai & tidak lag." else "Perangkat Anda mendukung performa penuh 120 FPS.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Emerald700
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Pengaturan Kinerja & Kuota",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Toggle Lite Mode
            item {
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aktifkan SULAONE Lite Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Menonaktifkan animasi kompleks, efek blur, dan bayangan berat agar enteng di HP Android spesifikasi pemula.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isLiteMode,
                            onCheckedChange = {
                                haptics.tapMedium()
                                manager.setLiteMode(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Emerald700)
                        )
                    }
                }
            }

            // Offline Material Download Toggle
            item {
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengunduhan Materi Belajar Offline",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Simpan modul PDF buku pelajaran di memori lokal agar bisa dibaca tanpa kuota internet.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOfflineDownload,
                            onCheckedChange = {
                                haptics.tapMedium()
                                manager.setOfflineDownload(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Emerald700)
                        )
                    }
                }
            }

            // Video Quality Selector
            item {
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kualitas Video Pembelajaran Streaming",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pilih resolusi optimal untuk menghemat kuota seluler siswa.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val qualities = listOf("360p (Hemat Kuota)", "480p (Standar)", "720p HD (Wi-Fi)")
                        qualities.forEach { q ->
                            val isSelected = videoQuality == q
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Emerald50 else Color.Transparent)
                                    .springPressable {
                                        haptics.tapLight()
                                        manager.setVideoQuality(q)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = q,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) Emerald900 else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Asset Delivery Info
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate100
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownloadDone, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dynamic Asset Delivery (App Bundle)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ukuran unduh Base APK dari Google Play Store dioptimalkan < 15MB. Modul berat (OCR, 3D Canvas) diunduh secara on-demand saat dibutuhkan.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate600
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
