package com.sultanagung1.sista.ui.teacher

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.CbtImageAttachment
import com.sultanagung1.sista.data.model.TeacherCreatedExam
import com.sultanagung1.sista.ui.cbt.components.CbtImageViewer
import com.sultanagung1.sista.ui.cbt.components.CbtLatexMathView
import com.sultanagung1.sista.ui.cbt.components.CbtLatexToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.feature.teacher.R

/**
 * Teacher-authored online exam (ulangan harian), created on the server with
 * POST teacher/cbt/exams. Everything shown here is either typed by the teacher
 * or read from the server: subject/class come from the teacher's own schedule,
 * images are uploaded as soon as they're picked, and the entry token is never
 * made on the phone — the server issues it and the proctor room shows it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCreateExamScreen(
    onNavigateBack: () -> Unit,
    onExamCreated: (Long) -> Unit,
    viewModel: TeacherCreateExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLivePreview by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    // Which slot the gallery result belongs to. Saved as plain strings so it
    // survives the activity being recreated while the picker is open.
    var pendingQuestionId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingOptionKey by rememberSaveable { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val questionId = pendingQuestionId
        val optionKey = pendingOptionKey
        pendingQuestionId = null
        pendingOptionKey = null
        if (uri == null || questionId == null) return@rememberLauncherForActivityResult
        val target = if (optionKey == null) ImageTarget.Question(questionId) else ImageTarget.Option(questionId, optionKey)
        scope.launch {
            val attachment = withContext(Dispatchers.IO) { readImageAttachment(context, uri) }
            viewModel.attachImage(target, uri.toString(), attachment)
        }
    }
    val pickImageFor: (ImageTarget) -> Unit = { target ->
        pendingQuestionId = target.questionId
        pendingOptionKey = (target as? ImageTarget.Option)?.key
        imagePicker.launch("image/*")
    }

    // Leaving while the request is in flight would cancel it with the
    // ViewModel and leave the teacher unsure whether the exam exists.
    val requestLeave: () -> Unit = {
        when {
            uiState.isSubmitting -> Unit
            uiState.hasUnsavedWork -> showDiscardDialog = true
            else -> onNavigateBack()
        }
    }
    BackHandler(enabled = uiState.isSubmitting || uiState.hasUnsavedWork) { requestLeave() }

    LaunchedEffect(uiState.imageError) {
        uiState.imageError?.let { message ->
            snackbarHostState.showSnackbar(message.resolve(context))
            viewModel.onImageErrorShown()
        }
    }

    Scaffold(
        topBar = {
            SistaTopBar(
                title = stringResource(R.string.ce_title),
                subtitle = stringResource(R.string.ce_subtitle),
                onBack = requestLeave
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        TeacherCreateExamForm(
            uiState = uiState,
            showLivePreview = showLivePreview,
            onToggleLivePreview = { showLivePreview = !showLivePreview },
            actions = CreateExamActions(
                onTitleChange = viewModel::updateTitle,
                onChoiceSelected = viewModel::selectChoice,
                onRetryChoices = viewModel::loadClassChoices,
                onDurationChange = viewModel::updateDuration,
                onPassingScoreChange = viewModel::updatePassingScore,
                onMobileOnlyChange = viewModel::setMobileOnly,
                onMaxViolationsChange = viewModel::setMaxViolations,
                onShuffleQuestionsChange = viewModel::setShuffleQuestions,
                onShuffleOptionsChange = viewModel::setShuffleOptions,
                onSelectQuestion = viewModel::selectQuestion,
                onAddQuestion = viewModel::addQuestion,
                onRemoveQuestion = viewModel::removeQuestion,
                onQuestionTextChange = viewModel::updateQuestionText,
                onCorrectKeySelected = viewModel::setCorrectKey,
                onOptionTextChange = viewModel::updateOptionText,
                onAddOption = viewModel::addOption,
                onRemoveLastOption = viewModel::removeLastOption,
                onPickImage = pickImageFor,
                onRetryImage = viewModel::retryImageUpload,
                onRemoveImage = viewModel::removeImage,
                onPublish = viewModel::publish,
                onDismissSubmitError = viewModel::dismissSubmitError,
            ),
            modifier = Modifier.padding(paddingValues),
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = { Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AccentAmber) },
            title = { Text(stringResource(R.string.ce_discard_title)) },
            text = { Text(stringResource(R.string.ce_discard_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRose)
                ) { Text(stringResource(R.string.ce_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text(stringResource(R.string.ce_keep_editing)) }
            }
        )
    }

    uiState.createdExam?.let { exam ->
        ExamCreatedDialog(
            exam = exam,
            onOpenProctor = { onExamCreated(exam.examId) },
            onDone = onNavigateBack
        )
    }
}

/** Every edit the form can make; the screen wires them to [TeacherCreateExamViewModel]. */
data class CreateExamActions(
    val onTitleChange: (String) -> Unit = {},
    val onChoiceSelected: (ExamClassChoice) -> Unit = {},
    val onRetryChoices: () -> Unit = {},
    val onDurationChange: (String) -> Unit = {},
    val onPassingScoreChange: (String) -> Unit = {},
    val onMobileOnlyChange: (Boolean) -> Unit = {},
    val onMaxViolationsChange: (Int) -> Unit = {},
    val onShuffleQuestionsChange: (Boolean) -> Unit = {},
    val onShuffleOptionsChange: (Boolean) -> Unit = {},
    val onSelectQuestion: (Int) -> Unit = {},
    val onAddQuestion: () -> Unit = {},
    val onRemoveQuestion: (Int) -> Unit = {},
    val onQuestionTextChange: (String, String) -> Unit = { _, _ -> },
    val onCorrectKeySelected: (String, String) -> Unit = { _, _ -> },
    val onOptionTextChange: (String, String, String) -> Unit = { _, _, _ -> },
    val onAddOption: (String) -> Unit = {},
    val onRemoveLastOption: (String) -> Unit = {},
    val onPickImage: (ImageTarget) -> Unit = {},
    val onRetryImage: (ImageTarget) -> Unit = {},
    val onRemoveImage: (ImageTarget) -> Unit = {},
    val onPublish: () -> Unit = {},
    val onDismissSubmitError: () -> Unit = {},
)

