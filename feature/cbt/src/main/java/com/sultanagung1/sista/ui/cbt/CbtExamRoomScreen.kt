package com.sultanagung1.sista.ui.cbt

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sultanagung1.sista.core.security.CbtAntiCheatEngine
import com.sultanagung1.sista.core.security.CbtEncryptedVault
import com.sultanagung1.sista.core.security.CbtLockTaskManager
import com.sultanagung1.sista.core.security.ExamViolationRecord
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.CbtQuestionItem
import com.sultanagung1.sista.feature.cbt.R
import com.sultanagung1.sista.ui.cbt.components.CbtImageViewer
import com.sultanagung1.sista.ui.cbt.components.CbtLatexMathView

/**
 * The exam itself. The clock comes from the server (token answer, then the
 * questions' own `remaining_seconds`) and the answers are sent when it runs
 * out. Leaving the app closes the attempt (FASE 26); reaching the exam's
 * violation limit locks it, and only the server's record counts: nothing on
 * the device can unlock it.
 */
@Composable
fun CbtExamRoomScreen(
    examId: Long,
    studentId: Long?,
    maxViolations: Int?,
    initialRemainingSeconds: Long?,
    viewModel: CbtViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val antiCheatEngine = remember(maxViolations) { CbtAntiCheatEngine(context, maxViolations) }
    val antiCheatState by antiCheatEngine.antiCheatState.collectAsState()
    val lockTaskManager = remember { CbtLockTaskManager(context) }
    val vault = remember { CbtEncryptedVault(context) }

    val uiState by viewModel.uiState.collectAsState()
    // The lifecycle observer below outlives recompositions; it must read the latest state.
    val latestState by rememberUpdatedState(uiState)
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitWarningDialog by remember { mutableStateOf(false) }
    val hasExitedRef = remember { mutableStateOf(false) }

    val releaseDevice = {
        if (activity != null) {
            antiCheatEngine.deactivateExamSecurity(activity)
            lockTaskManager.stopKioskMode(activity)
        }
    }

    LaunchedEffect(examId) {
        if (vault.hasEncryptedPayload(examId)) {
            viewModel.unlockFromVaultWithKey(examId, vault)
        } else {
            viewModel.loadQuestions(examId)
        }
        viewModel.startExamTimerIfUnknown(examId, initialRemainingSeconds)
    }

    // Release kiosk mode once the exam is over, however it ended.
    LaunchedEffect(uiState.isOver) {
        if (uiState.isOver) {
            releaseDevice()
            vault.clearVault(examId)
        }
    }
    LaunchedEffect(uiState.isBlockedByServer) {
        if (uiState.isBlockedByServer) antiCheatEngine.lockByServer()
    }

    // Hardware-level window protection (FLAG_SECURE), kiosk lock task and anti-cheat listeners.
    DisposableEffect(lifecycleOwner) {
        if (activity != null) {
            antiCheatEngine.activateExamSecurity(activity)
            lockTaskManager.startKioskMode(activity)
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // FASE 26: leaving, minimising or splitting the screen closes the exam.
                    if (!hasExitedRef.value && !latestState.isOver) {
                        hasExitedRef.value = true
                        val reason = if (activity?.isInMultiWindowMode == true) "split_screen" else "app_minimized"
                        viewModel.forceCloseExam(examId, reason, vault)
                        releaseDevice()
                        onNavigateBack()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (!hasExitedRef.value && !latestState.isOver) {
                        hasExitedRef.value = true
                        viewModel.forceCloseExam(examId, "app_closed", vault)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (activity != null) antiCheatEngine.verifyMultiWindowMode(activity)
                    if (latestState.isForceClosedBySystem) onNavigateBack()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            releaseDevice()
        }
    }

    // FASE 76.3: leaving is an explicit, confirmed action, never a side effect of
    // a single back gesture (an edge swipe on gesture navigation is easy to make).
    val exitAndForceClose: (String) -> Unit = { reason ->
        if (!hasExitedRef.value && !uiState.isOver) {
            hasExitedRef.value = true
            viewModel.forceCloseExam(examId, reason, vault)
            releaseDevice()
        }
        onNavigateBack()
    }
    BackHandler(enabled = true) {
        if (!hasExitedRef.value && !uiState.isOver) showExitWarningDialog = true else onNavigateBack()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.pauseExamTimer() }
    }

    // FASE 72.2: live proctoring and the liveness heartbeat; never pauses the clock.
    LaunchedEffect(examId, studentId) {
        viewModel.startLiveProctoring(examId, studentId)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopLiveProctoring() }
    }

    // Mirror each new violation to the backend the moment it's recorded, so the
    // proctor sees it live and the server can block the attempt at its limit.
    LaunchedEffect(antiCheatState.violationCount) {
        antiCheatState.violationHistory.lastOrNull()?.let { record ->
            viewModel.recordViolation(examId, record.type.name)
        }
    }

    val locked = antiCheatState.isExamLocked || uiState.isBlockedByServer
    Box(Modifier.fillMaxSize()) {
        CbtExamRoomContent(
            title = uiState.examMeta?.title,
            questions = uiState.currentExamQuestions,
            currentIndex = uiState.currentQuestionIndex,
            answers = uiState.selectedAnswers,
            remainingSeconds = uiState.remainingSeconds,
            violationCount = antiCheatState.violationCount,
            maxViolations = antiCheatState.maxViolationsAllowed,
            batteryLevel = antiCheatState.batteryLevel,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onRetry = { viewModel.loadQuestions(examId) },
            onSelect = { question, key -> viewModel.selectOption(question.id, key, examId) },
            onGoTo = viewModel::goToQuestion,
            onSubmit = { showSubmitDialog = true },
            onExit = { showExitWarningDialog = true },
        )
        if (locked) {
            ExamLockedPanel(
                maxViolations = antiCheatState.maxViolationsAllowed,
                history = antiCheatState.violationHistory,
                onLeave = {
                    hasExitedRef.value = true
                    releaseDevice()
                    onNavigateBack()
                },
            )
        }
    }

    val questions = uiState.currentExamQuestions
    val answered = CbtExamFormat.answeredCount(questions.map { it.id }, uiState.selectedAnswers)

    antiCheatState.pendingWarning?.takeIf { !locked }?.let { warning ->
        ViolationWarningDialog(warning, antiCheatState.maxViolationsAllowed, onDismiss = antiCheatEngine::dismissWarningDialog)
    }

    if (showExitWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExitWarningDialog = false },
            title = { Text(stringResource(R.string.cbt_exit_title)) },
            text = { Text(stringResource(R.string.cbt_exit_body, answered, questions.size)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitWarningDialog = false
                    exitAndForceClose("back_button_pressed")
                }) { Text(stringResource(R.string.cbt_exit_confirm), color = SistaTheme.colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitWarningDialog = false }) { Text(stringResource(R.string.cbt_exit_stay)) }
            },
        )
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(stringResource(R.string.cbt_submit_title)) },
            text = {
                Text(
                    if (answered < questions.size) {
                        stringResource(R.string.cbt_submit_body_missing, answered, questions.size, questions.size - answered)
                    } else {
                        stringResource(R.string.cbt_submit_body, answered, questions.size)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam(examId, vault)
                    },
                    modifier = Modifier.testTag("cbt_confirm_submit_button"),
                ) { Text(stringResource(R.string.cbt_submit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text(stringResource(R.string.cbt_submit_review)) }
            },
        )
    }

    if (uiState.isSubmitted) {
        AlertDialog(
            onDismissRequest = {},
            modifier = Modifier.testTag(
                if (uiState.isQueuedOffline) "cbt_submitted_offline_dialog" else "cbt_submitted_online_dialog"
            ),
            title = {
                Text(
                    stringResource(
                        when {
                            uiState.isQueuedOffline -> R.string.cbt_done_offline_title
                            uiState.isTimeUp -> R.string.cbt_done_time_title
                            else -> R.string.cbt_done_title
                        }
                    )
                )
            },
            text = {
                Text(stringResource(if (uiState.isQueuedOffline) R.string.cbt_done_offline_body else R.string.cbt_done_body))
            },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.cbt_done_close)) }
            },
        )
    }

    uiState.proctorIntervention?.let { cmd ->
        val forced = cmd.action == "FORCE_SUBMIT"
        AlertDialog(
            onDismissRequest = { if (!forced) viewModel.dismissProctorIntervention() },
            icon = {
                IconBadge(
                    if (cmd.action == "EXTEND_TIME") Icons.Outlined.Timer else Icons.Outlined.Lock,
                    tone = if (cmd.action == "EXTEND_TIME") StatusTone.Success else StatusTone.Danger,
                )
            },
            title = {
                Text(
                    stringResource(
                        when (cmd.action) {
                            "FORCE_SUBMIT" -> R.string.cbt_proctor_stop
                            "EXTEND_TIME" -> R.string.cbt_proctor_extend
                            else -> R.string.cbt_proctor_warning
                        }
                    )
                )
            },
            // The proctor's own words, as they wrote them.
            text = { Text(cmd.message ?: stringResource(R.string.cbt_proctor_default)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissProctorIntervention()
                    if (forced) viewModel.submitExam(examId, vault)
                }) { Text(stringResource(if (forced) R.string.cbt_proctor_submit else R.string.cbt_proctor_ok)) }
            },
        )
    }
}

