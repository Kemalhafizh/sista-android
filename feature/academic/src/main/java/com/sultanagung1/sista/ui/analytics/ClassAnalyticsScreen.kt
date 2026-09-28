package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.AtRiskStudentItem
import com.sultanagung1.sista.data.model.ClassAnalyticsData
import com.sultanagung1.sista.data.model.ScoreDistributionBucket
import com.sultanagung1.sista.data.model.TeacherClassOption

/**
 * A teacher's class analytics: pick one of the classes taught this year,
 * then its average, pass rate, score spread, and who is below the KKM.
 */
@Composable
fun ClassAnalyticsScreen(
    viewModel: ClassAnalyticsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val loading = state.isLoadingClasses || state.isLoadingPerformance
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(loading) { if (!loading) refreshRequested = false }

    ClassAnalyticsContent(
        state = state,
        refreshing = refreshRequested && loading,
        onRefresh = {
            refreshRequested = true
            viewModel.load()
        },
        onRetry = viewModel::load,
        onSelect = viewModel::select,
        onNavigateBack = onNavigateBack,
    )
}

/** The class analytics screen without a ViewModel, for previews and screenshots. */
@Composable
fun ClassAnalyticsContent(
    state: ClassAnalyticsUiState,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSelect: (TeacherClassOption) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val classes = state.classes
    val selected = state.selected
    val performance = state.performance
    AnalyticsScaffold(
        title = "Analitik kelas",
        subtitle = selected?.let { "${it.className} · ${it.subjectName}" },
        refreshing = refreshing,
        onRefresh = onRefresh,
        onNavigateBack = onNavigateBack,
        testTag = "class_analytics_root",
    ) {
        when {
            classes == null && state.classesError != null -> item(key = "classes_error") {
                ErrorState(title = "Daftar kelas belum bisa dimuat", body = state.classesError, onRetry = onRetry)
            }
            classes == null -> item(key = "classes_loading") { SkeletonList(rows = 4) }
            classes.isEmpty() -> item(key = "no_classes") {
                EmptyState(
                    title = "Belum ada kelas tahun ini",
                    body = "Kelas muncul dari jadwal, penilaian harian, atau nilai yang Anda catat tahun ajaran ini.",
                    icon = Icons.Outlined.Groups,
                )
            }
            else -> {
                if (classes.size > 1 && selected != null) {
                    item(key = "picker") {
                        FilterChipRow(
                            options = classes,
                            selected = selected,
                            onSelect = onSelect,
                            label = { "${it.className} · ${it.subjectName}" },
                            contentPadding = PaddingValues(0.dp),
                        )
                    }
                }
                when {
                    performance == null && state.performanceError != null -> item(key = "performance_error") {
                        ErrorState(
                            title = "Analitik kelas belum bisa dimuat",
                            body = state.performanceError,
                            onRetry = { selected?.let(onSelect) },
                        )
                    }
                    performance == null -> item(key = "performance_loading") { SkeletonList(rows = 3) }
                    performance.classAverage == null -> item(key = "no_grades") {
                        EmptyState(
                            title = "Belum ada nilai untuk kelas ini",
                            body = "Angka muncul setelah nilai ${performance.subjectName} dicatat.",
                        )
                    }
                    else -> {
                        state.performanceError?.let { message ->
                            item(key = "stale") { InlineBanner(message = "Data belum diperbarui. $message", tone = StatusTone.Warning) }
                        }
                        item(key = "figures") { Figures(performance) }
                        item(key = "spread_header") { SectionHeader("Sebaran nilai") }
                        item(key = "spread") { Spread(performance.distributionBuckets) }
                        item(key = "risk_header") { SectionHeader("Rata-rata di bawah KKM (${performance.atRiskStudents.size})") }
                        item(key = "risk") { BelowKkm(performance.atRiskStudents) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Figures(data: ClassAnalyticsData) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile(
                label = "Rata-rata kelas",
                value = decimal(data.classAverage),
                supporting = data.kkm?.let { "KKM ${decimal(it)}" },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatTile(
                label = "Nilai tuntas",
                value = data.passRatePercentage?.let { "${decimal(it)}%" } ?: "–",
                supporting = "mencapai KKM",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile(label = "Tertinggi", value = data.highestScore?.toString() ?: "–", modifier = Modifier.weight(1f).fillMaxHeight())
            StatTile(label = "Terendah", value = data.lowestScore?.toString() ?: "–", modifier = Modifier.weight(1f).fillMaxHeight())
        }
        coverage(data)?.let {
            Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
        }
    }
}

/** How much of the class the figures cover: scores counted, and students graded of all. */
internal fun coverage(data: ClassAnalyticsData): String? {
    val scores = data.scoresCount ?: return null
    val graded = data.studentsGraded
    val all = data.studentsCount
    return when {
        graded != null && all != null -> "Dari ${thousands(scores)} nilai, $graded dari $all siswa sudah dinilai."
        else -> "Dari ${thousands(scores)} nilai."
    }
}

@Composable
private fun Spread(buckets: List<ScoreDistributionBucket>) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            buckets.forEach { bucket ->
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(bucket.rangeLabel, style = SistaTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            "${thousands(bucket.count)} nilai · ${decimal(bucket.percentage.toDouble())}%",
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(SistaTheme.colors.surfaceVariant),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth((bucket.percentage / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(SistaTheme.colors.primary),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BelowKkm(students: List<AtRiskStudentItem>) {
    if (students.isEmpty()) {
        EmptyState(
            title = "Tidak ada siswa di bawah KKM",
            body = "Rata-rata setiap siswa yang sudah dinilai mencapai KKM.",
            icon = Icons.Outlined.TaskAlt,
        )
        return
    }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            students.forEachIndexed { index, student ->
                if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(student.studentName, style = SistaTheme.typography.bodyLarge, maxLines = 1)
                        Text(
                            "Kurang ${student.kktpThreshold - student.currentScore} dari KKM ${student.kktpThreshold}",
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                    StatusPill(text = student.currentScore.toString(), tone = StatusTone.Danger)
                }
            }
        }
    }
}