/** The exam form without a ViewModel or top bar, for previews and screenshots. */
@Composable
fun TeacherCreateExamForm(
    uiState: TeacherCreateExamUiState,
    showLivePreview: Boolean,
    onToggleLivePreview: () -> Unit,
    actions: CreateExamActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExamInfoSection(
            uiState = uiState,
            onTitleChange = actions.onTitleChange,
            onChoiceSelected = actions.onChoiceSelected,
            onRetryChoices = actions.onRetryChoices,
            onDurationChange = actions.onDurationChange,
            onPassingScoreChange = actions.onPassingScoreChange
        )

        ExamRulesSection(
            uiState = uiState,
            onMobileOnlyChange = actions.onMobileOnlyChange,
            onMaxViolationsChange = actions.onMaxViolationsChange,
            onShuffleQuestionsChange = actions.onShuffleQuestionsChange,
            onShuffleOptionsChange = actions.onShuffleOptionsChange
        )

        QuestionsSection(
            uiState = uiState,
            showLivePreview = showLivePreview,
            onToggleLivePreview = onToggleLivePreview,
            onSelectQuestion = actions.onSelectQuestion,
            onAddQuestion = actions.onAddQuestion,
            onRemoveQuestion = actions.onRemoveQuestion,
            onQuestionTextChange = actions.onQuestionTextChange,
            onCorrectKeySelected = actions.onCorrectKeySelected,
            onOptionTextChange = actions.onOptionTextChange,
            onAddOption = actions.onAddOption,
            onRemoveLastOption = actions.onRemoveLastOption,
            onPickImage = actions.onPickImage,
            onRetryImage = actions.onRetryImage,
            onRemoveImage = actions.onRemoveImage
        )

        PublishSection(
            uiState = uiState,
            onPublish = actions.onPublish,
            onDismissSubmitError = actions.onDismissSubmitError,
            onIssueClick = actions.onSelectQuestion
        )
    }
}

