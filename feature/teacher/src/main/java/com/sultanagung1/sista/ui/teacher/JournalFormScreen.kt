package com.sultanagung1.sista.ui.teacher

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.sultanagung1.sista.core.ui.component.ErrorState
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
import com.sultanagung1.sista.data.model.JournalScheduleItem
import com.sultanagung1.sista.ui.common.DraftRestoreDialog
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.feature.teacher.R

/**
 * Common methods offered as one tap, in the app's language. The server accepts
 * any text for `learning_method`, so the chosen words are stored as written.
 */
val JOURNAL_METHODS = listOf(
    R.string.jf_m_lecture,
    R.string.jf_m_group,
    R.string.jf_m_practicum,
    R.string.jf_m_pbl,
    R.string.jf_m_pjbl,
    R.string.jf_m_collab,
)

/** What the form sends, from the draft kept in [JournalMobileViewModel] (survives process death). */
data class JournalDraft(
    val topic: String = "",
    val method: String = "",
    val activity: String = "",
    val present: String = "",
    val absent: String = "",
    val objectivesMet: Boolean = true,
    val obstacles: String = "",
    val followUp: String = "",
) {
    val topicMissing get() = topic.isBlank()
    val methodMissing get() = method.isBlank()
    val activityMissing get() = activity.isBlank()
    val presentInvalid get() = present.toIntOrNull()?.takeIf { it >= 0 } == null
    val absentInvalid get() = absent.toIntOrNull()?.takeIf { it >= 0 } == null

    /** `teacher/journals` requires topic, method, activity and both counts. */
    val isComplete get() = !(topicMissing || methodMissing || activityMissing || presentInvalid || absentInvalid)
}

/** Callbacks for each field, so the content stays free of the ViewModel. */
data class JournalDraftActions(
    val onTopic: (String) -> Unit = {},
    val onMethod: (String) -> Unit = {},
    val onActivity: (String) -> Unit = {},
    val onPresent: (String) -> Unit = {},
    val onAbsent: (String) -> Unit = {},
    val onObjectivesMet: (Boolean) -> Unit = {},
    val onObstacles: (String) -> Unit = {},
    val onFollowUp: (String) -> Unit = {},
)

/** Fills the journal for one of today's slots and sends it to `POST teacher/journals`. */
@Composable
fun JournalFormScreen(
    scheduleId: String,
    viewModel: JournalMobileViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val schedule = viewModel.getScheduleById(scheduleId)
    val context = LocalContext.current
    val savedToast = stringResource(R.string.jf_saved_toast)

    // FASE 69.1: the draft lives in the ViewModel (SavedStateHandle-backed), seeded once
    // the slot is known so an existing entry's values are not lost to a blank seed.
    LaunchedEffect(scheduleId, schedule != null) {
        if (schedule != null || !uiState.isLoading) viewModel.startDraftFor(scheduleId, schedule)
    }
    // FASE 69.2: a draft from an earlier, closed session exists for this slot.
    if (uiState.restorableDraftAvailable) {
        DraftRestoreDialog(
            onRestore = { viewModel.restorePersistedDraft() },
            onDiscard = { viewModel.discardPersistedDraft() },
        )
    }

    val draft = JournalDraft(
        topic = uiState.draftMateriPokok,
        method = uiState.draftMetode,
        activity = uiState.draftMedia,
        present = uiState.draftHadir,
        absent = uiState.draftAbsen,
        objectivesMet = uiState.draftKompetensiTercapai,
        obstacles = uiState.draftCatatan,
        followUp = uiState.draftTindakLanjut,
    )
    JournalFormContent(
        schedule = schedule,
        loading = uiState.isLoading,
        draft = draft,
        submitting = uiState.isSubmitting,
        errorMessage = uiState.errorMessage,
        actions = JournalDraftActions(
            onTopic = viewModel::updateDraftMateriPokok,
            onMethod = viewModel::updateDraftMetode,
            onActivity = viewModel::updateDraftMedia,
            onPresent = viewModel::updateDraftHadir,
            onAbsent = viewModel::updateDraftAbsen,
            onObjectivesMet = viewModel::updateDraftKompetensi,
            onObstacles = viewModel::updateDraftCatatan,
            onFollowUp = viewModel::updateDraftTindakLanjut,
        ),
        onRetry = viewModel::loadSchedules,
        onSubmit = {
            viewModel.submitJournal(
                scheduleId = scheduleId,
                materiPokok = draft.topic.trim(),
                metode = draft.method.trim(),
                media = draft.activity.trim(),
                hadir = draft.present.toInt(),
                absen = draft.absent.toInt(),
                isKompetensiTercapai = draft.objectivesMet,
                catatan = draft.obstacles.trim(),
                tindakLanjut = draft.followUp.trim(),
                onSuccess = {
                    Toast.makeText(context, savedToast, Toast.LENGTH_LONG).show()
                    onNavigateBack()
                },
            )
        },
        onNavigateBack = onNavigateBack,
    )
}

