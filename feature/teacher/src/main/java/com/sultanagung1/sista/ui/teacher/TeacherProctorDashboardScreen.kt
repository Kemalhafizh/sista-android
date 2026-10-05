package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.LockedExamStudent
import com.sultanagung1.sista.feature.teacher.R
import kotlinx.coroutines.delay

/**
 * The operator's screen for one exam: the entry token students type, and the
 * students the anti-cheat locked out, each of whom can be let back in.
 */
@Composable
fun TeacherProctorDashboardScreen(
    examId: Long,
    viewModel: CbtProctorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()

    LaunchedEffect(examId) { viewModel.load(examId) }

    // Counts down from the server's remaining_seconds, re-synced on every new token.
    // Starts from the server's value on the first frame, so a fresh token never flashes as expired.
    var remaining by remember(uiState.token) { mutableIntStateOf(uiState.token?.remainingSeconds ?: 0) }
    LaunchedEffect(uiState.token) {
        while (remaining > 0) {
            delay(1000)
            remaining--
        }
    }

    TeacherProctorContent(
        examId = examId,
        state = uiState,
        remainingSeconds = uiState.token?.remainingSeconds?.let { remaining },
        onRenewToken = {
            haptics.tapHeavy()
            viewModel.regenerateToken(examId)
        },
        onRetryToken = { viewModel.loadToken(examId) },
        onRefreshLocked = { viewModel.loadLockedStudents(examId) },
        onUnlock = { student ->
            haptics.tapHeavy()
            viewModel.unlock(examId, student)
        },
        onDismissUnlockMessage = viewModel::clearUnlockMessage,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * The proctor screen without a ViewModel, for previews and screenshots.
 * [remainingSeconds] is the live countdown, or null when the server sent none.
 */
@Composable
fun TeacherProctorContent(
    examId: Long,
    state: CbtProctorUiState,
    remainingSeconds: Int?,
    onRenewToken: () -> Unit,
    onRetryToken: () -> Unit,
    onRefreshLocked: () -> Unit,
    onUnlock: (LockedExamStudent) -> Unit,
    onDismissUnlockMessage: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(R.string.pr_title),
                    subtitle = state.token?.title ?: stringResource(R.string.pr_exam_number, examId),
                    onBack = onNavigateBack,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                item(key = "token") { TokenCard(state, remainingSeconds, onRenewToken, onRetryToken) }
                item(key = "locked_header") {
                    SectionHeader(
                        stringResource(R.string.pr_locked),
                        actionLabel = stringResource(R.string.th_reload),
                        onAction = onRefreshLocked,
                    )
                }
                state.unlockMessage?.let { message ->
                    item(key = "unlock_message") {
                        InlineBanner(
                            message = message.asString(),
                            tone = if (state.unlockFailed) StatusTone.Danger else StatusTone.Success,
                            onDismiss = onDismissUnlockMessage,
                        )
                    }
                }
                item(key = "locked") { LockedStudents(state, onRefreshLocked, onUnlock) }
            }
        }
    }
}

@Composable
private fun TokenCard(state: CbtProctorUiState, remainingSeconds: Int?, onRenew: () -> Unit, onRetry: () -> Unit) {
    val token = state.token
    val expired = token?.isExpired == true || remainingSeconds == 0
    val tone = ProctorFormat.tokenTone(expired, remainingSeconds)
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.Key, tone = tone)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.pr_token), style = SistaTheme.typography.titleMedium)
                    token?.durationMinutes?.let {
                        Text(
                            stringResource(R.string.pr_token_valid_for, it),
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
            when {
                token == null && state.isLoading -> SkeletonList(rows = 1)
                token == null -> InlineBanner(
                    message = state.errorMessage ?: stringResource(R.string.pr_token_missing),
                    tone = StatusTone.Danger,
                    actionLabel = stringResource(R.string.tj_retry),
                    onAction = onRetry,
                )
                else -> {
                    val description = if (expired) stringResource(R.string.pr_token_expired) else token.accessToken
                    Text(
                        if (expired) "– – – – – –" else token.accessToken.chunked(1).joinToString(" "),
                        style = SistaTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                        ),
                        color = if (expired) SistaTheme.colors.onSurfaceVariant else SistaTheme.colors.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = description },
                    )
                    if (remainingSeconds != null && token.durationMinutes != null) {
                        LinearProgressIndicator(
                            progress = { (remainingSeconds.toFloat() / (token.durationMinutes!! * 60)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(SistaTheme.shapes.small),
                            color = tone.colors().content,
                            trackColor = SistaTheme.colors.surfaceVariant,
                            drawStopIndicator = {},
                        )
                    }
                    Text(
                        when {
                            expired -> stringResource(R.string.pr_token_expired)
                            remainingSeconds != null -> stringResource(R.string.pr_token_remaining, ProctorFormat.countdown(remainingSeconds))
                            else -> stringResource(R.string.pr_token_active)
                        },
                        style = SistaTheme.typography.labelLarge,
                        color = tone.colors().content,
                    )
                    if (state.errorMessage != null) InlineBanner(message = state.errorMessage, tone = StatusTone.Danger)
                }
            }
            SistaButton(
                stringResource(R.string.pr_token_renew),
                onRenew,
                leadingIcon = Icons.Outlined.Refresh,
                variant = if (expired) ButtonVariant.Primary else ButtonVariant.Secondary,
                loading = state.isLoading && token != null,
                enabled = !state.isLoading,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun LockedStudents(state: CbtProctorUiState, onRetry: () -> Unit, onUnlock: (LockedExamStudent) -> Unit) {
    when {
        state.lockedLoading && state.lockedStudents.isEmpty() -> SkeletonList(rows = 2)
        state.lockedError != null && state.lockedStudents.isEmpty() -> InlineBanner(
            message = state.lockedError,
            tone = StatusTone.Danger,
            actionLabel = stringResource(R.string.tj_retry),
            onAction = onRetry,
        )
        state.lockedStudents.isEmpty() -> EmptyState(
            title = stringResource(R.string.pr_locked_empty),
            body = stringResource(R.string.pr_locked_empty_body),
            icon = Icons.Outlined.VerifiedUser,
        )
        else -> SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs)) {
            Column {
                state.lockedStudents.forEachIndexed { index, student ->
                    if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                    LockedRow(student, unlocking = state.unlockingStudentId == student.studentId, enabled = state.unlockingStudentId == null) {
                        onUnlock(student)
                    }
                }
            }
        }
    }
}

@Composable
private fun LockedRow(student: LockedExamStudent, unlocking: Boolean, enabled: Boolean, onUnlock: () -> Unit) {
    Row(Modifier.padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                student.name ?: stringResource(R.string.pr_student_unnamed),
                style = SistaTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(student.nis?.let { stringResource(R.string.si_nis, it) }, student.classroom).joinToString(" · "),
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
            )
            Text(
                listOfNotNull(
                    student.reason,
                    student.violationCount?.takeIf { it > 0 }?.let { stringResource(R.string.pr_violations, it) },
                ).joinToString(" · "),
                style = SistaTheme.typography.bodySmall,
                color = StatusTone.Danger.colors().content,
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        SistaButton(
            stringResource(R.string.pr_unlock),
            onUnlock,
            variant = ButtonVariant.Secondary,
            leadingIcon = Icons.Outlined.LockOpen,
            loading = unlocking,
            enabled = enabled,
        )
    }
}