// --- Section 1: exam info ------------------------------------------------------

@Composable
private fun ExamInfoSection(
    uiState: TeacherCreateExamUiState,
    onTitleChange: (String) -> Unit,
    onChoiceSelected: (ExamClassChoice) -> Unit,
    onRetryChoices: () -> Unit,
    onDurationChange: (String) -> Unit,
    onPassingScoreChange: (String) -> Unit
) {
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Default.Assignment, title = stringResource(R.string.ce_section_info))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.ce_name)) },
            placeholder = { Text(stringResource(R.string.ce_name_hint)) },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        when {
            uiState.isLoadingChoices -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Emerald700)
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.ce_loading_schedule), style = MaterialTheme.typography.bodySmall, color = Slate600)
            }

            uiState.choicesError != null -> SulaoneErrorBanner(
                message = stringResource(R.string.ce_schedule_failed, uiState.choicesError),
                onRetry = onRetryChoices
            )

            uiState.classChoices.isEmpty() -> InfoNote(
                icon = Icons.Default.EventBusy,
                text = stringResource(R.string.ce_no_schedule),
                tint = AccentAmber
            )

            else -> {
                val labels = choiceLabels(uiState.classChoices.map { classChoiceLabel(it) }, uiState.classChoices)
                SulaoneDropdown(
                    selectedValue = uiState.selectedChoice
                        ?.let { selected -> labels.getOrNull(uiState.classChoices.indexOf(selected)) }
                        .orEmpty(),
                    onValueSelected = { label ->
                        uiState.classChoices.getOrNull(labels.indexOf(label))?.let(onChoiceSelected)
                    },
                    options = labels,
                    label = stringResource(R.string.ce_class),
                    placeholder = stringResource(R.string.ce_class_hint),
                    enabled = !uiState.isSubmitting,
                    searchEnabled = labels.size > 6
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.durationMinutes,
                onValueChange = onDurationChange,
                label = { Text(stringResource(R.string.ce_duration)) },
                placeholder = { Text("${TeacherCreateExamViewModel.MIN_DURATION}–${TeacherCreateExamViewModel.MAX_DURATION}") },
                singleLine = true,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = uiState.passingScore,
                onValueChange = onPassingScoreChange,
                label = { Text(stringResource(R.string.ce_passing)) },
                placeholder = { Text("0–100") },
                singleLine = true,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        InfoNote(
            icon = Icons.Default.Schedule,
            text = stringResource(R.string.ce_info_note)
        )
    }
}

// --- Section 2: rules the server actually enforces -----------------------------

