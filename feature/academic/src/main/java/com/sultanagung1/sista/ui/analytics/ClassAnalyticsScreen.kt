package com.sultanagung1.sista.ui.analytics

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassAnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val classDataState by viewModel.classAnalytics.collectAsState()
    val classOptions by viewModel.teacherClassOptions.collectAsState()
    val selectedClass by viewModel.selectedTeacherClass.collectAsState()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Analitik Hasil Belajar Kelas",
                subtitle = selectedClass?.let { "${it.subjectName} • ${it.className}" } ?: "Memuat kelas yang diampu...",
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
            if (classOptions.size > 1) {
                val optionLabels = classOptions.map { "${it.subjectName} • ${it.className}" }
                SulaoneDropdown(
                    selectedValue = selectedClass?.let { "${it.subjectName} • ${it.className}" } ?: "",
                    onValueSelected = { label ->
                        val index = optionLabels.indexOf(label)
                        if (index >= 0) viewModel.selectTeacherClass(classOptions[index])
                    },
                    options = optionLabels,
                    label = "Pilih Kelas & Mata Pelajaran",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when (val classResult = classDataState) {
                is AnalyticsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Emerald700) }

                is AnalyticsUiState.Error -> SulaoneErrorBanner(
                    message = classResult.message,
                    onRetry = { viewModel.loadClassAnalytics() }
                )

                is AnalyticsUiState.Success -> {
                val data = classResult.data
                // Summary Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(title = "Rata-rata Kelas", value = "${data.classAverage}", modifier = Modifier.weight(1f), isGood = true)
                    MetricBox(title = "Kelulusan KKTP", value = "${data.passRatePercentage}%", modifier = Modifier.weight(1f), isGood = true)
                    MetricBox(title = "Nilai Tertinggi", value = "${data.highestScore}", modifier = Modifier.weight(1f), isGood = true)
                }

                // Score Distribution Histogram
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Histogram Distribusi Nilai Siswa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    data.distributionBuckets.forEach { bucket ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(bucket.rangeLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("${bucket.count} Siswa (${bucket.percentage}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { bucket.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (bucket.rangeLabel.contains("< 75")) AccentRose else Emerald600
                            )
                        }
                    }
                }

                // At-Risk & Remedial Students List
                SulaoneCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = AccentRose.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccentRose, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Siswa Butuh Pendampingan (${data.atRiskStudents.size})", fontWeight = FontWeight.Bold, color = AccentRose)
                        }
                        SulaoneBadge(text = "Perlu Remedial", containerColor = AccentRose.copy(alpha = 0.15f), contentColor = AccentRose)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    data.atRiskStudents.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(student.recommendation, fontSize = 10.sp, color = Slate600)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentRose)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Skor: ${student.currentScore}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(title: String, value: String, modifier: Modifier = Modifier, isGood: Boolean = true) {
    SulaoneCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isGood) Emerald800 else AccentRose)
        }
    }
}
