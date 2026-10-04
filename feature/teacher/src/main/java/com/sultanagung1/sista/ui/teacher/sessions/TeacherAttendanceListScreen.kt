package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.ui.component.Avatar
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.sultanagung1.sista.feature.teacher.R
import com.sultanagung1.sista.core.designsystem.ClassSessionText

/**
 * FASE 77.4: "Daftar Hadir" — who scanned, who did not, and manual marks.
 * Marks are kept on the phone until "Simpan" sends them in one request; the
 * list is read-only once the session has ended (corrections go through
 * Waka Kurikulum/TU).
 */
@Composable
fun TeacherAttendanceListScreen(
    viewModel: TeacherAttendanceListViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherAttendanceListEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherAttendanceListEvent.ScreenStopped) },
    )
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TeacherAttendanceListEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    TeacherAttendanceListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

/** The class list without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherAttendanceListContent(
    state: TeacherAttendanceListState,
    onEvent: (TeacherAttendanceListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val session = state.session
    val pending = state.pendingChanges
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(R.string.tl_title),
                    subtitle = session?.let {
                        listOfNotNull(it.subjectName, it.classroomName, it.jamKe?.let { j -> stringResource(R.string.ts_period, j) }).joinToString(" · ")
                    },
                    onBack = onNavigateBack,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { if (state.rows.isNotEmpty()) SummaryBar(state, pending.size, onEvent) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("attendance_list"),
                contentPadding = PaddingValues(top = Spacing.sm, bottom = Spacing.xl),
            ) {
                when {
                    state.isLoading && state.rows.isEmpty() -> item { SkeletonList(Modifier.padding(horizontal = Spacing.screen)) }
                    state.notDeployed -> item {
                        SessionsUnavailable((state.errorMessage ?: ClassSessionText.notDeployed).asString(), onRetry = { onEvent(TeacherAttendanceListEvent.Refresh) })
                    }
                    state.rows.isEmpty() && state.errorMessage != null -> item {
                        ErrorState(
                            title = stringResource(R.string.tl_load_error),
                            body = state.errorMessage.asString(),
                            onRetry = { onEvent(TeacherAttendanceListEvent.Refresh) },
                        )
                    }
                    else -> {
                        if (session != null && !state.isEditable) {
                            item {
                                InlineBanner(
                                    message = stringResource(R.string.tl_ended),
                                    tone = StatusTone.Info,
                                    modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs),
                                )
                            }
                        }
                        item {
                            SistaTextField(
                                value = state.query,
                                onValueChange = { onEvent(TeacherAttendanceListEvent.QueryChanged(it)) },
                                label = stringResource(R.string.tl_search),
                                leadingIcon = Icons.Outlined.Search,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                        item {
                            val all = stringResource(R.string.tl_all)
                            val labels = SessionAttendanceStatus.entries.associateWith { stringResource(ClassSessionText.label(it)) }
                            FilterChipRow(
                                options = listOf<SessionAttendanceStatus?>(null) + SessionAttendanceStatus.entries,
                                selected = state.filter,
                                onSelect = { onEvent(TeacherAttendanceListEvent.FilterChanged(it)) },
                                label = { it?.let(labels::getValue) ?: all },
                                modifier = Modifier.padding(vertical = Spacing.sm),
                            )
                        }
                        val visible = state.visibleRows
                        if (visible.isEmpty()) {
                            item {
                                EmptyState(
                                    title = stringResource(R.string.tl_empty),
                                    body = stringResource(if (state.rows.isEmpty()) R.string.tl_empty_none else R.string.tl_empty_filter),
                                    icon = Icons.Outlined.Groups,
                                )
                            }
                        }
                        itemsIndexed(visible, key = { _, row -> row.studentId }) { index, row ->
                            if (index > 0) HorizontalDivider(Modifier.padding(start = 72.dp), color = SistaTheme.colors.outlineVariant)
                            StudentRow(
                                row = row,
                                status = state.statusOf(row),
                                isEdited = pending.containsKey(row.studentId),
                                editable = state.isEditable && !state.isSaving,
                                onMark = { onEvent(TeacherAttendanceListEvent.Mark(row.studentId, it)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBar(state: TeacherAttendanceListState, pendingCount: Int, onEvent: (TeacherAttendanceListEvent) -> Unit) {
    Surface(color = SistaTheme.colors.surfaceContainer) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screen, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            AttendanceSummary(state.previewCounts)
            if (pendingCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SistaButton(stringResource(R.string.tl_discard), { onEvent(TeacherAttendanceListEvent.DiscardChanges) }, variant = ButtonVariant.Text)
                    Spacer(Modifier.width(Spacing.sm))
                    SistaButton(
                        stringResource(R.string.tl_save, pendingCount),
                        { onEvent(TeacherAttendanceListEvent.Save) },
                        loading = state.isSaving,
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_attendance_button"),
                        fullWidth = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentRow(
    row: SessionAttendanceDto,
    status: SessionAttendanceStatus,
    isEdited: Boolean,
    editable: Boolean,
    onMark: (SessionAttendanceStatus) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val name = row.studentName ?: stringResource(R.string.tl_student_fallback)
    val statusLabel = stringResource(ClassSessionText.label(status))
    val changeStatus = stringResource(R.string.tl_change_status_cd, name, statusLabel)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name)
        Spacer(Modifier.width(Spacing.lg))
        Column(Modifier.weight(1f)) {
            Text(name, style = SistaTheme.typography.bodyLarge, color = SistaTheme.colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(
                    row.studentNis?.let { stringResource(R.string.tl_nis, it) },
                    ClassSessionText.checkIn(row).asString(),
                    if (isEdited) stringResource(R.string.tl_unsaved) else null,
                ).joinToString(" · "),
                style = SistaTheme.typography.bodySmall,
                color = if (isEdited) StatusTone.Warning.colors().content else SistaTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box {
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .then(
                        if (editable) {
                            Modifier
                                .clickable(role = Role.DropdownList) { menuOpen = true }
                                .semantics { contentDescription = changeStatus }
                        } else {
                            Modifier
                        },
                    )
                    .padding(start = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AttendanceStatusPill(status)
                if (editable) Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, tint = SistaTheme.colors.onSurfaceVariant)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                ClassSessionRules.TEACHER_MARK_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = { AttendanceStatusPill(option) },
                        onClick = {
                            menuOpen = false
                            onMark(option)
                        },
                    )
                }
            }
        }
    }
}