@Composable
private fun ExamRulesSection(
    uiState: TeacherCreateExamUiState,
    onMobileOnlyChange: (Boolean) -> Unit,
    onMaxViolationsChange: (Int) -> Unit,
    onShuffleQuestionsChange: (Boolean) -> Unit,
    onShuffleOptionsChange: (Boolean) -> Unit
) {
    val enabled = !uiState.isSubmitting
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Default.Security, title = stringResource(R.string.ce_section_rules), tint = AccentRose)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow(
            title = stringResource(R.string.ce_mobile_only),
            description = stringResource(R.string.ce_mobile_only_body),
            checked = uiState.mobileOnly,
            enabled = enabled,
            onCheckedChange = onMobileOnlyChange
        )

        SettingsDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ce_violations), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    stringResource(R.string.ce_violations_body),
                    fontSize = 11.sp,
                    color = Slate600
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onMaxViolationsChange(uiState.maxViolations - 1) },
                enabled = enabled && uiState.maxViolations > TeacherCreateExamViewModel.MIN_VIOLATIONS
            ) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = stringResource(R.string.ce_violations_less)) }
            Text(
                text = stringResource(R.string.ce_violations_value, uiState.maxViolations),
                style = MaterialTheme.typography.titleMedium.emphasized(),
                color = Emerald800,
                modifier = Modifier.widthIn(min = 28.dp)
            )
            IconButton(
                onClick = { onMaxViolationsChange(uiState.maxViolations + 1) },
                enabled = enabled && uiState.maxViolations < TeacherCreateExamViewModel.MAX_VIOLATIONS
            ) { Icon(Icons.Default.AddCircleOutline, contentDescription = stringResource(R.string.ce_violations_more)) }
        }

        SettingsDivider()

        // Both the web take view and the student app API
        // (ApiStudentController::cbtExamQuestions) honor these flags with the
        // same per-student seeded order, so they apply to app-only exams too.
        SettingSwitchRow(
            title = stringResource(R.string.ce_shuffle_questions),
            description = stringResource(R.string.ce_shuffle_questions_body),
            checked = uiState.shuffleQuestions,
            enabled = enabled,
            onCheckedChange = onShuffleQuestionsChange
        )
        SettingsDivider()
        SettingSwitchRow(
            title = stringResource(R.string.ce_shuffle_options),
            description = stringResource(R.string.ce_shuffle_options_body),
            checked = uiState.shuffleOptions,
            enabled = enabled,
            onCheckedChange = onShuffleOptionsChange
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoNote(
            icon = Icons.Default.Shield,
            text = stringResource(R.string.ce_security_note)
        )
    }
}

// --- Section 3: questions --------------------------------------------------------

