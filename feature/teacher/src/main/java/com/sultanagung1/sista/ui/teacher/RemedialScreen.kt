package com.sultanagung1.sista.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sultanagung1.sista.data.model.RemedialItem
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.ui.teacher.DailyAssessmentViewModel

@Composable
fun RemedialScreen(
    viewModel: DailyAssessmentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStudentRemedials()
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Tanggungan Remedial Saya",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Banner Aturan Remedial K-Merdeka
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Gold100)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Gold700, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sesuai regulasi Kurikulum Merdeka, nilai perbaikan remedial maksimal setara KKM mata pelajaran (75). Selesaikan sebelum batas waktu!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gold800
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Emerald700)
                    }
                }
            } else if (uiState.remedials.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = Emerald700, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Alhamdulillah, Tidak Ada Remedial!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Seluruh capaian pembelajaran kamu telah tuntas melampaui KKM.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
            } else {
                items(uiState.remedials) { remedial ->
                    RemedialCardItem(item = remedial)
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun RemedialCardItem(item: RemedialItem) {
    val isCompleted = item.status == "completed"

    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subjectName ?: "Mata Pelajaran",
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald700,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.assessmentTitle ?: "Ulangan Harian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isCompleted) Emerald100 else AccentRose.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isCompleted) "TUNTAS" else "BELUM TUNTAS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCompleted) Emerald800 else AccentRose,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Nilai Asli: ${item.originalScore.toInt()}", fontSize = 12.sp, color = AccentRose, fontWeight = FontWeight.Bold)
            Text("KKM: ${item.kkm.toInt()}", fontSize = 12.sp, color = Slate600)
            if (item.remedialScore != null) {
                Text("Nilai Remedial: ${item.remedialScore.toInt()}", fontSize = 12.sp, color = Emerald700, fontWeight = FontWeight.Bold)
            }
        }

        if (!item.teacherNotes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tugas: ${item.teacherNotes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!item.deadline.isNullOrBlank() && !isCompleted) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = AccentRose, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Batas Waktu: ${item.deadline}", fontSize = 11.sp, color = AccentRose)
            }
        }
    }
}
