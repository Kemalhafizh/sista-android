package com.sultanagung1.sista.ui.cbt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.CbtExamItem
import com.sultanagung1.sista.data.model.TokenValidationState
import com.sultanagung1.sista.feature.cbt.R

/**
 * The gate before an exam: the student types the six-character token the
 * proctor shows. The server decides; when it refuses, its own reason (wrong,
 * expired, another class, already finished, locked) is shown as sent.
 */
@Composable
fun CbtTokenEntryScreen(
    examId: Long,
    viewModel: CbtViewModel,
    onTokenValidated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var tokenInput by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()

    LaunchedEffect(Unit) {
        viewModel.resetTokenValidationState()
        viewModel.loadExams()
    }
    LaunchedEffect(uiState.tokenValidationState) {
        if (uiState.tokenValidationState is TokenValidationState.Success) {
            haptics.success()
            onTokenValidated()
        }
    }

    CbtTokenEntryContent(
        exam = uiState.exams.firstOrNull { it.id == examId },
        examLoading = !uiState.examsLoaded,
        token = tokenInput,
        validation = uiState.tokenValidationState,
        onTokenChange = { tokenInput = CbtExamFormat.cleanToken(it) },
        onSubmit = {
            haptics.tapLight()
            viewModel.validateExamToken(examId, tokenInput)
        },
        onNavigateBack = onNavigateBack,
    )
}

/** The token gate without a ViewModel, for previews and screenshots. */
@Composable
fun CbtTokenEntryContent(
    exam: CbtExamItem?,
    examLoading: Boolean,
    token: String,
    validation: TokenValidationState,
    onTokenChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    requestFocus: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val checking = validation is TokenValidationState.Loading
    val ready = token.length == CbtExamFormat.TOKEN_LENGTH && !checking
    val submit = {
        if (ready) {
            focusManager.clearFocus()
            onSubmit()
        }
    }
    if (requestFocus) {
        LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    }

    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = stringResource(R.string.cbt_token_title), onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                when {
                    exam != null -> ExamSummary(exam)
                    examLoading -> SkeletonList(rows = 1)
                    else -> InlineBanner(message = stringResource(R.string.cbt_token_exam_unknown), tone = StatusTone.Neutral)
                }

                SistaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Outlined.Key)
                            Spacer(Modifier.width(Spacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.cbt_token_heading), style = SistaTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.cbt_token_hint),
                                    style = SistaTheme.typography.bodySmall,
                                    color = SistaTheme.colors.onSurfaceVariant,
                                )
                            }
                        }
                        SistaTextField(
                            value = token,
                            onValueChange = onTokenChange,
                            label = stringResource(R.string.cbt_token_label),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                // FASE 74.1: automation hook for the E2E CBT exam flow.
                                .testTag("cbt_token_input"),
                            helperText = stringResource(R.string.cbt_token_count, token.length, CbtExamFormat.TOKEN_LENGTH),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            enabled = !checking,
                        )
                        (validation as? TokenValidationState.Error)?.let {
                            InlineBanner(message = it.message, tone = StatusTone.Danger)
                        }
                        SistaButton(
                            stringResource(R.string.cbt_token_submit),
                            onClick = submit,
                            leadingIcon = Icons.Outlined.PlayArrow,
                            loading = checking,
                            enabled = ready,
                            fullWidth = true,
                            modifier = Modifier.testTag("cbt_start_exam_button"),
                        )
                    }
                }

                SectionHeader(stringResource(R.string.cbt_rules_heading))
                SistaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        listOf(R.string.cbt_rule_app, R.string.cbt_rule_leave, R.string.cbt_rule_reset).forEach { rule ->
                            Row(verticalAlignment = Alignment.Top) {
                                IconBadge(Icons.Outlined.Security, tone = StatusTone.Warning, size = 28.dp)
                                Spacer(Modifier.width(Spacing.md))
                                Text(
                                    stringResource(rule),
                                    style = SistaTheme.typography.bodySmall,
                                    color = SistaTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamSummary(exam: CbtExamItem) {
    SistaCard(
        modifier = Modifier
            .fillMaxWidth()
            .sulaoneSharedBounds(key = "cbt_exam_card_${exam.id}"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    exam.subject.orEmpty(),
                    style = SistaTheme.typography.labelMedium,
                    color = SistaTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                CbtExamFormat.examType(exam.type)?.let { StatusPill(it.asString(), StatusTone.Brand) }
            }
            Text(exam.title, style = SistaTheme.typography.titleMedium)
            ExamFacts(exam)
            CbtExamFormat.schedule(exam.startsAt, exam.endsAt)?.let {
                Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
            }
        }
    }
}