@Composable
private fun QuestionsSection(
    uiState: TeacherCreateExamUiState,
    showLivePreview: Boolean,
    onToggleLivePreview: () -> Unit,
    onSelectQuestion: (Int) -> Unit,
    onAddQuestion: () -> Unit,
    onRemoveQuestion: (Int) -> Unit,
    onQuestionTextChange: (String, String) -> Unit,
    onCorrectKeySelected: (String, String) -> Unit,
    onOptionTextChange: (String, String, String) -> Unit,
    onAddOption: (String) -> Unit,
    onRemoveLastOption: (String) -> Unit,
    onPickImage: (ImageTarget) -> Unit,
    onRetryImage: (ImageTarget) -> Unit,
    onRemoveImage: (ImageTarget) -> Unit
) {
    val enabled = !uiState.isSubmitting
    val issueIndexes = uiState.questionsWithIssues

    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(icon = Icons.Default.EditNote, title = stringResource(R.string.ce_section_questions))
                Text(
                    text = stringResource(R.string.ce_questions_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = showLivePreview,
                onClick = onToggleLivePreview,
                label = { Text(stringResource(R.string.ce_preview), fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = if (showLivePreview) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(uiState.questions, key = { _, q -> q.localId }) { index, _ ->
                val isSelected = index == uiState.activeQuestionIndex
                val hasIssue = index in issueIndexes
                val container = when {
                    isSelected && hasIssue -> AccentRose
                    isSelected -> Emerald700
                    hasIssue -> AccentRose.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val content = when {
                    isSelected -> Color.White
                    hasIssue -> AccentRose
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(container)
                        .then(if (hasIssue && !isSelected) Modifier.border(1.dp, AccentRose, RoundedCornerShape(10.dp)) else Modifier)
                        .clickable { onSelectQuestion(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = content
                    )
                }
            }

            if (uiState.questions.size < TeacherCreateExamViewModel.MAX_QUESTIONS) {
                item(key = "add-question") {
                    FilledTonalButton(
                        onClick = onAddQuestion,
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.ce_add_question), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        val index = uiState.activeQuestionIndex
        val question = uiState.questions.getOrNull(index) ?: return@SulaoneCard

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ce_question_of, index + 1, uiState.questions.size),
                style = MaterialTheme.typography.titleSmall.emphasized(),
                color = Emerald800
            )
            if (uiState.questions.size > 1) {
                TextButton(
                    onClick = { onRemoveQuestion(index) },
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRose)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.ce_remove_question), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = question.text,
            onValueChange = { onQuestionTextChange(question.localId, it) },
            label = { Text(stringResource(R.string.ce_question_text)) },
            placeholder = { Text(stringResource(R.string.ce_question_hint)) },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CbtLatexToolbar(
            onInsertSnippet = { snippet ->
                val current = question.text
                val separator = if (current.isEmpty() || current.endsWith(" ") || current.endsWith("\n")) "" else " "
                onQuestionTextChange(question.localId, "$current$separator\$\$$snippet\$\$ ")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        val questionTarget = ImageTarget.Question(question.localId)
        if (question.image == null) {
            OutlinedButton(
                onClick = { onPickImage(questionTarget) },
                enabled = enabled,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald700)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ce_attach_question_image), fontSize = 12.sp, color = Emerald700)
            }
        } else {
            DraftImageCard(
                image = question.image,
                previewHeight = 160.dp,
                enabled = enabled,
                onRetry = { onRetryImage(questionTarget) },
                onReplace = { onPickImage(questionTarget) },
                onRemove = { onRemoveImage(questionTarget) }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Text(
            text = stringResource(R.string.ce_choices, question.options.first().key, question.options.last().key),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Emerald800
        )
        Text(
            text = if (question.correctKey == null) {
                stringResource(R.string.ce_key_missing)
            } else {
                stringResource(R.string.ce_key_set, question.correctKey)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (question.correctKey == null) AccentAmber else Slate600
        )

        Spacer(modifier = Modifier.height(10.dp))

        question.options.forEach { option ->
            val isCorrect = question.correctKey == option.key
            val optionTarget = ImageTarget.Option(question.localId, option.key)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCorrect) Emerald50 else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isCorrect) 1.5.dp else 1.dp,
                    color = if (isCorrect) Emerald700 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isCorrect) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = enabled) { onCorrectKeySelected(question.localId, option.key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCorrect) {
                                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ce_key_of, option.key), tint = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text(
                                    text = option.key,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedTextField(
                            value = option.text,
                            onValueChange = { onOptionTextChange(question.localId, option.key, it) },
                            placeholder = { Text(stringResource(R.string.ce_choice, option.key)) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (option.image == null) {
                            IconButton(onClick = { onPickImage(optionTarget) }, enabled = enabled) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = stringResource(R.string.ce_attach_choice_image, option.key),
                                    tint = Slate600
                                )
                            }
                        }
                    }

                    option.image?.let { image ->
                        Spacer(modifier = Modifier.height(6.dp))
                        DraftImageCard(
                            image = image,
                            previewHeight = 90.dp,
                            enabled = enabled,
                            onRetry = { onRetryImage(optionTarget) },
                            onReplace = { onPickImage(optionTarget) },
                            onRemove = { onRemoveImage(optionTarget) },
                            modifier = Modifier.padding(start = 46.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (question.options.size < TeacherCreateExamViewModel.OPTION_KEYS.size) {
                TextButton(onClick = { onAddOption(question.localId) }, enabled = enabled) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.ce_choice, TeacherCreateExamViewModel.OPTION_KEYS[question.options.size]), fontSize = 12.sp)
                }
            }
            if (question.options.size > TeacherCreateExamViewModel.MIN_OPTIONS) {
                TextButton(
                    onClick = { onRemoveLastOption(question.localId) },
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRose)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.ce_remove_choice, question.options.last().key), fontSize = 12.sp)
                }
            }
        }

        if (showLivePreview) {
            Spacer(modifier = Modifier.height(12.dp))
            QuestionPreview(question = question)
        }
    }
}

/** Renders the draft the way the student exam room will (KaTeX + images); the key mark is teacher-only. */
@Composable
private fun QuestionPreview(question: DraftQuestion) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Emerald700.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.ce_preview_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (question.text.isBlank()) {
                Text(stringResource(R.string.ce_preview_empty), style = MaterialTheme.typography.bodyMedium, color = Slate500)
            } else {
                CbtLatexMathView(text = question.text, style = MaterialTheme.typography.bodyLarge)
            }
            question.image?.let {
                Spacer(modifier = Modifier.height(10.dp))
                CbtImageViewer(imageUrl = it.previewUri, maxHeight = 180.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            question.options.forEach { opt ->
                val isCorrect = question.correctKey == opt.key
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCorrect) Emerald100.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${opt.key}.",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Emerald900 else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        if (opt.text.isNotBlank()) {
                            CbtLatexMathView(
                                text = opt.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCorrect) Emerald900 else MaterialTheme.colorScheme.onSurface
                            )
                        } else if (opt.image == null) {
                            Text("—", color = Slate500)
                        }
                        opt.image?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            CbtImageViewer(imageUrl = it.previewUri, maxHeight = 70.dp)
                        }
                    }
                    if (isCorrect) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ce_key), tint = Emerald700, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/** A picked image with its real upload state: uploading, uploaded, or failed (retryable). */
