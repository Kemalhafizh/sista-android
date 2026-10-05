package com.sultanagung1.sista.ui.teacher

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
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.TeacherCbtExamItem
import com.sultanagung1.sista.feature.teacher.R

/**
 * "Pengawas CBT" entry point. Shows only exams the backend says this teacher
 * operates; if exactly one is ongoing it opens that exam's proctor screen
 * directly (replacePicker = true). Never invents an exam id.
 */
@Composable
fun TeacherProctorExamsScreen(
    viewModel: TeacherProctorExamsViewModel = hiltViewModel(),
    onOpenExam: (examId: Long, replacePicker: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.autoOpenExamId) {
        uiState.autoOpenExamId?.let { examId ->
            viewModel.onAutoOpenConsumed()
            onOpenExam(examId, true)
        }
    }

    TeacherProctorExamsContent(
        state = uiState,
        onRefresh = viewModel::loadExams,
        onOpen = { onOpenExam(it.id, false) },
        onNavigateBack = onNavigateBack,
    )
}

/** The proctor's exam list without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherProctorExamsContent(
    state: TeacherProctorExamsUiState,
    onRefresh: () -> Unit,
    onOpen: (TeacherCbtExamItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(R.string.pe_title),
                    subtitle = stringResource(R.string.pe_subtitle),
                    onBack = onNavigateBack,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = state.isLoading && state.hasLoaded,
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
                    if (state.errorMessage != null && state.exams.isNotEmpty()) {
                        item(key = "stale") {
                            InlineBanner(
                                message = stringResource(R.string.tj_stale, state.errorMessage),
                                tone = StatusTone.Warning,
                                actionLabel = stringResource(R.string.tj_retry),
                                onAction = onRefresh,
                            )
                        }
                    }
                    when {
                        !state.hasLoaded -> item(key = "loading") { SkeletonList(rows = 3) }
                        state.errorMessage != null && state.exams.isEmpty() -> item(key = "error") {
                            ErrorState(title = stringResource(R.string.pe_error), body = state.errorMessage, onRetry = onRefresh)
                        }
                        state.exams.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = stringResource(R.string.pe_empty),
                                body = stringResource(R.string.pe_empty_body),
                                icon = Icons.Outlined.EventBusy,
                            )
                        }
                        else -> items(state.exams, key = { it.id }, contentType = { "exam" }) { exam ->
                            ExamCard(exam, onClick = { onOpen(exam) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCard(exam: TeacherCbtExamItem, onClick: () -> Unit) {
    SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Security, tone = if (exam.isOngoing) StatusTone.Success else StatusTone.Neutral)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(exam.title, style = SistaTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(ProctorFormat.examType(exam.type).asString(), exam.subject, exam.classroom).joinToString(" · "),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (exam.isOngoing) {
                    StatusPill(stringResource(R.string.pe_ongoing), StatusTone.Success)
                } else {
                    Text(
                        ProctorFormat.examTime(exam.startTime)?.let { stringResource(R.string.pe_starts, it) }
                            ?: stringResource(R.string.pe_no_start),
                        style = SistaTheme.typography.labelMedium,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
