package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.sultanagung1.sista.ui.analytics.components.HeatmapCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProgressScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val progressDataState by viewModel.parentProgress.collectAsState()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pantau Capaian Putra/Putri",
                subtitle = "SMA Islam Sultan Agung 1 Semarang",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val progressResult = progressDataState) {
                is AnalyticsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Emerald700) }

                is AnalyticsUiState.Error -> SulaoneErrorBanner(
                    message = progressResult.message,
                    onRetry = { viewModel.loadParentProgress() }
                )

                is AnalyticsUiState.Success -> {
                val data = progressResult.data
                // Child Bio & Overall Score
                SulaoneGradientCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Gold400),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = Slate950, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(data.childName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Kelas ${data.childClass}", fontSize = 11.sp, color = Emerald100)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Nilai Rata-rata", fontSize = 9.sp, color = Gold400)
                            Text("${data.academicScore}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Gold400)
                        }
                    }
                }

                // Attendance Heatmap
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    HeatmapCalendar(items = data.attendanceHeatmap)
                }

                // Tahfidz & Karakter Islami Progress
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Setoran Tahfidz & Karakter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        SulaoneBadge(
                            text = "Target: Juz ${data.tahfidzTargetJuz}",
                            containerColor = Emerald100,
                            contentColor = Emerald800
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Emerald50)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Emerald700),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Mosque, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val isComplete = data.tahfidzCurrentJuz >= data.tahfidzTargetJuz && data.tahfidzTargetJuz > 0
                            Text(
                                text = "Hafalan Juz ${data.tahfidzCurrentJuz}" + if (isComplete) " (Selesai)" else " dari target Juz ${data.tahfidzTargetJuz}",
                                fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald900
                            )
                            Text("${data.totalSurahCompleted} Surat telah disimak Ustadz", fontSize = 10.sp, color = Emerald700)
                        }

                        Icon(
                            imageVector = if (data.tahfidzCurrentJuz >= data.tahfidzTargetJuz && data.tahfidzTargetJuz > 0) Icons.Default.CheckCircle else Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Emerald700
                        )
                    }
                }
                }
            }
        }
    }
}
