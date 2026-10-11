package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.RemedialItem
import com.sultanagung1.sista.feature.teacher.R
import java.time.LocalDate
import java.util.Locale

/** "Remedial Saya": the remedial work teachers gave this student, as the server has it. */
@Composable
fun RemedialScreen(
    viewModel: StudentRemedialViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    RemedialContent(state, today = StudentRemedialRules.today(), onRetry = viewModel::load, onNavigateBack = onNavigateBack)
}

@Composable
fun RemedialContent(
    state: StudentRemedialUiState,
    today: LocalDate,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = stringResource(R.string.student_remedial_title), onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            val remedials = state.remedials
            when {
                remedials == null && state.isLoading ->
                    SkeletonList(Modifier.padding(padding).padding(Spacing.screen), rows = 3)
                remedials == null ->
                    ErrorState(
                        title = stringResource(R.string.student_remedial_load_failed),
                        body = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.padding(padding).fillMaxSize(),
                    )
                remedials.isEmpty() ->
                    EmptyState(
                        title = stringResource(R.string.student_remedial_empty_title),
                        body = stringResource(R.string.student_remedial_empty_body),
                        icon = Icons.Outlined.TaskAlt,
                        modifier = Modifier.padding(padding).fillMaxSize(),
                    )
                else -> {
                    val locale = LocalConfiguration.current.locales[0]
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).testTag("remedial_list"),
                        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        // A refresh that failed keeps the list and says so.
                        state.errorMessage?.let { message ->
                            item(key = "error") { InlineBanner(message = message, tone = StatusTone.Danger, actionLabel = stringResource(R.string.student_remedial_retry), onAction = onRetry) }
                        }
                        items(remedials, key = { it.id }) { RemedialCard(it, today, locale) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemedialCard(item: RemedialItem, today: LocalDate, locale: Locale) {
    val status = StudentRemedialRules.statusOf(item)
    val overdue = StudentRemedialRules.isOverdue(item, today)
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(end = Spacing.sm)) {
                Text(
                    item.subjectName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.student_remedial_unknown),
                    style = SistaTheme.typography.labelMedium,
                    color = SistaTheme.colors.primary,
                )
                Text(
                    item.assessmentTitle?.takeIf { it.isNotBlank() } ?: stringResource(R.string.student_remedial_unknown),
                    style = SistaTheme.typography.titleMedium,
                )
            }
            when {
                status == StudentRemedialRules.Status.DONE ->
                    StatusPill(stringResource(R.string.student_remedial_status_done), StatusTone.Success)
                overdue ->
                    StatusPill(stringResource(R.string.student_remedial_status_overdue), StatusTone.Danger)
                status == StudentRemedialRules.Status.PENDING ->
                    StatusPill(stringResource(R.string.student_remedial_status_pending), StatusTone.Warning)
                else ->
                    StatusPill(item.status?.takeIf { it.isNotBlank() } ?: stringResource(R.string.student_remedial_unknown), StatusTone.Neutral)
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ScoreValue(stringResource(R.string.student_remedial_original), StudentRemedialRules.score(item.originalScore, locale), Modifier.weight(1f))
            ScoreValue(stringResource(R.string.student_remedial_kkm), StudentRemedialRules.score(item.kkm, locale), Modifier.weight(1f))
            ScoreValue(stringResource(R.string.student_remedial_result), StudentRemedialRules.score(item.remedialScore, locale), Modifier.weight(1f))
        }

        item.teacherNotes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(
                stringResource(R.string.student_remedial_notes, notes),
                style = SistaTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.md),
            )
        }
        if (status != StudentRemedialRules.Status.DONE) {
            StudentRemedialRules.deadlineText(item.deadline, locale)?.let { deadline ->
                Text(
                    stringResource(R.string.student_remedial_deadline, deadline),
                    style = SistaTheme.typography.bodySmall,
                    color = if (overdue) SistaTheme.colors.error else SistaTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun ScoreValue(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant)
        Text(value ?: "–", style = SistaTheme.typography.titleMedium)
    }
}
