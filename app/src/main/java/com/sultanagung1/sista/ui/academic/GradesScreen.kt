/**
 * FASE 67: Overhauled Rapor & Capaian KKTP Screen.
 * Modern flat design: Slate50 canvas, 0dp elevation cards, 0.5dp hairline borders,
 * WCAG 2.2 AA typography, Emerald600 accents, haptic micro-interactions.
 */
package com.sultanagung1.sista.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Star
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
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.GradeItem

@Composable
fun GradesScreen(
    viewModel: AcademicViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()
    val isDark = isSystemInDarkTheme()

    var selectedSemester by remember { mutableStateOf("Semester Ganjil 2026") }
    val semesters = listOf("Semester Ganjil 2026", "Semester Genap 2025", "Transkrip Kumulatif")

    val sampleGrades = listOf(
        GradeItem("Matematika Peminatan", "Drs. H. Ahmad Fauzi", 92.0, 88.0, 95.0, 92.2, "A", "Tuntas"),
        GradeItem("Fisika Modern", "Dr. Hj. Siti Nurjanah", 85.0, 90.0, 88.0, 87.8, "A-", "Tuntas"),
        GradeItem("Kimia Organik", "Dra. Hj. Sri Wahyuni", 88.0, 84.0, 86.0, 86.0, "B+", "Tuntas"),
        GradeItem("Biologi Molekuler", "M. Ihsan, M.Sc", 94.0, 92.0, 96.0, 94.2, "A", "Tuntas"),
        GradeItem("Pendidikan Agama Islam", "Ust. M. Rizqi, Lc", 98.0, 96.0, 97.0, 97.0, "A", "Tuntas"),
        GradeItem("Bahasa Arab", "Ust. Abdullah, Lc", 90.0, 92.0, 94.0, 92.0, "A", "Tuntas")
    )

    val borderColor = if (isDark) Slate800 else Slate200

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Rapor & Capaian KKTP",
                subtitle = "Fase F • Kelas XII MIPA 1",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
        ) {
            item {
                // GPA & Class Rank Summary Banner (Modern Bento with Ambient Glow)
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = Color.Transparent,
                    glowColor = EmeraldGlow
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Emerald900, Emerald800, Emerald700))
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LiveStatusChip("KKTP TUNTAS", color = Gold400)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kurikulum Merdeka",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Emerald200
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "92.4",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Predikat A • Peringkat 2 dari 36 Siswa",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Emerald100
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Gold400)
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Slate950,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Semester selector tabs
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(semesters) { sem ->
                        val isSelected = selectedSemester == sem
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Emerald600 else Color.Transparent)
                                .border(
                                    0.5.dp,
                                    if (isSelected) Emerald600 else borderColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .springPressable {
                                    haptics.tapLight()
                                    selectedSemester = sem
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sem,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else if (isDark) Slate400 else Slate600
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Daftar Nilai Mata Pelajaran",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val displayList = uiState.academicSummary?.grades ?: sampleGrades

            items(displayList) { item ->
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 0.dp,
                    backgroundColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                    borderColor = borderColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.subjectName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.teacherName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ScorePill("Tugas", item.assignmentScore)
                                ScorePill("UTS", item.midtermScore)
                                ScorePill("UAS", item.finalScore)
                            }
                        }

                        val isGradeA = item.letterGrade.startsWith("A")
                        val gradeBadgeBg = if (isDark) {
                            if (isGradeA) Emerald900.copy(alpha = 0.4f) else AccentAmber.copy(alpha = 0.2f)
                        } else {
                            if (isGradeA) Emerald100 else AccentAmber.copy(alpha = 0.2f)
                        }
                        val gradeBadgeBorder = if (isDark) {
                            if (isGradeA) Emerald500.copy(alpha = 0.5f) else AccentAmber.copy(alpha = 0.4f)
                        } else {
                            if (isGradeA) Emerald300 else AccentAmber.copy(alpha = 0.4f)
                        }
                        val gradeBadgeTextColor = if (isDark) {
                            if (isGradeA) Emerald300 else Gold400
                        } else {
                            if (isGradeA) Emerald800 else AccentAmber
                        }

                        // Final Grade Badge
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(gradeBadgeBg)
                                .border(1.dp, gradeBadgeBorder, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.letterGrade,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = gradeBadgeTextColor
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun ScorePill(label: String, score: Double) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${score.toInt()}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