@Composable
private fun DraftImageCard(
    image: DraftImage,
    previewHeight: androidx.compose.ui.unit.Dp,
    enabled: Boolean,
    onRetry: () -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CbtImageViewer(imageUrl = image.previewUri, maxHeight = previewHeight, allowZoom = true)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                image.isUploading -> {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Emerald700)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ce_uploading), style = MaterialTheme.typography.labelSmall, color = Slate600, modifier = Modifier.weight(1f))
                }
                image.error != null -> {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.ce_upload_failed, image.error),
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentRose,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry, enabled = enabled) { Text(stringResource(R.string.ce_retry), fontSize = 12.sp) }
                }
                else -> {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ce_uploaded), style = MaterialTheme.typography.labelSmall, color = AccentGreen, modifier = Modifier.weight(1f))
                }
            }
            if (!image.isUploading) {
                IconButton(onClick = onReplace, enabled = enabled) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = stringResource(R.string.ce_replace_image), tint = Slate600)
                }
            }
            IconButton(onClick = onRemove, enabled = enabled) {
                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.ce_remove_image), tint = AccentRose)
            }
        }
    }
}

// --- Publish ---------------------------------------------------------------------

@Composable
private fun PublishSection(
    uiState: TeacherCreateExamUiState,
    onPublish: () -> Unit,
    onDismissSubmitError: () -> Unit,
    onIssueClick: (Int) -> Unit
) {
    val issues = uiState.validationIssues

    if (issues.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AccentRose.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(R.string.ce_fix_count, issues.size),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentRose
                )
                Spacer(modifier = Modifier.height(6.dp))
                issues.forEach { issue ->
                    val questionIndex = issue.questionIndex
                    Text(
                        text = "• ${issue.message.asString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (questionIndex != null) {
                                    Modifier
                                        .minimumInteractiveComponentSize()
                                        .clickable { onIssueClick(questionIndex) }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }

    uiState.submitError?.let { message ->
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AccentRose.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.ce_not_published), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AccentRose)
                    Text(message, style = MaterialTheme.typography.bodySmall, color = AccentRose)
                }
                IconButton(onClick = onDismissSubmitError, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ce_close_message), tint = AccentRose, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    val questionCount = uiState.questions.size
    val duration = uiState.durationMinutes.ifBlank { "–" }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Emerald50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(stringResource(R.string.ce_summary), style = MaterialTheme.typography.labelSmall, color = Slate600)
            Text(
                text = stringResource(R.string.ce_summary_line, questionCount, duration, uiState.selectedChoice?.let { classChoiceLabel(it) } ?: stringResource(R.string.ce_no_class)),
                fontWeight = FontWeight.Bold,
                color = Emerald900
            )
        }
    }

    Button(
        onClick = onPublish,
        enabled = !uiState.isSubmitting && !uiState.isUploadingAnyImage,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
    ) {
        when {
            uiState.isSubmitting -> {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.ce_publishing), fontWeight = FontWeight.Bold)
            }
            uiState.isUploadingAnyImage -> Text(stringResource(R.string.ce_waiting_images), fontWeight = FontWeight.Bold)
            else -> {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.ce_publish), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Built only from the server's 201 response — nothing here is assumed. */
