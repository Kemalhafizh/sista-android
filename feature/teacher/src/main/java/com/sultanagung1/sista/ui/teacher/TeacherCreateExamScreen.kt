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
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            snackbarHostState.showSnackbar(message)
            viewModel.onImageErrorShown()
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Buat Ulangan Daring",
                subtitle = "Soal pilihan ganda • diterbitkan ke server",
                onNavigateBack = requestLeave
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExamInfoSection(
                uiState = uiState,
                onTitleChange = viewModel::updateTitle,
                onChoiceSelected = viewModel::selectChoice,
                onRetryChoices = viewModel::loadClassChoices,
                onDurationChange = viewModel::updateDuration,
                onPassingScoreChange = viewModel::updatePassingScore
            )

            ExamRulesSection(
                uiState = uiState,
                onMobileOnlyChange = viewModel::setMobileOnly,
                onMaxViolationsChange = viewModel::setMaxViolations,
                onShuffleQuestionsChange = viewModel::setShuffleQuestions,
                onShuffleOptionsChange = viewModel::setShuffleOptions
            )

            QuestionsSection(
                uiState = uiState,
                showLivePreview = showLivePreview,
                onToggleLivePreview = { showLivePreview = !showLivePreview },
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
                onRemoveImage = viewModel::removeImage
            )

            PublishSection(
                uiState = uiState,
                onPublish = viewModel::publish,
                onDismissSubmitError = viewModel::dismissSubmitError,
                onIssueClick = { index -> viewModel.selectQuestion(index) }
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = { Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AccentAmber) },
            title = { Text("Buang draf ujian?") },
            text = { Text("Ujian ini belum diterbitkan. Soal dan pengaturan yang sudah Anda isi akan hilang.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRose)
                ) { Text("Buang Draf") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Lanjut Menyusun") }
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
        SectionHeader(icon = Icons.Default.Assignment, title = "1. Informasi Ujian")
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Nama Ujian") },
            placeholder = { Text("Contoh: Ulangan Harian Bab 3") },
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
                Text("Memuat jadwal mengajar Anda…", style = MaterialTheme.typography.bodySmall, color = Slate600)
            }

            uiState.choicesError != null -> SulaoneErrorBanner(
                message = "Jadwal mengajar gagal dimuat: ${uiState.choicesError}",
                onRetry = onRetryChoices
            )

            uiState.classChoices.isEmpty() -> InfoNote(
                icon = Icons.Default.EventBusy,
                text = "Belum ada jadwal mengajar yang tercatat untuk akun Anda, jadi mata pelajaran & kelas " +
                    "belum bisa dipilih. Minta bagian kurikulum menambahkan jadwal Anda, lalu buka layar ini lagi.",
                tint = AccentAmber
            )

            else -> {
                val labels = remember(uiState.classChoices) { choiceLabels(uiState.classChoices) }
                SulaoneDropdown(
                    selectedValue = uiState.selectedChoice
                        ?.let { selected -> labels.getOrNull(uiState.classChoices.indexOf(selected)) }
                        .orEmpty(),
                    onValueSelected = { label ->
                        uiState.classChoices.getOrNull(labels.indexOf(label))?.let(onChoiceSelected)
                    },
                    options = labels,
                    label = "Mata Pelajaran & Kelas",
                    placeholder = "Pilih dari jadwal mengajar Anda",
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
                label = { Text("Durasi (menit)") },
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
                label = { Text("KKTP") },
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
            text = "Ujian langsung dibuka setelah diterbitkan dan tersedia selama 7 hari. Token masuk " +
                "diterbitkan oleh server dan berganti tiap 5 menit — tampilkan dari Ruang Pengawas."
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
        SectionHeader(icon = Icons.Default.Security, title = "2. Aturan Pengerjaan", tint = AccentRose)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow(
            title = "Khusus aplikasi HP",
            description = "Siswa hanya bisa mengerjakan lewat aplikasi Sulaone; akses dari browser ditolak server.",
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
                Text("Batas pelanggaran", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Keluar dari aplikasi atau membuka split-screen dihitung pelanggaran. Saat batas ini " +
                        "tercapai, ujian siswa dihentikan otomatis.",
                    fontSize = 11.sp,
                    color = Slate600
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onMaxViolationsChange(uiState.maxViolations - 1) },
                enabled = enabled && uiState.maxViolations > TeacherCreateExamViewModel.MIN_VIOLATIONS
            ) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Kurangi batas pelanggaran") }
            Text(
                text = "${uiState.maxViolations}×",
                style = MaterialTheme.typography.titleMedium.emphasized(),
                color = Emerald800,
                modifier = Modifier.widthIn(min = 28.dp)
            )
            IconButton(
                onClick = { onMaxViolationsChange(uiState.maxViolations + 1) },
                enabled = enabled && uiState.maxViolations < TeacherCreateExamViewModel.MAX_VIOLATIONS
            ) { Icon(Icons.Default.AddCircleOutline, contentDescription = "Tambah batas pelanggaran") }
        }

        SettingsDivider()

        // Both the web take view and the student app API
        // (ApiStudentController::cbtExamQuestions) honor these flags with the
        // same per-student seeded order, so they apply to app-only exams too.
        SettingSwitchRow(
            title = "Acak urutan soal",
            description = "Tiap siswa mendapat urutan berbeda; urutannya tetap sama bila ujian dibuka ulang.",
            checked = uiState.shuffleQuestions,
            enabled = enabled,
            onCheckedChange = onShuffleQuestionsChange
        )
        SettingsDivider()
        SettingSwitchRow(
            title = "Acak urutan pilihan jawaban",
            description = "Posisi pilihan diacak per siswa; huruf A–E tetap melekat pada pilihan aslinya, " +
                "jadi kunci jawaban tidak berubah.",
            checked = uiState.shuffleOptions,
            enabled = enabled,
            onCheckedChange = onShuffleOptionsChange
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoNote(
            icon = Icons.Default.Shield,
            text = "Di aplikasi siswa, blokir tangkapan layar, penguncian layar ujian, deteksi split-screen, " +
                "serta pemeriksaan root/emulator selalu aktif untuk setiap ujian."
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
                SectionHeader(icon = Icons.Default.EditNote, title = "3. Soal Pilihan Ganda")
                Text(
                    text = "Gunakan \$\$…\$\$ untuk rumus. Gambar diunggah ke server begitu dipilih.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = showLivePreview,
                onClick = onToggleLivePreview,
                label = { Text("Pratinjau", fontSize = 12.sp) },
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
                        Text("Soal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                text = "Soal #${index + 1} dari ${uiState.questions.size}",
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
                    Text("Hapus Soal", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = question.text,
            onValueChange = { onQuestionTextChange(question.localId, it) },
            label = { Text("Teks pertanyaan") },
            placeholder = { Text("Tulis pertanyaan di sini") },
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
                Text("Lampirkan gambar soal (opsional)", fontSize = 12.sp, color = Emerald700)
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
            text = "Pilihan jawaban (${question.options.first().key}–${question.options.last().key})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Emerald800
        )
        Text(
            text = if (question.correctKey == null) {
                "Ketuk huruf pilihan untuk menandai kunci jawaban — belum ada yang ditandai."
            } else {
                "Kunci jawaban: ${question.correctKey}. Ketuk huruf lain untuk mengganti."
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
                                Icon(Icons.Default.Check, contentDescription = "Kunci jawaban ${option.key}", tint = Color.White, modifier = Modifier.size(18.dp))
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
                            placeholder = { Text("Pilihan ${option.key}") },
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (option.image == null) {
                            IconButton(onClick = { onPickImage(optionTarget) }, enabled = enabled) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Lampirkan gambar pilihan ${option.key}",
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
                    Text("Pilihan ${TeacherCreateExamViewModel.OPTION_KEYS[question.options.size]}", fontSize = 12.sp)
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
                    Text("Hapus pilihan ${question.options.last().key}", fontSize = 12.sp)
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
                    text = "Pratinjau tampilan soal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (question.text.isBlank()) {
                Text("(Teks pertanyaan masih kosong)", style = MaterialTheme.typography.bodyMedium, color = Slate500)
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
                        Icon(Icons.Default.Check, contentDescription = "Kunci jawaban", tint = Emerald700, modifier = Modifier.size(16.dp))
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
                    Text("Mengunggah gambar…", style = MaterialTheme.typography.labelSmall, color = Slate600, modifier = Modifier.weight(1f))
                }
                image.error != null -> {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gagal diunggah: ${image.error}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentRose,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry, enabled = enabled) { Text("Coba Lagi", fontSize = 12.sp) }
                }
                else -> {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tersimpan di server", style = MaterialTheme.typography.labelSmall, color = AccentGreen, modifier = Modifier.weight(1f))
                }
            }
            if (!image.isUploading) {
                IconButton(onClick = onReplace, enabled = enabled) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Ganti gambar", tint = Slate600)
                }
            }
            IconButton(onClick = onRemove, enabled = enabled) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus gambar", tint = AccentRose)
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
                    text = "Perbaiki ${issues.size} hal sebelum menerbitkan:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentRose
                )
                Spacer(modifier = Modifier.height(6.dp))
                issues.forEach { issue ->
                    val questionIndex = issue.questionIndex
                    Text(
                        text = "• ${issue.message}",
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
                    Text("Ujian belum diterbitkan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AccentRose)
                    Text(message, style = MaterialTheme.typography.bodySmall, color = AccentRose)
                }
                IconButton(onClick = onDismissSubmitError, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup pesan", tint = AccentRose, modifier = Modifier.size(18.dp))
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
            Text("Ringkasan", style = MaterialTheme.typography.labelSmall, color = Slate600)
            Text(
                text = "$questionCount soal • $duration menit • ${uiState.selectedChoice?.label ?: "kelas belum dipilih"}",
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
                Text("Menerbitkan ke server…", fontWeight = FontWeight.Bold)
            }
            uiState.isUploadingAnyImage -> Text("Menunggu gambar selesai diunggah…", fontWeight = FontWeight.Bold)
            else -> {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terbitkan Ujian", fontWeight = FontWeight.Bold)
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
        title = { Text("Ujian diterbitkan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(exam.title, fontWeight = FontWeight.Bold)
                listOfNotNull(exam.subject, exam.classroom).takeIf { it.isNotEmpty() }?.let {
                    Text(it.joinToString(" • "), color = Slate600)
                }
                Text("${exam.totalQuestions} soal • ${exam.durationMinutes} menit", color = Slate600)
                formatCreatedExamTime(exam.endTime)?.let {
                    Text("Dapat dikerjakan sampai $it", color = Slate600)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (exam.canViewToken) {
                        "Token masuk diterbitkan server dan berganti tiap 5 menit. Buka Ruang Pengawas untuk menampilkannya kepada siswa."
                    } else {
                        "Akun Anda tidak berwenang melihat token ujian ini. Token dibagikan oleh pengawas yang ditunjuk."
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
                ) { Text("Buka Ruang Pengawas") }
            } else {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) { Text("Selesai") }
            }
        },
        dismissButton = if (exam.canViewToken) {
            { TextButton(onClick = onDone) { Text("Selesai") } }
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
private fun choiceLabels(choices: List<ExamClassChoice>): List<String> {
    val duplicated = choices.groupingBy { it.label }.eachCount().filterValues { it > 1 }.keys
    return choices.map { choice ->
        if (choice.label in duplicated) "${choice.label} (#${choice.subjectId}/#${choice.classroomId})" else choice.label
    }
}

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

private val createdExamTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy • HH:mm", Locale("id", "ID"))

private fun formatCreatedExamTime(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(createdExamTimeFormatter)
    } catch (e: Exception) {
        null
    }
}
