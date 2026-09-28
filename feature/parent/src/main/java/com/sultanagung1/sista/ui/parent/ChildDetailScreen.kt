package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.ChildAttendanceLog
import com.sultanagung1.sista.data.model.ChildGradeItem

private val TABS = listOf("Nilai", "Presensi")

/**
 * One child's grades (`parent/child/{uuid}/grades`) and latest attendance
 * (`parent/child/{uuid}/attendance`), opened for the child in the route.
 */
@Composable
fun ChildDetailScreen(
    studentId: String,
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    // This screen has its own ViewModel; show the child in the route, not the first one.
    LaunchedEffect(studentId) { viewModel.focusChild(studentId) }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val child = uiState.selectedChild?.takeIf { it.uuid == studentId }
    ChildDetailContent(
        name = child?.name,
        details = listOfNotNull(child?.classroom?.takeIf { it.isNotBlank() && it != "N/A" }?.let { "Kelas $it" }, child?.nis?.let { "NIS $it" }),
        grades = if (child != null) uiState.childGrades else emptyList(),
        attendance = if (child != null) uiState.childAttendanceLogs else emptyList(),
        loading = child == null && uiState.errorMessage == null || uiState.isLoadingChildDetail,
        errorMessage = uiState.errorMessage ?: uiState.childErrorMessage.takeIf { child != null },
        notFound = !uiState.isLoading && uiState.children.isNotEmpty() && uiState.children.none { it.uuid == studentId },
        tab = tab,
        onTab = { tab = it },
        onRetry = viewModel::refresh,
        onNavigateBack = onNavigateBack,
    )
}

/** The child detail without a ViewModel, for previews and screenshots. */
@Composable
fun ChildDetailContent(
    name: String?,
    details: List<String>,
    grades: List<ChildGradeItem>,
    attendance: List<ChildAttendanceLog>,
    loading: Boolean,
    errorMessage: String?,
    notFound: Boolean,
    tab: Int,
    onTab: (Int) -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = name ?: "Detail anak",
                    subtitle = details.joinToString(" · ").ifBlank { null },
                    onBack = onNavigateBack,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "tabs") {
                    FilterChipRow(
                        options = TABS.indices.toList(),
                        selected = tab,
                        onSelect = onTab,
                        label = { TABS[it] },
                        contentPadding = PaddingValues(vertical = Spacing.sm),
                    )
                }
                when {
                    notFound -> item(key = "not_found") {
                        EmptyState(title = "Anak tidak ditemukan", body = "Data ini bukan milik anak yang ditautkan ke akun Anda.")
                    }
                    errorMessage != null && grades.isEmpty() && attendance.isEmpty() -> item(key = "error") {
                        ErrorState(title = "Data anak belum bisa dimuat", body = errorMessage, onRetry = onRetry)
                    }
                    loading && grades.isEmpty() && attendance.isEmpty() -> item(key = "loading") { SkeletonList(rows = 5) }
                    tab == 0 -> gradesTab(grades)
                    else -> attendanceTab(attendance)
                }
            }
        }
    }
}

private val GRADE_TYPES = mapOf("uh" to "Ulangan harian", "tugas" to "Tugas", "pts" to "PTS", "pas" to "PAS")

private fun androidx.compose.foundation.lazy.LazyListScope.gradesTab(grades: List<ChildGradeItem>) {
    if (grades.isEmpty()) {
        item(key = "no_grades") {
            EmptyState(
                title = "Belum ada nilai",
                body = "Nilai muncul di sini setelah guru memasukkannya.",
                icon = Icons.Outlined.Grade,
            )
        }
        return
    }
    item(key = "grade_summary") {
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Rata-rata", style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
                    Text(decimal(grades.map { it.score }.average()), style = SistaTheme.typography.headlineSmall)
                }
                StatusPill("${grades.size} nilai", StatusTone.Neutral)
            }
        }
    }
    grades.groupBy { it.subject }.toSortedMap().forEach { (subject, items) ->
        item(key = "subject_$subject", contentType = "subject") {
            Column {
                SectionHeader(subject, actionLabel = null)
                SistaCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        items.sortedByDescending { it.date }.forEachIndexed { index, grade ->
                            if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                            Row(Modifier.padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(GRADE_TYPES[grade.type.lowercase()] ?: grade.type.uppercase(), style = SistaTheme.typography.bodyLarge)
                                    Text(
                                        listOfNotNull(dateLabel(grade.date), grade.description?.takeIf { it.isNotBlank() }).joinToString(" · "),
                                        style = SistaTheme.typography.bodySmall,
                                        color = SistaTheme.colors.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(Modifier.width(Spacing.sm))
                                Text(decimal(grade.score), style = SistaTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ATTENDANCE_TONES = mapOf("H" to StatusTone.Success, "S" to StatusTone.Info, "I" to StatusTone.Info, "A" to StatusTone.Danger)

private fun androidx.compose.foundation.lazy.LazyListScope.attendanceTab(logs: List<ChildAttendanceLog>) {
    if (logs.isEmpty()) {
        item(key = "no_attendance") {
            EmptyState(
                title = "Belum ada presensi",
                body = "Presensi muncul di sini setelah guru mencatatnya.",
                icon = Icons.Outlined.EventAvailable,
            )
        }
        return
    }
    item(key = "attendance_summary") {
        val counts = logs.groupingBy { it.status.uppercase() }.eachCount()
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("${logs.size} catatan terakhir", style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("H" to "Hadir", "S" to "Sakit", "I" to "Izin", "A" to "Alpa").forEach { (code, label) ->
                    StatusPill("$label ${counts[code] ?: 0}", if ((counts[code] ?: 0) > 0) ATTENDANCE_TONES.getValue(code) else StatusTone.Neutral)
                }
            }
        }
    }
    item(key = "attendance_list") {
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                logs.forEachIndexed { index, log ->
                    if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                    Row(Modifier.padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(dateLabel(log.date) ?: log.date, style = SistaTheme.typography.bodyLarge)
                            log.notes?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant, maxLines = 2)
                            }
                        }
                        StatusPill(log.statusLabel, ATTENDANCE_TONES[log.status.uppercase()] ?: StatusTone.Neutral)
                    }
                }
            }
        }
    }
}
