package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.CbtImageAttachment
import com.sultanagung1.sista.data.model.CbtOptionItem
import com.sultanagung1.sista.data.model.TeacherCreateExamRequest
import com.sultanagung1.sista.data.model.TeacherCreatedExam
import com.sultanagung1.sista.data.model.TeacherQuestionPayload
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.repository.CbtRepository
import com.sultanagung1.sista.data.repository.TeachingJournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** A subject + class pair the teacher actually teaches (from GET teacher/schedule). */
data class ExamClassChoice(
    val subjectId: Long,
    val subjectName: String,
    val classroomId: Long,
    val classroomName: String
) {
    val label: String get() = "$subjectName • $classroomName"
}

/**
 * A picked image. [previewUri] is the local content:// URI shown immediately;
 * [remoteUrl] is the http(s) URL returned by POST teacher/cbt/upload-image —
 * only that one is ever sent in the exam payload (the server rejects local paths).
 */
data class DraftImage(
    val previewUri: String,
    val remoteUrl: String? = null,
    val isUploading: Boolean = false,
    val error: String? = null
)

data class DraftOption(
    val key: String,
    val text: String = "",
    val image: DraftImage? = null
)

data class DraftQuestion(
    val localId: String = UUID.randomUUID().toString(),
    val text: String = "",
    val image: DraftImage? = null,
    val options: List<DraftOption> = TeacherCreateExamViewModel.OPTION_KEYS.map { DraftOption(it) },
    // Deliberately no default: the old screen pre-marked "A", so a teacher who
    // forgot to set the key silently published a wrong answer key.
    val correctKey: String? = null
)

sealed interface ImageTarget {
    val questionId: String

    data class Question(override val questionId: String) : ImageTarget
    data class Option(override val questionId: String, val key: String) : ImageTarget
}

data class ValidationIssue(
    val message: String,
    /** 0-based index of the question this issue belongs to, or null for exam-level fields. */
    val questionIndex: Int? = null
)

data class TeacherCreateExamUiState(
    val isLoadingChoices: Boolean = true,
    val choicesError: String? = null,
    val classChoices: List<ExamClassChoice> = emptyList(),
    val selectedChoice: ExamClassChoice? = null,
    val title: String = "",
    val durationMinutes: String = "",
    val passingScore: String = "",
    val maxViolations: Int = TeacherCreateExamViewModel.DEFAULT_MAX_VIOLATIONS,
    val mobileOnly: Boolean = true,
    val shuffleQuestions: Boolean = true,
    val shuffleOptions: Boolean = true,
    val questions: List<DraftQuestion> = listOf(DraftQuestion()),
    val activeQuestionIndex: Int = 0,
    /** Set on the first publish attempt; from then on issues track the live form. */
    val showValidation: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdExam: TeacherCreatedExam? = null,
    val imageError: String? = null
) {
    /**
     * Recomputed from the current form rather than stored, so a fixed problem
     * disappears as soon as it's fixed instead of lingering until the next
     * publish tap.
     */
    val validationIssues: List<ValidationIssue>
        get() = if (showValidation) TeacherCreateExamViewModel.validate(this) else emptyList()

    val questionsWithIssues: Set<Int> get() = validationIssues.mapNotNull { it.questionIndex }.toSet()

    val isUploadingAnyImage: Boolean
        get() = questions.any { q -> q.image?.isUploading == true || q.options.any { it.image?.isUploading == true } }

    /** Anything a teacher would lose by leaving — drives the discard confirmation. */
    val hasUnsavedWork: Boolean
        get() = createdExam == null && (
            title.isNotBlank() || durationMinutes.isNotBlank() || passingScore.isNotBlank() ||
                questions.size > 1 ||
                questions.any { q -> q.text.isNotBlank() || q.image != null || q.options.any { it.text.isNotBlank() || it.image != null } }
            )
}

/**
 * Backs Screen.TeacherCreateExam. Replaces a screen that was entirely fake:
 * it never called the server, generated an "SA1-xxxx" entry token on the
 * phone, pre-filled sample calculus questions and an Unsplash image, showed
 * "berhasil disinkronisasi ke server" without sending anything, then opened
 * the proctor view for a hardcoded exam id 101.
 *
 * Now: subject/class come from the teacher's real schedule, images are
 * uploaded to the server as they're picked, the exam is created with
 * POST teacher/cbt/exams, and the proctor view is only offered when the
 * server says this teacher may view the (server-issued) token.
 */
