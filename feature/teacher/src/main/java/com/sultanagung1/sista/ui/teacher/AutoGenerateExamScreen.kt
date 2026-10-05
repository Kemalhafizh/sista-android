package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.AutoGenerateExamRequest
import com.sultanagung1.sista.data.model.QuestionBankItem
import com.sultanagung1.sista.feature.teacher.R
import com.sultanagung1.sista.ui.cbt.components.CbtLatexMathView

/** What the teacher asks the bank for. [categoryId] null means every category. */
data class QuestionPickForm(
    val categoryId: Long? = null,
    val count: String = "10",
    val mixIndex: Int = 0,
) {
    /** `question-bank/auto-generate` accepts 1..100 questions. */
    val countValue: Int? get() = count.toIntOrNull()?.takeIf { it in 1..100 }
}

/**
 * Previews a set of questions picked from the bank by difficulty. The server
 * only returns the set (no exam id is sent), so nothing is saved here; an exam
 * is made in "Buat ujian".
 */
@Composable
fun AutoGenerateExamScreen(
    viewModel: QuestionBankViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var count by rememberSaveable { mutableStateOf("10") }
    var mixIndex by rememberSaveable { mutableIntStateOf(0) }
    val form = QuestionPickForm(categoryId, count, mixIndex)

    AutoGenerateContent(
        state = uiState,
        form = form,
        onCategory = { categoryId = it },
        onCount = { count = it.filter(Char::isDigit).take(3) },
        onMix = { mixIndex = it },
        onPick = {
            form.countValue?.let { total ->
                viewModel.autoGenerate(
                    AutoGenerateExamRequest(
                        categoryId = form.categoryId,
                        totalQuestions = total,
                        difficultyDistribution = QuestionBankFormat.MIXES[form.mixIndex],
                    ),
                )
            }
        },
        onNavigateBack = onNavigateBack,
    )
}

/** The preview without a ViewModel, for previews and screenshots. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoGenerateContent(
    state: QuestionBankUiState,
    form: QuestionPickForm,
    onCategory: (Long?) -> Unit,
    onCount: (String) -> Unit,
    onMix: (Int) -> Unit,
    onPick: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = stringResource(R.string.qb_preview), onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "note") { InlineBanner(message = stringResource(R.string.qb_preview_note), tone = StatusTone.Info) }
                item(key = "form") {
                    SistaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            Text(stringResource(R.string.qb_category), style = SistaTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Choice(stringResource(R.string.qb_all_categories), form.categoryId == null) { onCategory(null) }
                                state.categories.forEach { category ->
                                    Choice(category.name, form.categoryId == category.id) { onCategory(category.id) }
                                }
                            }
                            SistaTextField(
                                value = form.count,
                                onValueChange = onCount,
                                label = stringResource(R.string.qb_count),
                                errorText = stringResource(R.string.qb_count_invalid).takeIf { form.countValue == null },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            Text(stringResource(R.string.qb_mix), style = SistaTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                QuestionBankFormat.MIXES.forEachIndexed { index, mix ->
                                    Choice(QuestionBankFormat.mixLabel(mix), form.mixIndex == index) { onMix(index) }
                                }
                            }
                            Text(
                                stringResource(R.string.qb_mix_hint),
                                style = SistaTheme.typography.bodySmall,
                                color = SistaTheme.colors.onSurfaceVariant,
                            )
                            SistaButton(
                                stringResource(R.string.qb_pick),
                                onPick,
                                leadingIcon = Icons.Outlined.AutoAwesome,
                                loading = state.isGenerating,
                                enabled = !state.isGenerating && form.countValue != null,
                                fullWidth = true,
                            )
                        }
                    }
                }
                state.generateError?.let { error ->
                    item(key = "error") { InlineBanner(message = error, tone = StatusTone.Danger) }
                }
                state.generatedExam?.let { result ->
                    item(key = "result_header") {
                        SectionHeader(stringResource(R.string.qb_picked, result.totalGenerated))
                    }
                    if (result.items.isEmpty()) {
                        item(key = "result_empty") { InlineBanner(message = stringResource(R.string.qb_picked_none), tone = StatusTone.Warning) }
                    }
                    itemsIndexed(result.items, key = { _, item -> item.id }) { index, item -> PickedQuestion(index + 1, item) }
                }
            }
        }
    }
}

@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) ({ Icon(Icons.Outlined.Check, contentDescription = null) }) else null,
    )
}

@Composable
private fun PickedQuestion(number: Int, item: QuestionBankItem) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.qb_question_number, number),
                    style = SistaTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.sm))
                StatusPill(
                    listOfNotNull(QuestionBankFormat.difficulty(item.difficulty).asString(), item.cognitiveLevel).joinToString(" · "),
                    QuestionBankFormat.difficultyTone(item.difficulty),
                )
            }
            CbtLatexMathView(text = item.questionText, style = SistaTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.qb_answer_key, item.correctAnswer),
                style = SistaTheme.typography.labelLarge,
                color = SistaTheme.colors.primary,
            )
        }
    }
}
