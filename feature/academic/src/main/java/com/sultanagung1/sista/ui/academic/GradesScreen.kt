/**
 * FASE 67: Overhauled Rapor & Capaian KKTP Screen.
 * Modern flat design: Slate50 canvas, 0dp elevation cards, 0.5dp hairline borders,
 * WCAG 2.2 AA typography, Emerald600 accents, haptic micro-interactions.
 *
 * Renders GET student/grades exactly as the backend's Grade model actually
 * shapes it: one entry per graded assessment (subject, type e.g. "UH"/"UTS"/
 * "UAS", score, date) — there is no assignment/midterm/final rollup, GPA, or
 * class-rank concept on the backend, so none of those are fabricated here.
 * Entries are grouped by subject and each subject's average is a real
 * computation over its own entries, not invented.
 */
package com.sultanagung1.sista.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentAmber
import com.sultanagung1.sista.core.designsystem.Emerald100
import com.sultanagung1.sista.core.designsystem.Emerald200
import com.sultanagung1.sista.core.designsystem.Emerald700
import com.sultanagung1.sista.core.designsystem.Emerald800
import com.sultanagung1.sista.core.designsystem.Emerald900
import com.sultanagung1.sista.core.designsystem.EmeraldGlow
import com.sultanagung1.sista.core.designsystem.Gold400
import com.sultanagung1.sista.core.designsystem.ModernBentoCard
import com.sultanagung1.sista.core.designsystem.Slate200
import com.sultanagung1.sista.core.designsystem.Slate50
import com.sultanagung1.sista.core.designsystem.Slate500
import com.sultanagung1.sista.core.designsystem.Slate800
import com.sultanagung1.sista.core.designsystem.Slate950
import com.sultanagung1.sista.core.designsystem.SulaoneEmptyState
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SkeletonBox
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.GradeEntry
import kotlin.math.roundToInt

private data class SubjectGrades(val subject: String, val entries: List<GradeEntry>) {
    val average: Double get() = entries.map { it.score }.average()
}

@Composable
fun GradesScreen(
    viewModel: AcademicViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val borderColor = if (isDark) Slate800 else Slate200
    val bySubject = remember(uiState.grades) {
        uiState.grades
            .groupBy { it.subject }
            .map { (subject, entries) -> SubjectGrades(subject, entries.sortedByDescending { it.date }) }
            .sortedBy { it.subject }
    }
    val overallAverage = remember(uiState.grades) {
        uiState.grades.takeIf { it.isNotEmpty() }?.map { it.score }?.average()
    }

    // FASE 76.2: the list scrolls under a see-through top bar; the hairline
    // appears once content is actually passing beneath it.
    val listState = rememberLazyListState()
    val listScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Nilai & Capaian",
                subtitle = "Berdasarkan nilai yang diinput guru",
                onNavigateBack = onNavigateBack,
                translucent = true,
                showDivider = listScrolled
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 4.dp, bottom = 28.dp)
        ) {
            item {
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
                    glowColor = EmeraldGlow
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Emerald900, Emerald800, Emerald700)))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rata-rata Nilai",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald200
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = overallAverage?.let { "%.1f".format(it) } ?: "—",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Text(
                                    text = if (uiState.grades.isEmpty()) "Belum ada nilai tercatat"
                                           else "${bySubject.size} Mata Pelajaran • ${uiState.grades.size} Nilai",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Emerald100
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Gold400)
                                    .border(2.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f), CircleShape),
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

            if (uiState.errorMessage != null) {
                item {
                    SulaoneErrorBanner(
                        message = uiState.errorMessage ?: "Gagal memuat nilai.",
                        onRetry = { viewModel.loadGrades() }
                    )
                }
            } else if (uiState.grades.isEmpty() && uiState.isLoading) {
                // FASE 76.5: this state used to render nothing at all.
                item { SulaoneTieredLoading(isLoading = true) { GradesSkeleton() } }
            } else if (uiState.grades.isEmpty() && !uiState.isLoading) {
                item {
                    SulaoneEmptyState(
                        icon = Icons.Default.Star,
                        title = "Belum Ada Nilai",
                        description = "Nilai akan muncul di sini setelah guru menginputnya."
                    )
                }
            }

            items(bySubject, key = { it.subject }) { group ->
                ModernBentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 0.dp,
                    backgroundColor = if (isDark) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.White,
                    borderColor = borderColor
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.subject,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (group.average >= 75) Emerald100.copy(alpha = if (isDark) 0.25f else 1f)
                                        else AccentAmber.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Rata-rata ${group.average.roundToInt()}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (group.average >= 75) Emerald800 else AccentAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        group.entries.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = entry.type,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val description = entry.description
                                    if (!description.isNullOrBlank()) {
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    text = "${entry.score.roundToInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (entry.score >= 75) Emerald700 else AccentAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Mirrors a subject card: subject name + average pill, then assessment rows. */
@Composable
private fun GradesSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(18.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    SkeletonBox(modifier = Modifier.width(52.dp).height(26.dp), shape = RoundedCornerShape(13.dp))
                }
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(12.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
            }
        }
    }
}
