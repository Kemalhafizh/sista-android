package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.LinearProgressIndicator
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
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.ParentProgressData
import java.time.LocalDate
import java.time.ZoneId

/**
 * One child's progress this year: their average against the class, this
 * month's attendance day by day, and the hafalan target.
 */
@Composable
fun ChildProgressScreen(
    viewModel: ChildProgressViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) refreshRequested = false }

    ChildProgressContent(
        state = state,
        today = remember(state.data) { LocalDate.now(ZoneId.of("Asia/Jakarta")) },
        refreshing = refreshRequested && state.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.load()
        },
        onRetry = viewModel::load,
        onNavigateBack = onNavigateBack,
    )
}

/** The child progress screen without a ViewModel, for previews and screenshots. */
@Composable
fun ChildProgressContent(
    state: ChildProgressUiState,
    today: LocalDate,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val data = state.data
    AnalyticsScaffold(
        title = "Progres belajar",
        subtitle = data?.let { listOf(it.childName, it.childClass.takeIf(String::isNotBlank)?.let { c -> "Kelas $c" }) }
            ?.filterNot { it.isNullOrBlank() }?.joinToString(" · ")?.ifBlank { null },
        refreshing = refreshing,
        onRefresh = onRefresh,
        onNavigateBack = onNavigateBack,
        testTag = "child_progress_root",
    ) {
        when {
            data == null && state.errorMessage != null -> item(key = "error") {
                ErrorState(title = "Progres anak belum bisa dimuat", body = state.errorMessage, onRetry = onRetry)
            }
            data == null -> item(key = "loading") { SkeletonList(rows = 4) }
            else -> {
                state.errorMessage?.let { message ->
                    item(key = "stale") { InlineBanner(message = "Data belum diperbarui. $message", tone = StatusTone.Warning) }
                }
                item(key = "scores") { Scores(data) }
                item(key = "attendance_header") { SectionHeader("Presensi ${monthLabel(today)}") }
                item(key = "attendance") { Attendance(data, today) }
                item(key = "tahfidz_header") { SectionHeader("Hafalan") }
                item(key = "tahfidz") { Tahfidz(data) }
            }
        }
    }
}

@Composable
private fun Scores(data: ParentProgressData) {
    val child = data.academicScore
    val cls = data.classAverageScore
    Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        StatTile(
            label = "Rata-rata anak",
            value = decimal(child),
            supporting = when {
                child == null -> "belum ada nilai"
                cls == null -> "tahun ajaran ini"
                else -> "${signed(child - cls)} dari kelas"
            },
            icon = Icons.Outlined.Grade,
            tone = when {
                child == null || cls == null -> StatusTone.Neutral
                child >= cls -> StatusTone.Success
                else -> StatusTone.Warning
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            label = "Rata-rata kelas",
            value = decimal(cls),
            supporting = if (cls == null) "belum ada nilai" else "seluruh siswa kelas",
            icon = Icons.Outlined.Groups,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun Attendance(data: ParentProgressData, today: LocalDate) {
    val marks = data.attendanceHeatmap.associate { it.dayNumber to dayMarkOf(it.status) }
    val counted = marks.filterKeys { it <= today.dayOfMonth }.values
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            AttendanceMonth(monthGrid(today, marks))
            Row(Modifier.fillMaxWidth()) {
                listOf(DayMark.Present, DayMark.Sick, DayMark.Permit, DayMark.Absent).forEach { mark ->
                    Legend(mark, counted.count { it == mark }, Modifier.weight(1f))
                }
            }
            val unrecorded = counted.count { it == DayMark.Unrecorded }
            if (unrecorded > 0) {
                Text(
                    "$unrecorded hari tanpa catatan presensi: akhir pekan, libur, atau belum dicatat.",
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Tahfidz(data: ParentProgressData) {
    val target = data.tahfidzTargetJuz?.takeIf { it > 0 }
    val achieved = data.tahfidzCurrentJuz ?: 0
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconBadge(icon = Icons.Outlined.AutoStories, tone = StatusTone.Brand)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (target == null) {
                    Text("Belum ada target hafalan tahun ini", style = SistaTheme.typography.titleSmall)
                } else {
                    Text("$achieved dari $target juz", style = SistaTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { (achieved.toFloat() / target).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = SistaTheme.colors.primary,
                        trackColor = SistaTheme.colors.surfaceVariant,
                        drawStopIndicator = {},
                    )
                }
                Text(
                    if (data.totalSurahCompleted > 0) "${data.totalSurahCompleted} surah sudah disetor." else "Belum ada setoran tercatat.",
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}
