package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.CardVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.StartAction
import com.sultanagung1.sista.data.model.ClassSessionStatus
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.feature.teacher.R
import com.sultanagung1.sista.core.ui.R as CoreUiR
import com.sultanagung1.sista.core.designsystem.ClassSessionText

/**
 * FASE 77.2: "Sesi Kelas Hari Ini" — the teacher's teaching slots of today
 * from `teacher/class-sessions/today`, each with what can be done now:
 * start (inside the server's window), open the running class, or review.
 */
@Composable
fun TeacherTodaySessionsScreen(
    viewModel: TeacherTodaySessionsViewModel,
    onOpenActiveSession: (sessionId: Long) -> Unit,
    onOpenWeeklySchedule: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherTodaySessionsEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherTodaySessionsEvent.ScreenStopped) },
    )
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TeacherTodaySessionsEffect.OpenActiveSession -> onOpenActiveSession(effect.sessionId)
                is TeacherTodaySessionsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    TeacherTodaySessionsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.onEvent(TeacherTodaySessionsEvent.Refresh) },
        onStart = { session, topic -> viewModel.onEvent(TeacherTodaySessionsEvent.StartSession(session.scheduleId, topic)) },
        onOpen = { id -> viewModel.onEvent(TeacherTodaySessionsEvent.OpenSession(id)) },
        onOpenWeeklySchedule = onOpenWeeklySchedule,
        onNavigateBack = onNavigateBack,
    )
}

/** Today's sessions without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherTodaySessionsContent(
    state: TeacherTodaySessionsState,
    onRefresh: () -> Unit,
    onStart: (ClassSessionDto, String?) -> Unit,
    onOpen: (Long) -> Unit,
    onOpenWeeklySchedule: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var startTarget by remember { mutableStateOf<ClassSessionDto?>(null) }

    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SistaTopBar(
                    title = stringResource(R.string.ts_title),
                    subtitle = state.today?.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", displayLocale(LocalConfiguration.current.locales[0]))),
                    onBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("teacher_sessions_list"),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    when {
                        state.isLoading && state.sessions.isEmpty() -> item(key = "loading") { SkeletonList(rows = 3) }
                        state.notDeployed -> item(key = "unavailable") {
                            SessionsUnavailable((state.errorMessage ?: ClassSessionText.notDeployed).asString(), onRetry = onRefresh)
                        }
                        state.sessions.isEmpty() && state.errorMessage != null -> item(key = "error") {
                            ErrorState(title = stringResource(R.string.ts_load_error), body = state.errorMessage.asString(), onRetry = onRefresh)
                        }
                        state.sessions.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = stringResource(R.string.ts_no_lessons),
                                body = stringResource(R.string.ts_no_lessons_body),
                                icon = Icons.Outlined.EventBusy,
                                actionLabel = stringResource(R.string.ts_week_schedule),
                                onAction = onOpenWeeklySchedule,
                            )
                        }
                        else -> {
                            // A failed background poll keeps the list; say so above it.
                            state.errorMessage?.let { message ->
                                item(key = "stale") {
                                    InlineBanner(
                                        message = stringResource(R.string.ts_refresh_failed, message.asString()),
                                        tone = StatusTone.Warning,
                                        actionLabel = stringResource(CoreUiR.string.core_reload),
                                        onAction = onRefresh,
                                    )
                                }
                            }
                            items(state.sessions, key = { "${it.scheduleId}-${it.sessionId}" }) { session ->
                                SessionCard(
                                    session = session,
                                    startAction = ClassSessionRules.startActionFor(session, state.nowMinutes),
                                    isStarting = state.startingScheduleId == session.scheduleId,
                                    onStart = { startTarget = session },
                                    onOpen = onOpen,
                                )
                            }
                        }
                    }
                }
            }
        }

        startTarget?.let { target ->
            StartSessionDialog(
                session = target,
                onDismiss = { startTarget = null },
                onConfirm = { topic ->
                    startTarget = null
                    onStart(target, topic)
                },
            )
        }
    }
}

@Composable
private fun SessionCard(
    session: ClassSessionDto,
    startAction: StartAction,
    isStarting: Boolean,
    onStart: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val status = session.effectiveStatus
    val sessionId = session.sessionId
    SistaCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (status == ClassSessionStatus.ACTIVE) CardVariant.Highlighted else CardVariant.Filled,
        onClick = if (sessionId != null) ({ onOpen(sessionId) }) else null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SessionStatusPill(status)
                Spacer(Modifier.weight(1f))
                session.jamKe?.let {
                    Text(stringResource(R.string.ts_period, it), style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
                }
            }
            Column {
                Text(session.subjectName ?: stringResource(R.string.ts_subject_fallback), style = SistaTheme.typography.titleLarge)
                Text(
                    listOfNotNull(session.classroomName?.let { stringResource(R.string.ts_class, it) }, ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd))
                        .joinToString(" · "),
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
                if (status.isFinished && session.actualStart != null) {
                    Text(
                        stringResource(R.string.ts_held, ClassSessionRules.timeRange(session.actualStart, session.actualEnd)),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            }
            if (status == ClassSessionStatus.ACTIVE || status.isFinished) {
                AttendanceSummary(ClassSessionRules.countsOf(session), showBreakdown = status.isFinished)
            }
            when {
                status == ClassSessionStatus.ACTIVE && sessionId != null -> SistaButton(
                    stringResource(R.string.ts_open_session),
                    { onOpen(sessionId) },
                    leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                    fullWidth = true,
                )
                status.isFinished && sessionId != null -> SistaButton(
                    stringResource(R.string.ts_view_detail),
                    { onOpen(sessionId) },
                    variant = ButtonVariant.Outlined,
                    fullWidth = true,
                )
                else -> when (startAction) {
                    StartAction.Available -> SistaButton(
                        stringResource(R.string.ts_start),
                        onStart,
                        leadingIcon = Icons.Outlined.PlayArrow,
                        loading = isStarting,
                        enabled = !isStarting,
                        fullWidth = true,
                        modifier = Modifier.testTag("start_session_${session.scheduleId}"),
                    )
                    is StartAction.NotYet -> {
                        SistaButton(stringResource(R.string.ts_start), {}, enabled = false, fullWidth = true)
                        Text(
                            stringResource(R.string.ts_opens_at, startAction.opensAt),
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                    StartAction.Missed -> InlineBanner(
                        message = stringResource(R.string.ts_missed),
                        tone = StatusTone.Warning,
                    )
                    StartAction.Hidden -> Unit
                }
            }
        }
    }
}

@Composable
private fun StartSessionDialog(session: ClassSessionDto, onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var topic by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ts_start_dialog_title, session.subjectName ?: stringResource(R.string.ts_class_fallback))) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    stringResource(
                        R.string.ts_start_dialog_body,
                        session.classroomName.orEmpty(),
                        ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd),
                    ),
                    style = SistaTheme.typography.bodyMedium,
                )
                SistaTextField(
                    value = topic,
                    onValueChange = { topic = it.take(ClassSessionRules.TOPIC_MAX_CHARS) },
                    label = stringResource(R.string.ts_topic_optional),
                    helperText = stringResource(R.string.ts_topic_count, topic.length, ClassSessionRules.TOPIC_MAX_CHARS),
                )
            }
        },
        confirmButton = { SistaButton(stringResource(R.string.ts_start_confirm), { onConfirm(topic.ifBlank { null }) }, variant = ButtonVariant.Text) },
        dismissButton = { SistaButton(stringResource(R.string.ts_cancel), onDismiss, variant = ButtonVariant.Text) },
    )
}