/** The exam room without a ViewModel or device side effects, for previews and screenshots. */
@Composable
fun CbtExamRoomContent(
    title: String?,
    questions: List<CbtQuestionItem>,
    currentIndex: Int,
    answers: Map<String, String>,
    remainingSeconds: Long?,
    violationCount: Int,
    maxViolations: Int?,
    batteryLevel: Int?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onSelect: (CbtQuestionItem, String) -> Unit,
    onGoTo: (Int) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit,
) {
    val question = questions.getOrNull(currentIndex) ?: questions.firstOrNull()
    val index = if (question == null) 0 else questions.indexOf(question)
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = title ?: stringResource(R.string.cbt_room_title),
                    subtitle = if (questions.isEmpty()) null else stringResource(R.string.cbt_question_of, index + 1, questions.size),
                    onBack = onExit,
                    actions = {
                        StatusPill(
                            CbtExamFormat.clock(remainingSeconds),
                            CbtExamFormat.clockTone(remainingSeconds),
                            icon = Icons.Outlined.Timer,
                            modifier = Modifier.padding(end = Spacing.sm),
                        )
                    },
                )
            },
            bottomBar = {
                if (question != null) {
                    NavigationBar(
                        isFirst = index == 0,
                        isLast = index == questions.lastIndex,
                        onPrevious = { onGoTo(index - 1) },
                        onNext = { onGoTo(index + 1) },
                        onSubmit = onSubmit,
                    )
                }
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when {
                    question == null && isLoading -> Box(Modifier.padding(Spacing.screen)) { SkeletonList(rows = 4) }
                    question == null -> ErrorState(
                        title = stringResource(R.string.cbt_questions_error),
                        body = errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.padding(Spacing.screen),
                    )
                    else -> {
                        IntegrityRow(violationCount, maxViolations, batteryLevel)
                        QuestionStrip(questions, index, answers, onGoTo)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            errorMessage?.let { InlineBanner(message = it, tone = StatusTone.Danger) }
                            QuestionBody(question, answers[question.id.toString()], onSelect)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrityRow(violationCount: Int, maxViolations: Int?, batteryLevel: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            violationCount == 0 -> StatusPill(stringResource(R.string.cbt_integrity_ok), StatusTone.Success)
            maxViolations != null -> StatusPill(stringResource(R.string.cbt_violations_of, violationCount, maxViolations), StatusTone.Warning)
            else -> StatusPill(stringResource(R.string.cbt_violations, violationCount), StatusTone.Warning)
        }
        batteryLevel?.let {
            StatusPill(stringResource(R.string.cbt_battery, it), if (it <= 15) StatusTone.Danger else StatusTone.Neutral)
        }
    }
}

/** One chip per question: filled when answered, outlined for the current one. */
@Composable
private fun QuestionStrip(questions: List<CbtQuestionItem>, current: Int, answers: Map<String, String>, onGoTo: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        itemsIndexed(questions, key = { _, q -> q.id }) { i, q ->
            val answered = !answers[q.id.toString()].isNullOrBlank()
            val isCurrent = i == current
            val colors = SistaTheme.colors
            val label = stringResource(
                if (answered) R.string.cbt_chip_answered else R.string.cbt_chip_open,
                i + 1,
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (answered) colors.primary else colors.surface,
                contentColor = if (answered) colors.onPrimary else colors.onSurface,
                border = BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) colors.primary else colors.outlineVariant),
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClickLabel = label) { onGoTo(i) },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${i + 1}", style = SistaTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun QuestionBody(question: CbtQuestionItem, selected: String?, onSelect: (CbtQuestionItem, String) -> Unit) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            CbtLatexMathView(text = question.questionText, style = SistaTheme.typography.bodyLarge, color = SistaTheme.colors.onSurface)
            if (!question.imageUrl.isNullOrBlank()) {
                CbtImageViewer(imageUrl = question.imageUrl, maxHeight = 220.dp, allowZoom = true)
            }
        }
    }
    question.options.forEach { option ->
        val checked = selected == option.key
        val colors = SistaTheme.colors
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (checked) StatusTone.Brand.colors().container else colors.surface,
            border = BorderStroke(if (checked) 2.dp else 1.dp, if (checked) colors.primary else colors.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                // FASE 74.1: per-option automation hook keyed on the real answer key.
                .testTag("cbt_option_${option.key}")
                .clickable { onSelect(question, option.key) },
        ) {
            Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(if (checked) colors.primary else colors.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.key,
                        style = SistaTheme.typography.labelLarge,
                        color = if (checked) colors.onPrimary else colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    CbtLatexMathView(text = option.text, style = SistaTheme.typography.bodyMedium, color = colors.onSurface)
                    if (!option.imageUrl.isNullOrBlank()) {
                        CbtImageViewer(imageUrl = option.imageUrl, maxHeight = 110.dp, allowZoom = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationBar(isFirst: Boolean, isLast: Boolean, onPrevious: () -> Unit, onNext: () -> Unit, onSubmit: () -> Unit) {
    Surface(color = SistaTheme.colors.surface, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SistaButton(
                stringResource(R.string.cbt_previous),
                onPrevious,
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                enabled = !isFirst,
                modifier = Modifier.weight(1f),
            )
            if (isLast) {
                SistaButton(
                    stringResource(R.string.cbt_collect),
                    onSubmit,
                    leadingIcon = Icons.Outlined.Check,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cbt_collect_button"),
                )
            } else {
                SistaButton(
                    stringResource(R.string.cbt_next),
                    onNext,
                    leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cbt_next_button"),
                )
            }
        }
    }
}

/** Shown over the exam once its violation limit is reached; the only way out is leaving. */
@Composable
fun ExamLockedPanel(maxViolations: Int?, history: List<ExamViolationRecord>, onLeave: () -> Unit) {
    ShellTheme {
        Surface(color = SistaTheme.colors.background, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.screen),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
            ) {
                IconBadge(Icons.Outlined.Lock, tone = StatusTone.Danger, size = 64.dp)
                Text(stringResource(R.string.cbt_locked_title), style = SistaTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    if (maxViolations != null) stringResource(R.string.cbt_locked_body_limit, maxViolations) else stringResource(R.string.cbt_locked_body),
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (history.isNotEmpty()) {
                    SistaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Text(stringResource(R.string.cbt_locked_log), style = SistaTheme.typography.labelLarge)
                            history.takeLast(5).forEach { record ->
                                Text(
                                    stringResource(R.string.cbt_locked_entry, record.violationNumber, stringResource(record.type.titleRes)),
                                    style = SistaTheme.typography.bodySmall,
                                    color = SistaTheme.colors.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                SistaButton(stringResource(R.string.cbt_locked_leave), onLeave, variant = ButtonVariant.Secondary, fullWidth = true)
            }
        }
    }
}

@Composable
private fun ViolationWarningDialog(warning: ExamViolationRecord, maxViolations: Int?, onDismiss: () -> Unit) {
    val lastChance = maxViolations != null && warning.violationNumber == maxViolations - 1
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { IconBadge(Icons.Outlined.Lock, tone = if (lastChance) StatusTone.Danger else StatusTone.Warning) },
        title = {
            Text(
                if (maxViolations != null) {
                    stringResource(R.string.cbt_warning_title_of, warning.violationNumber, maxViolations)
                } else {
                    stringResource(R.string.cbt_warning_title)
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(stringResource(R.string.cbt_warning_body, stringResource(warning.type.titleRes)))
                if (lastChance) {
                    Text(stringResource(R.string.cbt_warning_last), color = StatusTone.Danger.colors().content)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cbt_warning_ok)) } },
    )
}
