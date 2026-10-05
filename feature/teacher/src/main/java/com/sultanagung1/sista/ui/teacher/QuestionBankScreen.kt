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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.QuestionBankCategory
import com.sultanagung1.sista.feature.teacher.R

/**
 * The question bank from `question-bank/categories`: each competency with how
 * many of its questions are easy, medium and hard, as counted by the server.
 */
@Composable
fun QuestionBankScreen(
    viewModel: QuestionBankViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAutoGenerate: () -> Unit,
    onNavigateToManualCreate: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    QuestionBankContent(
        state = uiState,
        onRetry = { viewModel.fetchCategories() },
        onPreview = onNavigateToAutoGenerate,
        onCreateExam = onNavigateToManualCreate,
        onNavigateBack = onNavigateBack,
    )
}

/** The question bank without a ViewModel, for previews and screenshots. */
@Composable
fun QuestionBankContent(
    state: QuestionBankUiState,
    onRetry: () -> Unit,
    onPreview: () -> Unit,
    onCreateExam: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(R.string.qb_title),
                    onBack = onNavigateBack,
                    actions = {
                        IconButton(onClick = onPreview) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = stringResource(R.string.qb_preview))
                        }
                    },
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateExam,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.qb_create_exam)) },
                    containerColor = SistaTheme.colors.primary,
                    contentColor = SistaTheme.colors.onPrimary,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            val categories = state.categories
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl * 2),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                if (state.errorMessage != null && categories.isNotEmpty()) {
                    item(key = "stale") {
                        InlineBanner(
                            message = stringResource(R.string.tj_stale, state.errorMessage),
                            tone = StatusTone.Warning,
                            actionLabel = stringResource(R.string.tj_retry),
                            onAction = onRetry,
                        )
                    }
                }
                when {
                    state.isLoading && categories.isEmpty() -> item(key = "loading") { SkeletonList(rows = 4) }
                    state.errorMessage != null && categories.isEmpty() -> item(key = "error") {
                        ErrorState(title = stringResource(R.string.qb_error), body = state.errorMessage, onRetry = onRetry)
                    }
                    categories.isEmpty() -> item(key = "empty") {
                        EmptyState(
                            title = stringResource(R.string.qb_empty),
                            body = stringResource(R.string.qb_empty_body),
                            icon = Icons.Outlined.Quiz,
                        )
                    }
                    else -> {
                        item(key = "header") {
                            SectionHeader(stringResource(R.string.qb_categories, categories.size))
                        }
                        items(categories, key = { it.id }, contentType = { "category" }) { CategoryCard(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: QuestionBankCategory) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(category.name, style = SistaTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val meta = listOfNotNull(
                        category.gradeLevel?.let { stringResource(R.string.qb_grade, it) },
                        category.curriculumRef?.takeIf { it.isNotBlank() },
                    )
                    if (meta.isNotEmpty()) {
                        Text(
                            meta.joinToString(" · "),
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.width(Spacing.sm))
                StatusPill(stringResource(R.string.qb_items, category.itemsCount), StatusTone.Neutral)
            }
            category.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (category.itemsCount > 0) DifficultyRow(category)
        }
    }
}

/** Easy, medium and hard as counted by the server; "–" when it sent no count. */
@Composable
private fun DifficultyRow(category: QuestionBankCategory) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        QuestionBankFormat.DIFFICULTIES.forEach { code ->
            val count = when (code) {
                "mudah" -> category.easyCount
                "sedang" -> category.mediumCount
                else -> category.hardCount
            }
            Column {
                Text(
                    count?.toString() ?: "–",
                    style = SistaTheme.typography.titleMedium,
                    color = QuestionBankFormat.difficultyTone(code).colors().content,
                )
                Text(
                    QuestionBankFormat.difficulty(code).asString(),
                    style = SistaTheme.typography.labelMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}
