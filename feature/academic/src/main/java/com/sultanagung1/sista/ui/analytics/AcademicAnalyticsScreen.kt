package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.SemesterTrendPoint
import com.sultanagung1.sista.data.model.StudentAnalyticsData
import com.sultanagung1.sista.data.model.SubjectAverage

/**
 * The student's own figures for this academic year: average and attendance,
 * every graded subject against its KKM, and the report cards so far.
 */
@Composable
fun AcademicAnalyticsScreen(
    viewModel: StudentAnalyticsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) refreshRequested = false }

    StudentAnalyticsContent(
        state = state,
        refreshing = refreshRequested && state.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.load()
        },
        onRetry = viewModel::load,
        onNavigateBack = onNavigateBack,
    )
}

/** The student analytics screen without a ViewModel, for previews and screenshots. */
@Composable
fun StudentAnalyticsContent(
    state: StudentAnalyticsUiState,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val data = state.data
    AnalyticsScaffold(
        title = "Analitik belajar",
        subtitle = data?.className?.takeIf { it.isNotBlank() }?.let { "Kelas $it · tahun ajaran ini" },
        refreshing = refreshing,
        onRefresh = onRefresh,
        onNavigateBack = onNavigateBack,
        testTag = "student_analytics_root",
    ) {
        when {
            data == null && state.errorMessage != null -> item(key = "error") {
                ErrorState(title = "Analitik belum bisa dimuat", body = state.errorMessage, onRetry = onRetry)
            }
            data == null -> item(key = "loading") { SkeletonList(rows = 4) }
            else -> {
                val subjects = subjectsOf(data)
                state.errorMessage?.let { message ->
                    item(key = "stale") { InlineBanner(message = "Data belum diperbarui. $message", tone = StatusTone.Warning) }
                }
                item(key = "overview") { Overview(data, subjects) }
                item(key = "subjects_header") { SectionHeader("Per mata pelajaran") }
                item(key = "subjects") { Subjects(subjects) }
                item(key = "reports_header") { SectionHeader("Rapor per semester") }
                item(key = "reports") { ReportCards(data.semesterTrends) }
            }
        }
    }
}

/** Every graded subject; an older server only sent up to six, as radar axes. */
internal fun subjectsOf(data: StudentAnalyticsData): List<SubjectAverage> =
    data.subjects ?: data.competencyRadar.map { SubjectAverage(it.label, it.value.toDouble(), it.targetKktp.toDouble()) }

@Composable
private fun Overview(data: StudentAnalyticsData, subjects: List<SubjectAverage>) {
    Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        StatTile(
            label = "Rata-rata nilai",
            value = decimal(data.overallAverage),
            supporting = if (subjects.isEmpty()) "belum ada nilai" else "dari ${subjects.size} mapel",
            icon = Icons.Outlined.Grade,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            label = "Kehadiran",
            value = data.attendanceRate?.let { "${decimal(it)}%" } ?: "–",
            supporting = data.attendanceRecorded?.takeIf { it > 0 }?.let { "dari ${thousands(it)} presensi" } ?: "belum ada presensi",
            icon = Icons.Outlined.EventAvailable,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun Subjects(subjects: List<SubjectAverage>) {
    if (subjects.isEmpty()) {
        EmptyState(
            title = "Belum ada nilai tahun ini",
            body = "Rata-rata per mapel muncul setelah guru mencatat nilai.",
            icon = Icons.AutoMirrored.Outlined.MenuBook,
        )
        return
    }
    val below = subjects.count { it.average < it.kkm }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(
                if (below == 0) "Semua mapel di atas KKM." else "$below dari ${subjects.size} mapel di bawah KKM.",
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
            )
            subjects.sortedBy { it.average }.forEach { SubjectBar(it) }
        }
    }
}

@Composable
private fun ReportCards(trends: List<SemesterTrendPoint>) {
    if (trends.isEmpty()) {
        EmptyState(title = "Belum ada rapor", body = "Rapor muncul setelah wali kelas menerbitkannya.")
        return
    }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            trends.forEachIndexed { index, trend ->
                if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                val change = trends.getOrNull(index - 1)?.let { trend.gpaScore - it.gpaScore }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(trend.semesterName, style = SistaTheme.typography.bodyLarge)
                        trend.rankInClass?.takeIf { it > 0 }?.let {
                            Text(
                                trend.totalStudents?.let { total -> "Peringkat $it dari $total" } ?: "Peringkat $it",
                                style = SistaTheme.typography.bodySmall,
                                color = SistaTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                    change?.let {
                        StatusPill(text = signed(it.toDouble()), tone = if (it >= 0) StatusTone.Success else StatusTone.Warning)
                    }
                    Text(decimal(trend.gpaScore.toDouble()), style = SistaTheme.typography.titleMedium)
                }
            }
        }
    }
}
