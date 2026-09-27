package com.sultanagung1.sista.ui.settings

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val fontScale by viewModel.fontScale.collectAsState()
    val isDyslexicFriendly by viewModel.isDyslexicFriendly.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Aksesibilitas & Inklusivitas",
                subtitle = "Kemudahan Akses untuk Semua Pengguna",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 1. Font Scaling Slider Section
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ukuran Teks Dinamis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Skala ukuran: ${(fontScale * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("A", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate500)
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.setFontScale(it) },
                        valueRange = 0.75f..2.0f,
                        steps = 4,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Emerald700,
                            activeTrackColor = Emerald700
                        )
                    )
                    Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("75%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Text("125%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Text("150%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Text("200%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dyslexia-Friendly Toggle Section
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Ramah Disleksia",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Meningkatkan spasi antar-huruf dan baris teks untuk kenyamanan membaca optimal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDyslexicFriendly,
                        onCheckedChange = { viewModel.setDyslexicFriendly(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald700, checkedTrackColor = Emerald100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. High Contrast Mode Toggle
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Kontras Tinggi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Menonjolkan batas elemen dan teks hitam-putih pekat bagi pengguna dengan keterbatasan penglihatan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isHighContrast,
                        onCheckedChange = { viewModel.setHighContrast(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald700, checkedTrackColor = Emerald100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Live Typography Preview
            Text(
                text = "Pratinjau Keterbacaan Teks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Jadwal Pelajaran: Fisika Quantum",
                        fontSize = (18 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (isDyslexicFriendly) 1.5.sp else 0.sp,
                        lineHeight = if (isDyslexicFriendly) (26 * fontScale).sp else (22 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ruang Laboratorium Sains 3 • Pukul 07.30 - 09.00 WIB bersama Ibu Lestari, S.Pd.",
                        fontSize = (14 * fontScale).sp,
                        letterSpacing = if (isDyslexicFriendly) 1.2.sp else 0.sp,
                        lineHeight = if (isDyslexicFriendly) (20 * fontScale).sp else (18 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
