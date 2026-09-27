package com.sultanagung1.sista.ui.academic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.RaporCharacterItem
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.RaporEntryItem

@Composable
fun RaporDetailScreen(
    viewModel: RaporViewModel,
    childId: Long? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPdfViewer: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.fetchRapor(childId)
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Rapor Kurikulum Merdeka",
                onNavigateBack = onNavigateBack,
                actions = {
                    val pdfStatus = uiState.raporData?.pdfStatus
                    if (pdfStatus?.isReady == true && !pdfStatus.filePath.isNullOrBlank()) {
                        val studentName = uiState.raporData?.student?.name ?: "Siswa"
                        IconButton(onClick = { onNavigateToPdfViewer(pdfStatus.filePath, "Rapor $studentName") }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Buka PDF", tint = Emerald700)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Emerald700)
            }
        } else if (uiState.raporData != null) {
            val data = uiState.raporData!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Student Info & GPA Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald800)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = data.student.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "NISN: ${data.student.nisn} | ${data.student.className}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald100
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = Emerald700
                                ) {
                                    Box(
                                        modifier = Modifier.size(54.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = data.academicSummary.predikatUmum,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Gold400
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Emerald700.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Rata-Rata Nilai", style = MaterialTheme.typography.labelSmall, color = Emerald200)
                                    Text("${data.academicSummary.averageScore}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Mata Pelajaran", style = MaterialTheme.typography.labelSmall, color = Emerald200)
                                    Text("${data.academicSummary.totalSubjects} Mapel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Capaian Pembelajaran & Nilai (KKTP)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                }

                items(data.entries) { entry ->
                    RaporEntryCard(entry = entry)
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Profil Pelajar Pancasila & Karakter Islami",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                }

                items(data.characters) { character ->
                    RaporCharacterCard(character = character)
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun RaporEntryCard(entry: RaporEntryItem) {
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.subjectName ?: "Mata Pelajaran",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (entry.predikat) {
                    "A" -> Emerald100
                    "B" -> Emerald50
                    else -> Gold100
                }
            ) {
                Text(
                    text = "${entry.score.toInt()} (${entry.predikat})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (entry.predikat) {
                        "A" -> Emerald900
                        "B" -> Emerald700
                        else -> Gold800
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        if (!entry.capaianKompetensi.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Capaian Kompetensi:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Emerald700
            )
            Text(
                text = entry.capaianKompetensi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!entry.catatanGuru.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Catatan Pendidik: ${entry.catatanGuru}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )
        }
    }
}

@Composable
private fun RaporCharacterCard(character: RaporCharacterItem) {
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = character.dimension,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Emerald100
            ) {
                Text(
                    text = character.grade,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        if (!character.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = character.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
