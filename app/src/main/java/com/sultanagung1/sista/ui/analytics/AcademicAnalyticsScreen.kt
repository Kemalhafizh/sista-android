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
import com.sultanagung1.sista.ui.analytics.components.RadarChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicAnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val analytics by viewModel.studentAnalytics.collectAsState()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Analitik Akademik & Kompetensi",
                subtitle = "Pemetaan Capaian KKTP Kurikulum Merdeka",
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
            analytics?.let { data ->
                // Overall Score Header Card
                SulaoneGradientCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "RATA-RATA NILAI AKADEMIK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold400
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${data.overallAverage} / 100",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Predikat A (Sangat Memuaskan) • Peringkat 1",
                                fontSize = 11.sp,
                                color = Emerald100
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Gold400),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Slate950,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // 6-Axis Radar Competency Chart
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spider Radar 6-Sumbu KKTP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            SulaoneBadge(
                                text = "KKTP: 75",
                                containerColor = Gold100,
                                contentColor = Gold800
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        RadarChart(data = data.competencyRadar)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Competency legend summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            data.competencyRadar.take(3).forEach { pt ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(pt.label, fontSize = 10.sp, color = Slate600)
                                    Text("${pt.value.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                                }
                            }
                        }
                    }
                }

                // Semester Progress Trend
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tren Kenaikan Nilai per Semester",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    data.semesterTrends.forEach { trend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(trend.semesterName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Peringkat ${trend.rankInClass}/${trend.totalStudents}", fontSize = 10.sp, color = Slate500)
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Emerald100)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("${trend.gpaScore}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald900)
                                }
                            }
                        }
                    }
                }

                // Top 5 Highest Subject Performances
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mata Pelajaran Tertinggi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    data.topSubjects.forEach { (sub, sc) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sub, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("$sc", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { sc / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp)),
                                color = Emerald700
                            )
                        }
                    }
                }
            }
        }
    }
}
