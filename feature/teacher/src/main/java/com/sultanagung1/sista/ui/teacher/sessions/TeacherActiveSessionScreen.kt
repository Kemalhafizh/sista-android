package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.KeepScreenAwake
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaoneQrCode
import com.sultanagung1.sista.core.ui.component.ButtonSize
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.IconBadge
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
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness
import com.sultanagung1.sista.data.model.ClassSessionRules.TimerTone
import com.sultanagung1.sista.data.model.ClassSessionStatus
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.sultanagung1.sista.feature.teacher.R
import com.sultanagung1.sista.core.designsystem.ClassSessionText

/**
 * FASE 77.3: the running class — rotating QR from the server, session timer,
 * live attendance and "Akhiri kelas". A finished session opens here too, as
 * its summary.
 */
@Composable
fun TeacherActiveSessionScreen(
    viewModel: TeacherActiveSessionViewModel,
    onOpenAttendanceList: (sessionId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenTeachingJournal: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherActiveSessionEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherActiveSessionEvent.ScreenStopped) },
    )
    // 77.3.5: the QR must stay on screen, bright, for the whole class.
    KeepScreenAwake(enabled = state.isLive, maxBrightness = true)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TeacherActiveSessionEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    TeacherActiveSessionContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.onEvent(TeacherActiveSessionEvent.Retry) },
        onOpenAttendanceList = { onOpenAttendanceList(state.sessionId) },
        onEndConfirmed = { notes, topic -> viewModel.onEvent(TeacherActiveSessionEvent.EndSessionConfirmed(notes, topic)) },
        onDismissTimeUp = { viewModel.onEvent(TeacherActiveSessionEvent.DismissTimeUp) },
        onContinueAfterTimeUp = { viewModel.onEvent(TeacherActiveSessionEvent.ContinueAfterTimeUp) },
        onOpenTeachingJournal = onOpenTeachingJournal,
        onNavigateBack = onNavigateBack,
    )
}