@Composable
private fun ExamCreatedDialog(
    exam: TeacherCreatedExam,
    onOpenProctor: () -> Unit,
    onDone: () -> Unit
) {
    AlertDialog(
        // The exam already exists on the server; make the teacher pick where to go next.
        onDismissRequest = {},
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(44.dp)) },
        title = { Text(stringResource(R.string.ce_published)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(exam.title, fontWeight = FontWeight.Bold)
                listOfNotNull(exam.subject, exam.classroom).takeIf { it.isNotEmpty() }?.let {
                    Text(it.joinToString(" • "), color = Slate600)
                }
                Text(stringResource(R.string.ce_published_meta, exam.totalQuestions, exam.durationMinutes), color = Slate600)
                ProctorFormat.examTime(exam.endTime)?.let {
                    Text(stringResource(R.string.ce_available_until, it), color = Slate600)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (exam.canViewToken) {
                        stringResource(R.string.ce_token_note)
                    } else {
                        stringResource(R.string.ce_token_not_allowed)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            if (exam.canViewToken) {
                Button(
                    onClick = onOpenProctor,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) { Text(stringResource(R.string.ce_open_proctor)) }
            } else {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) { Text(stringResource(R.string.ce_done)) }
            }
        },
        dismissButton = if (exam.canViewToken) {
            { TextButton(onClick = onDone) { Text(stringResource(R.string.ce_done)) } }
        } else {
            null
        }
    )
}

// --- Small building blocks ---------------------------------------------------------

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = Emerald700
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.emphasized(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(description, fontSize = 11.sp, color = Slate600)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun InfoNote(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color = Emerald700
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- Helpers -------------------------------------------------------------------------

/**
 * Dropdown labels, one per choice. Two schedule rows can carry the same
 * subject and class names under different ids; those get their ids appended
 * so every label maps back to exactly one choice.
 */
private fun choiceLabels(labels: List<String>, choices: List<ExamClassChoice>): List<String> {
    val duplicated = labels.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    return labels.mapIndexed { index, label ->
        val choice = choices[index]
        if (label in duplicated) "$label (#${choice.subjectId}/#${choice.classroomId})" else label
    }
}

/** "Fisika · XI MIPA 2"; a name the schedule left empty is shown by its id. */
@Composable
private fun classChoiceLabel(choice: ExamClassChoice): String =
    listOf(
        choice.subjectName ?: stringResource(R.string.ce_subject_number, choice.subjectId),
        choice.classroomName ?: stringResource(R.string.ce_class_number, choice.classroomId),
    ).joinToString(" · ")

/**
 * Reads a picked image off the main thread. Stops one byte past the server's
 * limit so an oversized file is rejected without being loaded whole; an
 * unreadable file comes back empty and is rejected with a clear message by
 * [TeacherCreateExamViewModel.imageRejectionReason].
 */
private fun readImageAttachment(context: Context, uri: Uri): CbtImageAttachment {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri).orEmpty()
    val bytes = try {
        resolver.openInputStream(uri)?.use { input ->
            val limit = TeacherCreateExamViewModel.MAX_IMAGE_BYTES + 1
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (total < limit) {
                val read = input.read(buffer, 0, minOf(buffer.size.toLong(), limit - total).toInt())
                if (read < 0) break
                out.write(buffer, 0, read)
                total += read
            }
            out.toByteArray()
        } ?: ByteArray(0)
    } catch (e: Exception) {
        ByteArray(0)
    }
    val extension = when (mimeType.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }
    return CbtImageAttachment(bytes = bytes, mimeType = mimeType, fileName = "soal-${System.currentTimeMillis()}.$extension")
}
