package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun ChildDetailScreen(
    studentId: String,
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val child = uiState.selectedChild
    val logs = uiState.childAttendanceLogs

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Rapor & Nilai", "Riwayat Presensi", "Catatan Guru")

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = child?.name ?: "Detail Perkembangan Ananda",
                subtitle = "${child?.className ?: "Kelas XII"} • NISN: ${child?.nisn ?: "-"}",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald800
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            SulaoneCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Rata-rata Rapor (Fase F)", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                        Text(text = "92.4", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Emerald800)
                                        Text(text = "Predikat A (Sangat Memuaskan)", style = MaterialTheme.typography.bodySmall, color = Emerald900, fontWeight = FontWeight.Bold)
                                    }
                                    SulaoneBadge(text = "Peringkat 2 Kelas", containerColor = Gold100, contentColor = Gold900)
                                }
                            }
                        }

                        items(
                            listOf(
                                Triple("Matematika Tingkat Lanjut", 95.0, "Ustadz Ahmad Fauzi, M.Pd"),
                                Triple("Fisika Peminatan", 92.0, "Ustadzah Nurul Hidayah, S.Pd"),
                                Triple("Pendidikan Agama Islam & Budi Pekerti", 96.0, "Ustadz Drs. Bambang Suherman"),
                                Triple("Bahasa Arab", 90.0, "Ustadz Muhammad Luthfi, Lc"),
                                Triple("Bahasa Inggris Tingkat Lanjut", 88.0, "Ustadzah Siti Aminah, S.Pd")
                            )
                        ) { (mapel, score, teacher) ->
                            SulaoneCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = mapel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = "Pengampu: $teacher", style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = score.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Emerald700)
                                        Text(text = "KKTP: 75", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Text(
                                text = "Log Presensi Gerbang & Kelas (30 Hari Terakhir)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(logs) { log ->
                            SulaoneCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (log.isPunctual) Emerald600 else Gold600)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = log.date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(text = "Masuk: ${log.checkInTime} ${if (log.checkOutTime != null) "• Pulang: ${log.checkOutTime}" else ""}", style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                                        }
                                    }
                                    SulaoneBadge(text = log.status, containerColor = Emerald100, contentColor = Emerald900)
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            SulaoneCard {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "Catatan Khusus Wali Kelas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Emerald900)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"Ananda Muhammad Rizky menunjukkan keteladanan akhlak yang sangat baik, selalu hadir tepat waktu, dan aktif memimpin tadarus pagi di kelas XII MIPA 1.\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate700
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "— Ustadz Drs. H. Bambang Suherman (Wali Kelas)", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            SulaoneCard {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "Catatan Bimbingan Konseling (BK)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"Telah menyelesaikan asesmen peminatan karir SNBP/SNBT dengan rekomendasi Fakultas Teknik Informatika & Kedokteran PTN Favorit.\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate700
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "— Ustadzah Fatimah, S.Psi (Guru BK)", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