/** The session screen without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherActiveSessionContent(
    state: TeacherActiveSessionState,
    onRetry: () -> Unit,
    onOpenAttendanceList: () -> Unit,
    onEndConfirmed: (notes: String?, topic: String?) -> Unit,
    onDismissTimeUp: () -> Unit,
    onContinueAfterTimeUp: () -> Unit,
    onOpenTeachingJournal: () -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    qrContent: @Composable (payload: String, dimmed: Boolean) -> Unit = { payload, dimmed ->
        SulaoneQrCode(
            payload = payload,
            contentDescription = stringResource(R.string.ta_qr_cd),
            dimmed = dimmed,
            modifier = Modifier.fillMaxWidth(),
        )
    },
) {
    var showEndDialog by remember { mutableStateOf(false) }
    val session = state.session

    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(if (state.status?.isFinished == true) R.string.ta_title_summary else R.string.ta_title_active),
                    subtitle = session?.let { listOfNotNull(it.subjectName, it.classroomName).joinToString(" · ") },
                    onBack = onNavigateBack,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                when {
                    state.isLoading && session == null -> SkeletonList(rows = 3)
                    state.notDeployed -> SessionsUnavailable((state.errorMessage ?: ClassSessionText.notDeployed).asString(), onRetry)
                    session == null -> ErrorState(title = stringResource(R.string.ta_not_found), body = state.errorMessage?.asString(), onRetry = onRetry)
                    state.isLive -> LiveSession(state, session, qrContent, onOpenAttendanceList) { showEndDialog = true }
                    else -> FinishedSession(state, session, onOpenAttendanceList, onOpenTeachingJournal, onNavigateBack)
                }
            }
        }

        if (state.showTimeUpDialog && !showEndDialog) {
            AlertDialog(
                onDismissRequest = onDismissTimeUp,
                title = { Text(stringResource(R.string.ta_time_up_title)) },
                text = {
                    val autoCloseAt = ClassSessionRules.clockOf(session?.autoCloseAt)
                    Text(autoCloseAt?.let { stringResource(R.string.ta_time_up_body_at, it) } ?: stringResource(R.string.ta_time_up_body))
                },
                confirmButton = {
                    SistaButton(stringResource(R.string.ta_end), {
                        onDismissTimeUp()
                        showEndDialog = true
                    }, variant = ButtonVariant.Text)
                },
                dismissButton = { SistaButton(stringResource(R.string.ta_continue), onContinueAfterTimeUp, variant = ButtonVariant.Text) },
            )
        }

        if (showEndDialog && session != null) {
            EndSessionDialog(
                session = session,
                alphaCount = state.counts.alpha,
                onDismiss = { showEndDialog = false },
                onConfirm = { notes, topic ->
                    showEndDialog = false
                    onEndConfirmed(notes, topic)
                },
            )
        }
    }
}

@Composable
private fun LiveSession(
    state: TeacherActiveSessionState,
    session: ClassSessionDto,
    qrContent: @Composable (String, Boolean) -> Unit,
    onOpenAttendanceList: () -> Unit,
    onEnd: () -> Unit,
) {
    QrPanel(state, qrContent)
    TimerCard(state, session)
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(stringResource(R.string.ta_attendance_now), style = SistaTheme.typography.titleMedium)
            AttendanceSummary(state.counts)
            SistaButton(
                stringResource(R.string.ta_view_mark),
                onOpenAttendanceList,
                variant = ButtonVariant.Outlined,
                leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                fullWidth = true,
            )
        }
    }
    SistaButton(
        stringResource(R.string.ta_end),
        onEnd,
        variant = ButtonVariant.Danger,
        size = ButtonSize.Large,
        leadingIcon = Icons.Outlined.Stop,
        loading = state.isEnding,
        enabled = !state.isEnding,
        fullWidth = true,
        modifier = Modifier.testTag("end_session_button"),
    )
}

@Composable
private fun QrPanel(state: TeacherActiveSessionState, qrContent: @Composable (String, Boolean) -> Unit) {
    val freshness = state.qrFreshness
    val payload = state.qrToken
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth()
                    .height(if (payload != null && freshness != QrFreshness.UNAVAILABLE) 300.dp else 220.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    payload != null && freshness != QrFreshness.UNAVAILABLE -> qrContent(payload, freshness == QrFreshness.STALE)
                    freshness == QrFreshness.UNAVAILABLE -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        IconBadge(Icons.Outlined.WifiOff, tone = StatusTone.Danger, size = 56.dp)
                        Text(
                            stringResource(R.string.ta_qr_offline),
                            textAlign = TextAlign.Center,
                            style = SistaTheme.typography.bodyMedium,
                        )
                    }
                    else -> CircularProgressIndicator()
                }
            }
            Text(
                when (freshness) {
                    QrFreshness.FRESH -> stringResource(R.string.ta_qr_fresh, state.qrSecondsLeft)
                    QrFreshness.STALE -> stringResource(R.string.ta_qr_stale)
                    QrFreshness.UNAVAILABLE -> stringResource(R.string.ta_qr_unavailable)
                    QrFreshness.LOADING -> stringResource(R.string.ta_qr_loading)
                },
                style = SistaTheme.typography.labelLarge,
                color = if (freshness == QrFreshness.FRESH) SistaTheme.colors.onSurfaceVariant else StatusTone.Warning.colors().content,
            )
            if (freshness == QrFreshness.FRESH) {
                LinearProgressIndicator(
                    progress = { state.qrSecondsLeft / state.qrRotationSeconds.coerceAtLeast(1).toFloat() },
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    drawStopIndicator = {},
                )
            }
            Text(
                stringResource(R.string.ta_qr_rotation, state.qrRotationSeconds),
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TimerCard(state: TeacherActiveSessionState, session: ClassSessionDto) {
    val remaining = state.remainingSeconds
    val tone = when (state.timerTone) {
        TimerTone.NORMAL -> StatusTone.Brand
        TimerTone.WARNING -> StatusTone.Warning
        TimerTone.CRITICAL, TimerTone.OVERTIME -> StatusTone.Danger
    }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Timer, tone = tone)
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(
                    when {
                        remaining == null -> stringResource(R.string.ta_remaining_none)
                        state.timerTone == TimerTone.OVERTIME -> stringResource(R.string.ta_overtime, ClassSessionRules.formatCountdown(-remaining))
                        else -> stringResource(R.string.ta_remaining, ClassSessionRules.formatCountdown(remaining))
                    },
                    style = SistaTheme.typography.titleMedium,
                    color = if (state.timerTone == TimerTone.NORMAL) SistaTheme.colors.onSurface else tone.colors().content,
                    // Read out only when the tone changes, not every second.
                    modifier = Modifier.semantics { if (state.timerTone != TimerTone.NORMAL) liveRegion = LiveRegionMode.Polite },
                )
                Text(
                    listOfNotNull(
                        ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd),
                        session.jamKe?.let { stringResource(R.string.ts_period, it) },
                    ).joinToString(" · "),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FinishedSession(
    state: TeacherActiveSessionState,
    session: ClassSessionDto,
    onOpenAttendanceList: () -> Unit,
    onOpenTeachingJournal: () -> Unit,
    onBack: () -> Unit,
) {
    val status = session.effectiveStatus
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SessionStatusPill(status)
            Text(session.subjectName ?: stringResource(R.string.ta_session_fallback), style = SistaTheme.typography.titleLarge)
            Text(
                listOfNotNull(
                    stringResource(R.string.ta_scheduled, ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd)),
                    session.actualStart?.let { stringResource(R.string.ts_held, ClassSessionRules.timeRange(it, session.actualEnd)) },
                ).joinToString(" · "),
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
            )
            val counts = if (state.counts.total > 0) state.counts else ClassSessionRules.countsOf(session)
            AttendanceSummary(counts)
            session.topic?.takeIf { it.isNotBlank() }?.let { Text(stringResource(R.string.ta_topic, it), style = SistaTheme.typography.bodyMedium) }
            session.notes?.takeIf { it.isNotBlank() }?.let { Text(stringResource(R.string.ta_notes, it), style = SistaTheme.typography.bodyMedium) }
        }
    }
    if (status == ClassSessionStatus.AUTO_CLOSED) {
        InlineBanner(
            message = stringResource(R.string.ta_auto_closed),
            tone = StatusTone.Warning,
        )
    }
    // The server creates a teaching-journal draft when a session ends with a topic.
    if (session.teachingJournalId != null) {
        InlineBanner(
            title = stringResource(R.string.ta_journal_draft),
            message = stringResource(R.string.ta_journal_draft_body),
            tone = StatusTone.Info,
            actionLabel = stringResource(R.string.ta_open_journal),
            onAction = onOpenTeachingJournal,
        )
    }
    Text(
        stringResource(R.string.ta_after_end),
        style = SistaTheme.typography.bodySmall,
        color = SistaTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    SistaButton(stringResource(R.string.ta_view_list), onOpenAttendanceList, variant = ButtonVariant.Outlined, fullWidth = true)
    SistaButton(stringResource(R.string.ta_back_to_list), onBack, fullWidth = true)
}

@Composable
private fun EndSessionDialog(
    session: ClassSessionDto,
    alphaCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (notes: String?, topic: String?) -> Unit,
) {
    var notes by remember { mutableStateOf(session.notes.orEmpty()) }
    var topic by remember { mutableStateOf(session.topic.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
        title = { Text(stringResource(R.string.ta_end_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    if (alphaCount > 0) {
                        stringResource(R.string.ta_end_body_alpha, session.subjectName.orEmpty(), session.classroomName.orEmpty(), alphaCount)
                    } else {
                        stringResource(R.string.ta_end_body_all, session.subjectName.orEmpty(), session.classroomName.orEmpty())
                    },
                    style = SistaTheme.typography.bodyMedium,
                )
                SistaTextField(
                    value = topic,
                    onValueChange = { topic = it.take(ClassSessionRules.TOPIC_MAX_CHARS) },
                    label = stringResource(R.string.ts_topic_optional),
                    helperText = stringResource(R.string.ta_topic_helper),
                )
                SistaTextField(
                    value = notes,
                    onValueChange = { notes = it.take(ClassSessionRules.END_NOTES_MAX_CHARS) },
                    label = stringResource(R.string.ta_notes_optional),
                    singleLine = false,
                )
            }
        },
        confirmButton = { SistaButton(stringResource(R.string.ta_end), { onConfirm(notes.ifBlank { null }, topic.ifBlank { null }) }, variant = ButtonVariant.Danger) },
        dismissButton = { SistaButton(stringResource(R.string.ts_cancel), onDismiss, variant = ButtonVariant.Text) },
    )
}
