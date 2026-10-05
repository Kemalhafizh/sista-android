package com.sultanagung1.sista.ui.cbt

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.text.localDecimal
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.CbtExamItem
import com.sultanagung1.sista.feature.cbt.R

/**
 * The student's CBT exams from `student/cbt/exams`: only published exams of
 * their class, each with where they stand (open, upcoming, done, locked...)
 * as the server reports it.
 */
@Composable
fun CbtExamListScreen(
    viewModel: CbtViewModel,
    onNavigateToRoom: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadExams() }

    // The spinner follows the real request instead of a fixed delay.
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) refreshRequested = false
    }

    CbtExamListContent(
        exams = uiState.exams,
        loaded = uiState.examsLoaded,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        refreshing = refreshRequested && uiState.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.loadExams()
        },
        onOpen = { onNavigateToRoom(it.id) },
        onNavigateBack = onNavigateBack,
    )
}

/** The exam list without a ViewModel, for previews and screenshots. */
@Composable
fun CbtExamListContent(
    exams: List<CbtExamItem>,
    loaded: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpen: (CbtExamItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = stringResource(R.string.cbt_list_title), onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    item(key = "rules") {
                        InlineBanner(
                            title = stringResource(R.string.cbt_rules_title),
                            message = stringResource(R.string.cbt_rules_body),
                            tone = StatusTone.Info,
                        )
                    }
                    if (errorMessage != null && exams.isNotEmpty()) {
                        item(key = "stale") {
                            InlineBanner(
                                message = stringResource(R.string.cbt_stale, errorMessage),
                                tone = StatusTone.Warning,
                                actionLabel = stringResource(R.string.cbt_retry),
                                onAction = onRefresh,
                            )
                        }
                    }
                    when {
                        (isLoading || !loaded) && exams.isEmpty() -> item(key = "loading") { SkeletonList(rows = 3) }
                        errorMessage != null && exams.isEmpty() -> item(key = "error") {
                            ErrorState(title = stringResource(R.string.cbt_list_error), body = errorMessage, onRetry = onRefresh)
                        }
                        exams.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = stringResource(R.string.cbt_list_empty),
                                body = stringResource(R.string.cbt_list_empty_body),
                                icon = Icons.Outlined.Quiz,
                            )
                        }
                        else -> items(exams, key = { it.id }, contentType = { "exam" }) { exam ->
                            ExamCard(exam, onOpen = { onOpen(exam) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCard(exam: CbtExamItem, onOpen: () -> Unit) {
    val availability = CbtExamFormat.availabilityOf(exam)
    SistaCard(
        modifier = Modifier
            .fillMaxWidth()
            .sulaoneSharedBounds(key = "cbt_exam_card_${exam.id}"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOfNotNull(exam.subject, CbtExamFormat.examType(exam.type)?.asString()).joinToString(" · "),
                    style = SistaTheme.typography.labelMedium,
                    color = SistaTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.sm))
                StatusPill(stringResource(availability.label), availability.tone)
            }
            Text(exam.title, style = SistaTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            ExamFacts(exam)
            CbtExamFormat.schedule(exam.startsAt, exam.endsAt)?.let {
                Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
            }
            when (availability) {
                ExamAvailability.DONE -> exam.score?.let {
                    Text(
                        stringResource(R.string.cbt_score, localDecimal(it)),
                        style = SistaTheme.typography.labelLarge,
                        color = SistaTheme.colors.primary,
                    )
                }
                ExamAvailability.FORCE_CLOSED -> Text(
                    stringResource(R.string.cbt_force_closed_hint),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
                else -> Unit
            }
            if (availability.canEnter) {
                SistaButton(
                    stringResource(if (availability == ExamAvailability.IN_PROGRESS) R.string.cbt_continue else R.string.cbt_start),
                    onOpen,
                    leadingIcon = Icons.Outlined.PlayArrow,
                    fullWidth = true,
                )
            }
        }
    }
}

/** "Durasi: 90 menit · Soal: 25 · KKM: 75", leaving out what the server didn't send. */
@Composable
internal fun ExamFacts(exam: CbtExamItem) {
    val facts = listOfNotNull(
        exam.durationMinutes?.let { stringResource(R.string.cbt_duration, it) },
        exam.totalQuestions?.let { stringResource(R.string.cbt_questions, it) },
        exam.passingScore?.let { stringResource(R.string.cbt_passing, localDecimal(it)) },
    )
    if (facts.isNotEmpty()) {
        Text(facts.joinToString(" · "), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
    }
}
