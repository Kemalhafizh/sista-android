package com.sultanagung1.sista.ui.teacher

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.StudentAttendanceInputItem
import com.sultanagung1.sista.feature.teacher.R

/** How the screen words and colours each code of [AttendanceMarks]. */
private enum class Mark(val code: String, @StringRes val label: Int, val tone: StatusTone) {
    PRESENT(AttendanceMarks.PRESENT, R.string.tatt_present, StatusTone.Success),
    PERMIT(AttendanceMarks.PERMIT, R.string.tatt_permit, StatusTone.Warning),
    SICK(AttendanceMarks.SICK, R.string.tatt_sick, StatusTone.Info),
    ABSENT(AttendanceMarks.ABSENT, R.string.tatt_absent, StatusTone.Danger),
}

/**
 * Attendance for one lesson: everyone starts as present and the teacher marks
 * the exceptions, then it is sent to `POST teacher/attendance`. The server only
 * accepts a class the teacher teaches this year; its refusal is shown as sent.
 */
@Composable
fun TeacherAttendanceScreen(
    classroomId: Long,
    scheduleId: Long,
    className: String,
    viewModel: TeacherAttendanceViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(classroomId) { viewModel.load(classroomId) }
    LaunchedEffect(uiState.recordedCount) {
        val recorded = uiState.recordedCount ?: return@LaunchedEffect
        Toast.makeText(context, UiText.Res(R.string.tatt_saved, recorded).resolve(context), Toast.LENGTH_LONG).show()
        onNavigateBack()
    }

    TeacherAttendanceContent(
        className = className,
        state = uiState,
        onRetry = { viewModel.load(classroomId) },
        onMark = viewModel::setStatus,
        onMarkAllPresent = viewModel::markAllPresent,
        onSave = { viewModel.submit(scheduleId.takeIf { it > 0 }) },
        onNavigateBack = onNavigateBack,
    )
}

/** The attendance sheet without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherAttendanceContent(
    className: String,
    state: TeacherAttendanceUiState,
    onRetry: () -> Unit,
    onMark: (studentId: Long, code: String) -> Unit,
    onMarkAllPresent: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val students = state.students
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(R.string.tatt_title),
                    subtitle = className.takeIf { it.isNotBlank() }?.let { stringResource(R.string.th_class, it) },
                    onBack = onNavigateBack,
                )
            },
            bottomBar = {
                if (students.isNotEmpty()) {
                    Surface(color = SistaTheme.colors.surface, shadowElevation = 8.dp) {
                        SistaButton(
                            text = stringResource(R.string.tatt_save),
                            onClick = onSave,
                            leadingIcon = Icons.Outlined.Save,
                            loading = state.submitting,
                            enabled = !state.submitting,
                            fullWidth = true,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(horizontal = Spacing.screen, vertical = Spacing.md),
                        )
                    }
                }
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            when {
                state.loading && students.isEmpty() -> SkeletonList(Modifier.padding(padding), rows = 6)
                state.loadError != null && students.isEmpty() -> ErrorState(
                    title = stringResource(R.string.tatt_error),
                    body = state.loadError,
                    onRetry = onRetry,
                    modifier = Modifier
                        .padding(padding)
                        .padding(Spacing.screen),
                )
                students.isEmpty() -> EmptyState(
                    title = stringResource(R.string.tatt_empty),
                    body = stringResource(R.string.tatt_empty_body),
                    icon = Icons.Outlined.Groups,
                    modifier = Modifier
                        .padding(padding)
                        .padding(Spacing.screen),
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    item(key = "summary") { Summary(students, onMarkAllPresent, enabled = !state.submitting) }
                    if (state.submitError != null) {
                        item(key = "submit_error") { InlineBanner(message = state.submitError, tone = StatusTone.Danger) }
                    }
                    item(key = "roster") {
                        SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs)) {
                            Column {
                                students.forEachIndexed { index, student ->
                                    if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                                    StudentRow(index + 1, student, enabled = !state.submitting, onMark = { onMark(student.studentId, it) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Summary(students: List<StudentAttendanceInputItem>, onMarkAllPresent: () -> Unit, enabled: Boolean) {
    val tally = AttendanceMarks.tally(students)
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.tatt_students, students.size), style = SistaTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.tatt_hint),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                SistaButton(
                    stringResource(R.string.tatt_all_present),
                    onMarkAllPresent,
                    variant = ButtonVariant.Secondary,
                    leadingIcon = Icons.Outlined.DoneAll,
                    enabled = enabled,
                )
            }
            Row {
                Mark.entries.forEach { mark ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${tally[mark.code] ?: 0}",
                            style = SistaTheme.typography.titleLarge,
                            color = mark.tone.colors().content,
                        )
                        Text(
                            stringResource(mark.label),
                            style = SistaTheme.typography.labelMedium,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentRow(number: Int, student: StudentAttendanceInputItem, enabled: Boolean, onMark: (String) -> Unit) {
    Column(Modifier.padding(vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$number",
                style = SistaTheme.typography.labelMedium,
                color = SistaTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(24.dp),
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(student.name, style = SistaTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    studentNumber(student.nis, student.nisn),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
        Row(
            Modifier
                .padding(start = 24.dp + Spacing.md)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Mark.entries.forEach { mark ->
                MarkOption(mark, selected = student.status == mark.code, enabled = enabled, onClick = { onMark(mark.code) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MarkOption(mark: Mark, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val tone = mark.tone.colors()
    Text(
        stringResource(mark.label),
        style = SistaTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) tone.onContainer else SistaTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(if (selected) tone.container else SistaTheme.colors.surfaceVariant, SistaTheme.shapes.small)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = Spacing.sm),
    )
}
