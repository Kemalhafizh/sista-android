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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
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
import com.sultanagung1.sista.data.model.DailyAssessmentItem
import com.sultanagung1.sista.data.model.RemedialDashboard
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.text.localDecimal
import com.sultanagung1.sista.feature.teacher.R

/**
 * The teacher's daily assessments from `assessments/teacher`: what each one
 * is, and how many students have a score. Opening one goes to its score sheet.
 */
@Composable
fun DailyAssessmentScreen(
    viewModel: DailyAssessmentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScoreInput: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    // Loads on open and again on return from a score sheet, so progress is current.
    LifecycleStartStopEffect(onStart = viewModel::fetchTeacherAssessments, onStop = {})

    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) refreshRequested = false }

    DailyAssessmentContent(
        assessments = uiState.assessments,
        remedial = uiState.remedialDashboard,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        refreshing = refreshRequested && uiState.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.fetchTeacherAssessments()
        },
        onOpen = { onNavigateToScoreInput(it.id) },
        onNavigateBack = onNavigateBack,
    )
}

/** The assessment list without a ViewModel, for previews and screenshots. */
@Composable
fun DailyAssessmentContent(
    assessments: List<DailyAssessmentItem>,
    remedial: RemedialDashboard?,
    isLoading: Boolean,
    errorMessage: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpen: (DailyAssessmentItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = stringResource(R.string.th_shortcut_assessment), onBack = onNavigateBack) },
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
                    if (remedial != null && remedial.total > 0) {
                        item(key = "remedial") { RemedialSummary(remedial) }
                    }
                    if (errorMessage != null && assessments.isNotEmpty()) {
                        item(key = "stale") {
                            InlineBanner(
                                message = stringResource(R.string.tj_stale, errorMessage),
                                tone = StatusTone.Warning,
                                actionLabel = stringResource(R.string.tj_retry),
                                onAction = onRefresh,
                            )
                        }
                    }
                    when {
                        isLoading && assessments.isEmpty() -> item(key = "loading") { SkeletonList(rows = 4) }
                        errorMessage != null && assessments.isEmpty() -> item(key = "error") {
                            ErrorState(title = stringResource(R.string.da_error), body = errorMessage, onRetry = onRefresh)
                        }
                        assessments.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = stringResource(R.string.da_empty),
                                body = stringResource(R.string.da_empty_body),
                                icon = Icons.AutoMirrored.Outlined.Assignment,
                            )
                        }
                        else -> items(assessments, key = { it.id }, contentType = { "assessment" }) { item ->
                            AssessmentCard(item, onClick = { onOpen(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemedialSummary(remedial: RemedialDashboard) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Replay, tone = if (remedial.pending > 0) StatusTone.Warning else StatusTone.Success)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.da_remedial), style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
                Text(
                    if (remedial.pending > 0) stringResource(R.string.da_remedial_pending, remedial.pending) else stringResource(R.string.da_remedial_done),
                    style = SistaTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.da_remedial_progress, remedial.completed, remedial.total),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AssessmentCard(item: DailyAssessmentItem, onClick: () -> Unit) {
    val total = item.classroom?.studentsCount
    val scored = item.scoredCount
    SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = SistaTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOfNotNull(
                            item.subject?.name,
                            item.classroom?.name?.let { stringResource(R.string.th_class, it) },
                            journalDate(item.assessmentDate),
                        ).joinToString(" · "),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                StatusPill(stringResource(R.string.da_kkm, localDecimal(item.kkm)), StatusTone.Neutral)
            }
            if (total != null && scored != null && total > 0) {
                val done = scored >= total
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { (scored.toFloat() / total).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(SistaTheme.shapes.small),
                        color = SistaTheme.colors.primary,
                        trackColor = SistaTheme.colors.surfaceVariant,
                        drawStopIndicator = {},
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        if (done) stringResource(R.string.da_remedial_done) else stringResource(R.string.da_scored, scored, total),
                        style = SistaTheme.typography.labelMedium,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