@HiltViewModel
class TeacherCreateExamViewModel @Inject constructor(
    private val cbtRepository: CbtRepository,
    private val teachingJournalRepository: TeachingJournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherCreateExamUiState())
    val uiState: StateFlow<TeacherCreateExamUiState> = _uiState.asStateFlow()

    /** Bytes of images whose upload failed, kept so the teacher can retry without re-picking. */
    private val failedUploads = mutableMapOf<String, CbtImageAttachment>()

    init {
        loadClassChoices()
    }

    fun loadClassChoices() {
        viewModelScope.launch {
            teachingJournalRepository.getTeacherSchedule().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoadingChoices = true, choicesError = null) }
                    is NetworkResult.Success -> {
                        val choices = classChoicesFrom(result.data)
                        _uiState.update { state ->
                            state.copy(
                                isLoadingChoices = false,
                                classChoices = choices,
                                // Pre-select only when there is exactly one real option.
                                selectedChoice = state.selectedChoice?.takeIf { it in choices } ?: choices.singleOrNull()
                            )
                        }
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingChoices = false, choicesError = result.message) }
                }
            }
        }
    }

    // --- Exam-level fields ---------------------------------------------------

    fun selectChoice(choice: ExamClassChoice) = _uiState.update { it.copy(selectedChoice = choice) }
    fun updateTitle(value: String) = _uiState.update { it.copy(title = value.take(MAX_TITLE_LENGTH)) }
    fun updateDuration(value: String) = _uiState.update { it.copy(durationMinutes = value.filter(Char::isDigit).take(3)) }

    fun updatePassingScore(value: String) {
        // Digits with at most one decimal point (the server accepts a numeric 0–100).
        val cleaned = value.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        if (cleaned.count { it == '.' } <= 1 && cleaned.length <= 6) {
            _uiState.update { it.copy(passingScore = cleaned) }
        }
    }

    fun setMaxViolations(value: Int) = _uiState.update { it.copy(maxViolations = value.coerceIn(MIN_VIOLATIONS, MAX_VIOLATIONS)) }
    fun setMobileOnly(value: Boolean) = _uiState.update { it.copy(mobileOnly = value) }
    fun setShuffleQuestions(value: Boolean) = _uiState.update { it.copy(shuffleQuestions = value) }
    fun setShuffleOptions(value: Boolean) = _uiState.update { it.copy(shuffleOptions = value) }

    // --- Questions ------------------------------------------------------------

    fun selectQuestion(index: Int) = _uiState.update { state ->
        state.copy(activeQuestionIndex = index.coerceIn(0, state.questions.lastIndex))
    }

    fun addQuestion() = _uiState.update { state ->
        if (state.questions.size >= MAX_QUESTIONS) state
        else state.copy(questions = state.questions + DraftQuestion(), activeQuestionIndex = state.questions.size)
    }

    fun removeQuestion(index: Int) = _uiState.update { state ->
        if (state.questions.size <= 1 || index !in state.questions.indices) return@update state
        val remaining = state.questions.filterIndexed { i, _ -> i != index }
        state.copy(
            questions = remaining,
            activeQuestionIndex = state.activeQuestionIndex.coerceAtMost(remaining.lastIndex)
        )
    }

    fun updateQuestionText(questionId: String, text: String) =
        updateQuestion(questionId) { it.copy(text = text.take(MAX_QUESTION_TEXT)) }

    fun setCorrectKey(questionId: String, key: String) = updateQuestion(questionId) { it.copy(correctKey = key) }

    fun updateOptionText(questionId: String, key: String, text: String) = updateQuestion(questionId) { q ->
        q.copy(options = q.options.map { if (it.key == key) it.copy(text = text.take(MAX_OPTION_TEXT)) else it })
    }

    /** Adds the next letter (up to E) — the server accepts 2–5 options keyed A–E. */
    fun addOption(questionId: String) = updateQuestion(questionId) { q ->
        val next = OPTION_KEYS.getOrNull(q.options.size) ?: return@updateQuestion q
        q.copy(options = q.options + DraftOption(next))
    }

    /** Removes the last option only, so the remaining keys stay a contiguous A.. range. */
    fun removeLastOption(questionId: String) = updateQuestion(questionId) { q ->
        if (q.options.size <= MIN_OPTIONS) return@updateQuestion q
        val removed = q.options.last()
        q.copy(
            options = q.options.dropLast(1),
            correctKey = q.correctKey.takeIf { it != removed.key }
        )
    }

    // --- Images -----------------------------------------------------------------

    /**
     * Uploads right away so the payload only ever carries server URLs. The
     * result is applied only if the same image is still attached when the
     * upload finishes — a teacher can remove or replace it mid-upload.
     */
    fun attachImage(target: ImageTarget, previewUri: String, attachment: CbtImageAttachment) {
        imageRejectionReason(attachment)?.let { reason ->
            _uiState.update { it.copy(imageError = reason) }
            return
        }
        setImage(target, DraftImage(previewUri = previewUri, isUploading = true))
        upload(target, previewUri, attachment)
    }

    fun retryImageUpload(target: ImageTarget) {
        val current = imageAt(target) ?: return
        val attachment = failedUploads[current.previewUri] ?: return
        setImage(target, current.copy(isUploading = true, error = null))
        upload(target, current.previewUri, attachment)
    }

    fun removeImage(target: ImageTarget) {
        imageAt(target)?.let { failedUploads.remove(it.previewUri) }
        setImage(target, null)
    }

    fun onImageErrorShown() = _uiState.update { it.copy(imageError = null) }

    private fun upload(target: ImageTarget, previewUri: String, attachment: CbtImageAttachment) {
        viewModelScope.launch {
            cbtRepository.uploadExamImage(attachment).collect { result ->
                val stillAttached = imageAt(target)?.previewUri == previewUri
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> {
                        failedUploads.remove(previewUri)
                        if (stillAttached) {
                            setImage(target, DraftImage(previewUri = previewUri, remoteUrl = result.data.imageUrl))
                        }
                    }
                    is NetworkResult.Error -> {
                        failedUploads[previewUri] = attachment
                        if (stillAttached) {
                            setImage(target, DraftImage(previewUri = previewUri, error = result.message))
                        }
                    }
                }
            }
        }
    }

    private fun imageAt(target: ImageTarget): DraftImage? {
        val question = _uiState.value.questions.firstOrNull { it.localId == target.questionId } ?: return null
        return when (target) {
            is ImageTarget.Question -> question.image
            is ImageTarget.Option -> question.options.firstOrNull { it.key == target.key }?.image
        }
    }

    private fun setImage(target: ImageTarget, image: DraftImage?) = updateQuestion(target.questionId) { q ->
        when (target) {
            is ImageTarget.Question -> q.copy(image = image)
            is ImageTarget.Option -> q.copy(options = q.options.map { if (it.key == target.key) it.copy(image = image) else it })
        }
    }

    // --- Publish ----------------------------------------------------------------

    fun publish() {
        val state = _uiState.value
        if (state.isSubmitting || state.createdExam != null) return

        val issues = validate(state)
        _uiState.update { it.copy(showValidation = true, submitError = null) }
        if (issues.isNotEmpty()) {
            issues.firstNotNullOfOrNull { it.questionIndex }?.let { selectQuestion(it) }
            return
        }

        viewModelScope.launch {
            cbtRepository.createTeacherExam(buildRequest(state)).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isSubmitting = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isSubmitting = false, createdExam = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSubmitting = false, submitError = result.message) }
                }
            }
        }
    }

    fun dismissSubmitError() = _uiState.update { it.copy(submitError = null) }

    private fun updateQuestion(questionId: String, transform: (DraftQuestion) -> DraftQuestion) = _uiState.update { state ->
        state.copy(questions = state.questions.map { if (it.localId == questionId) transform(it) else it })
    }

    companion object {
        val OPTION_KEYS = listOf("A", "B", "C", "D", "E")
        const val MIN_OPTIONS = 2
        const val MIN_DURATION = 5
        const val MAX_DURATION = 300
        const val MIN_VIOLATIONS = 1
        const val MAX_VIOLATIONS = 10
        const val DEFAULT_MAX_VIOLATIONS = 3 // same default the server applies
        const val MAX_QUESTIONS = 200
        const val MAX_TITLE_LENGTH = 255
        const val MAX_QUESTION_TEXT = 10_000
        const val MAX_OPTION_TEXT = 5_000
        const val MAX_IMAGE_BYTES = 10L * 1024 * 1024 // server: max:10240 (KB)
        // "image/jpg" is non-standard but some gallery providers report it.
        val ALLOWED_IMAGE_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

        /** Distinct subject+class pairs from the teacher's real schedule, sorted for a stable picker. */
        fun classChoicesFrom(schedule: List<TeacherScheduleSlot>): List<ExamClassChoice> =
            schedule
                .filter { it.subjectId > 0 && it.classroomId > 0 }
                .map {
                    ExamClassChoice(
                        subjectId = it.subjectId,
                        subjectName = it.subjectName?.takeIf { name -> name.isNotBlank() } ?: "Mapel #${it.subjectId}",
                        classroomId = it.classroomId,
                        classroomName = it.classroomName?.takeIf { name -> name.isNotBlank() } ?: "Kelas #${it.classroomId}"
                    )
                }
                .distinctBy { it.subjectId to it.classroomId }
                .sortedWith(compareBy({ it.subjectName }, { it.classroomName }))

        fun imageRejectionReason(attachment: CbtImageAttachment): String? = when {
            attachment.mimeType.lowercase() !in ALLOWED_IMAGE_TYPES ->
                "Format gambar tidak didukung. Gunakan JPG, PNG, atau WebP."
            attachment.bytes.size > MAX_IMAGE_BYTES ->
                "Ukuran gambar melebihi 10 MB."
            attachment.bytes.isEmpty() ->
                "Gambar tidak bisa dibaca dari perangkat."
            else -> null
        }

        /** Mirrors the server's rules so problems surface before a round trip. */
        fun validate(state: TeacherCreateExamUiState): List<ValidationIssue> {
            val issues = mutableListOf<ValidationIssue>()

            if (state.title.isBlank()) issues += ValidationIssue("Nama ujian wajib diisi.")
            if (state.selectedChoice == null) issues += ValidationIssue("Pilih mata pelajaran & kelas dari jadwal mengajar Anda.")

            val duration = state.durationMinutes.toIntOrNull()
            if (duration == null || duration !in MIN_DURATION..MAX_DURATION) {
                issues += ValidationIssue("Durasi ujian harus $MIN_DURATION–$MAX_DURATION menit.")
            }
            val passing = state.passingScore.toDoubleOrNull()
            if (passing == null || passing < 0.0 || passing > 100.0) {
                issues += ValidationIssue("KKTP harus berupa angka 0–100.")
            }
            if (state.questions.isEmpty()) issues += ValidationIssue("Tambahkan minimal satu soal.")

            state.questions.forEachIndexed { index, q ->
                val n = index + 1
                if (q.text.isBlank()) issues += ValidationIssue("Soal #$n: teks pertanyaan masih kosong.", index)
                if (q.correctKey == null || q.options.none { it.key == q.correctKey }) {
                    issues += ValidationIssue("Soal #$n: tandai kunci jawaban yang benar.", index)
                }
                q.options.filter { it.text.isBlank() && it.image == null }.forEach { opt ->
                    issues += ValidationIssue("Soal #$n: pilihan ${opt.key} belum diisi (teks atau gambar).", index)
                }
                val images = listOfNotNull(q.image) + q.options.mapNotNull { it.image }
                if (images.any { it.isUploading }) {
                    issues += ValidationIssue("Soal #$n: gambar masih diunggah, tunggu sebentar.", index)
                } else if (images.any { it.remoteUrl == null }) {
                    issues += ValidationIssue("Soal #$n: ada gambar yang gagal diunggah — coba lagi atau hapus gambarnya.", index)
                }
            }
            return issues
        }

        /** Only call after [validate] returned no issues. */
        fun buildRequest(state: TeacherCreateExamUiState): TeacherCreateExamRequest {
            val choice = requireNotNull(state.selectedChoice)
            return TeacherCreateExamRequest(
                title = state.title.trim(),
                subjectId = choice.subjectId,
                classroomId = choice.classroomId,
                durationMinutes = state.durationMinutes.toInt(),
                passingScore = state.passingScore.toDouble(),
                maxViolations = state.maxViolations,
                mobileOnly = state.mobileOnly,
                // Honored by both the web exam and the student app API
                // (ApiStudentController::cbtExamQuestions), so sent as chosen
                // regardless of mobileOnly.
                shuffleQuestions = state.shuffleQuestions,
                shuffleOptions = state.shuffleOptions,
                questions = state.questions.map { q ->
                    TeacherQuestionPayload(
                        questionText = q.text.trim(),
                        imageUrl = q.image?.remoteUrl,
                        options = q.options.map { opt ->
                            CbtOptionItem(key = opt.key, text = opt.text.trim(), imageUrl = opt.image?.remoteUrl)
                        },
                        correctAnswer = requireNotNull(q.correctKey)
                    )
                }
            )
        }
    }
}