/** The journal form without a ViewModel, for previews and screenshots. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalFormContent(
    schedule: JournalScheduleItem?,
    loading: Boolean,
    draft: JournalDraft,
    submitting: Boolean,
    errorMessage: UiText?,
    actions: JournalDraftActions,
    onRetry: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    initiallyShowErrors: Boolean = false,
) {
    // Required-field hints appear after the first attempt, not while the teacher is still typing.
    var showErrors by rememberSaveable { mutableStateOf(initiallyShowErrors) }
    val readOnly = schedule?.isFilled == true
    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = stringResource(if (readOnly) R.string.th_shortcut_journal else R.string.jf_title_fill),
                    subtitle = schedule?.let { "${it.subject} · ${stringResource(R.string.th_class, it.className)}" },
                    onBack = onNavigateBack,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            when {
                schedule == null && loading -> SkeletonList(Modifier.padding(padding), rows = 4)
                schedule == null -> ErrorState(
                    title = stringResource(if (errorMessage != null) R.string.th_schedule_error else R.string.jf_not_found),
                    body = errorMessage?.asString() ?: stringResource(R.string.jf_not_found_body),
                    onRetry = onRetry,
                    modifier = Modifier
                        .padding(padding)
                        .padding(Spacing.screen),
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.screen, vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    SlotSummary(schedule)
                    if (readOnly) {
                        InlineBanner(
                            message = stringResource(R.string.jf_readonly),
                            tone = StatusTone.Info,
                        )
                    }

                    FormSection(stringResource(R.string.jf_section_learning)) {
                        SistaTextField(
                            value = draft.topic,
                            onValueChange = actions.onTopic,
                            label = stringResource(R.string.jf_topic),
                            placeholder = stringResource(R.string.jf_topic_hint),
                            errorText = stringResource(R.string.jf_topic_required).takeIf { showErrors && draft.topicMissing },
                            singleLine = false,
                            minLines = 2,
                            enabled = !readOnly,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        )
                        Text(stringResource(R.string.jf_method), style = SistaTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            val options = (JOURNAL_METHODS.map { stringResource(it) } + draft.method).filter { it.isNotBlank() }.distinct()
                            options.forEach { method ->
                                val selected = draft.method == method
                                FilterChip(
                                    selected = selected,
                                    onClick = { actions.onMethod(method) },
                                    label = { Text(method) },
                                    enabled = !readOnly,
                                    leadingIcon = if (selected) ({ Icon(Icons.Outlined.Check, contentDescription = null) }) else null,
                                )
                            }
                        }
                        if (showErrors && draft.methodMissing) {
                            Text(stringResource(R.string.jf_method_required), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.error)
                        }
                        SistaTextField(
                            value = draft.activity,
                            onValueChange = actions.onActivity,
                            label = stringResource(R.string.jf_activity),
                            placeholder = stringResource(R.string.jf_activity_hint),
                            errorText = stringResource(R.string.jf_activity_required).takeIf { showErrors && draft.activityMissing },
                            singleLine = false,
                            minLines = 3,
                            enabled = !readOnly,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        )
                    }

                    FormSection(stringResource(R.string.jf_section_attendance)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            CountField(stringResource(R.string.jf_present), draft.present, actions.onPresent, showErrors && draft.presentInvalid, !readOnly, Modifier.weight(1f))
                            CountField(stringResource(R.string.jf_absent), draft.absent, actions.onAbsent, showErrors && draft.absentInvalid, !readOnly, Modifier.weight(1f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.jf_objectives), style = SistaTheme.typography.bodyLarge)
                                Text(
                                    stringResource(R.string.jf_objectives_hint),
                                    style = SistaTheme.typography.bodySmall,
                                    color = SistaTheme.colors.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.width(Spacing.md))
                            Switch(checked = draft.objectivesMet, onCheckedChange = actions.onObjectivesMet, enabled = !readOnly)
                        }
                    }

                    FormSection(stringResource(R.string.jf_section_notes)) {
                        SistaTextField(
                            value = draft.obstacles,
                            onValueChange = actions.onObstacles,
                            label = stringResource(R.string.jf_obstacles),
                            singleLine = false,
                            minLines = 2,
                            enabled = !readOnly,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        )
                        SistaTextField(
                            value = draft.followUp,
                            onValueChange = actions.onFollowUp,
                            label = stringResource(R.string.jf_follow_up),
                            placeholder = stringResource(R.string.jf_follow_up_hint),
                            singleLine = false,
                            minLines = 2,
                            enabled = !readOnly,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        )
                    }

                    if (errorMessage != null) InlineBanner(message = errorMessage.asString(), tone = StatusTone.Danger)
                    if (!readOnly) {
                        SistaButton(
                            text = stringResource(R.string.jf_save),
                            onClick = {
                                showErrors = true
                                if (draft.isComplete) onSubmit()
                            },
                            leadingIcon = Icons.Outlined.Save,
                            loading = submitting,
                            enabled = !submitting,
                            fullWidth = true,
                        )
                        Text(
                            stringResource(R.string.jf_footer),
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotSummary(schedule: JournalScheduleItem) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(schedule.subject, style = SistaTheme.typography.titleMedium)
            Text(
                listOfNotNull(
                    stringResource(R.string.th_class, schedule.className),
                    schedule.date?.let { journalDate(it) },
                    schedule.timeRange,
                    stringResource(R.string.tj_period, schedule.jamKe),
                ).joinToString(" · "),
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(title, style = SistaTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
        content()
    }
}

@Composable
private fun CountField(label: String, value: String, onChange: (String) -> Unit, error: Boolean, enabled: Boolean, modifier: Modifier) {
    SistaTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter(Char::isDigit).take(3)) },
        label = label,
        errorText = stringResource(R.string.jf_number).takeIf { error },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = modifier,
    )
}
