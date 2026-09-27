package com.sultanagung1.sista.ui.cbt

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.security.CbtEncryptedVault
import com.sultanagung1.sista.core.sync.OfflineActionQueue
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.CbtRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CbtUiEvent : UiEvent {
    data object LoadExams : CbtUiEvent
    data class SelectOption(val questionId: Long, val optionKey: String, val examId: Long? = null) : CbtUiEvent
    data class GoToQuestion(val index: Int) : CbtUiEvent
    data class SubmitExam(val examId: Long) : CbtUiEvent
}

sealed interface CbtUiEffect : UiEffect {
    data class ShowNotification(val message: String) : CbtUiEffect
}

data class CbtUiState(
    val isLoading: Boolean = false,
    val exams: List<CbtExamItem> = emptyList(),
    val currentExamQuestions: List<CbtQuestionItem> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val remainingSeconds: Long = 5400, // 90 mins
    val violationCount: Int = 0,
    val isSubmitted: Boolean = false,
    val submitResult: CbtSubmitResponse? = null,
    val errorMessage: String? = null,
    // === FASE 25: Enterprise CBT States ===
    val isPreFetched: Boolean = false,
    val isVaultDecrypted: Boolean = false,
    val proctorIntervention: ProctorCommand? = null,
    val lastSyncedAt: Long = 0L,
    // === FASE 26: Token Gate & Force Close ===
    val isForceClosedBySystem: Boolean = false,
    val forceCloseReason: String? = null,
    val tokenValidated: Boolean = false,
    val tokenValidationState: TokenValidationState = TokenValidationState.Idle,
    // === FASE 72.2: Live Proctoring — the student's own numeric id, resolved
    // once from validateExamToken()'s response, needed to subscribe to the
    // per-student private proctor-intervention channel. ===
    val studentId: Long? = null,
    // The exam's real anti-cheat lockout threshold (exams.max_violations),
    // also resolved from validateExamToken() — drives
    // CbtAntiCheatEngine.maxViolationsAllowed instead of a hardcoded value.
    val maxViolations: Int? = null,
    // === FASE 69: Resilient submission — a true network failure (not a
    // server-side rejection) queues the submission offline instead of
    // just showing an error the student can do nothing about. ===
    val isQueuedOffline: Boolean = false
) : UiState


/**
 * FASE 69.1: [savedStateHandle] survives process death (the LMK killing the
 * app while a student briefly switches away during a 90-minute exam) —
 * currentQuestionIndex/selectedAnswers/remainingSeconds are restored instead
 * of resetting to a blank exam. The countdown timer itself now lives here
 * (a single tick source) instead of duplicated as separate, disconnected
 * Compose-local state in CbtExamRoomScreen, which never actually flowed back
 * into timeSpentSeconds on submit.
 */
@HiltViewModel
class CbtViewModel @Inject constructor(
    private val cbtRepository: CbtRepository,
    private val offlineActionQueue: OfflineActionQueue,
    private val savedStateHandle: SavedStateHandle,
    private val webSocketManager: ReverbWebSocketManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreSnapshot())
    val uiState: StateFlow<CbtUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CbtUiEffect>()
    val effect: SharedFlow<CbtUiEffect> = _effect.asSharedFlow()

    private var autoSyncJob: Job? = null
    private var timerJob: Job? = null
    private var heartbeatJob: Job? = null
    private val gson = Gson()

    init {
        loadExams()
    }

    private fun restoreSnapshot(): CbtUiState {
        val answersJson = savedStateHandle.get<String>(KEY_ANSWERS)
        val restoredAnswers: Map<String, String> = if (!answersJson.isNullOrBlank()) {
            try {
                gson.fromJson(answersJson, object : TypeToken<Map<String, String>>() {}.type)
            } catch (_: Exception) {
                emptyMap()
            }
        } else emptyMap()
        return CbtUiState(
            currentQuestionIndex = savedStateHandle.get<Int>(KEY_QUESTION_INDEX) ?: 0,
            selectedAnswers = restoredAnswers,
            remainingSeconds = savedStateHandle.get<Long>(KEY_REMAINING_SECONDS) ?: 5400L,
            violationCount = savedStateHandle.get<Int>(KEY_VIOLATION_COUNT) ?: 0
        )
    }

    /** Persists exactly the fields a process-death restore needs — called after every mutation to those fields. */
    private fun persistSnapshot(state: CbtUiState) {
        savedStateHandle[KEY_ANSWERS] = gson.toJson(state.selectedAnswers)
        savedStateHandle[KEY_QUESTION_INDEX] = state.currentQuestionIndex
        savedStateHandle[KEY_REMAINING_SECONDS] = state.remainingSeconds
        savedStateHandle[KEY_VIOLATION_COUNT] = state.violationCount
    }

    /**
     * Starts (or resumes) the exam countdown. Uses a wall-clock deadline
     * rather than a naive per-second decrement so a process-death gap of
     * several minutes is reflected accurately once the ViewModel restarts,
     * instead of silently pausing the clock while the app was dead.
     */
    fun startExamTimer(totalDurationSeconds: Long) {
        if (timerJob?.isActive == true) return
        val alreadyRestored = savedStateHandle.get<Long>(KEY_REMAINING_SECONDS) != null
        if (!alreadyRestored) {
            _uiState.value = _uiState.value.copy(remainingSeconds = totalDurationSeconds)
            persistSnapshot(_uiState.value)
        }
        val deadlineElapsedRealtime = android.os.SystemClock.elapsedRealtime() + _uiState.value.remainingSeconds * 1000
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                val secondsLeft = ((deadlineElapsedRealtime - android.os.SystemClock.elapsedRealtime()) / 1000).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(remainingSeconds = secondsLeft)
                savedStateHandle[KEY_REMAINING_SECONDS] = secondsLeft
            }
        }
    }

    fun pauseExamTimer() {
        timerJob?.cancel()
    }

    fun onEvent(event: CbtUiEvent) {
        when (event) {
            is CbtUiEvent.LoadExams -> loadExams()
            is CbtUiEvent.SelectOption -> selectOption(event.questionId, event.optionKey, event.examId)
            is CbtUiEvent.GoToQuestion -> goToQuestion(event.index)
            is CbtUiEvent.SubmitExam -> submitExam(event.examId)
        }
    }

    fun loadExams() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cbtRepository.getExams().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            exams = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun loadQuestions(examId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            cbtRepository.getExamQuestions(examId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentExamQuestions = result.data,
                            currentQuestionIndex = 0,
                            errorMessage = null
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    /**
     * Pre-fetch encrypted exam payload into local CbtEncryptedVault before exam start.
     */
    fun preFetchExamPayload(examId: Long, vault: CbtEncryptedVault) {
        viewModelScope.launch {
            cbtRepository.getEncryptedPayload(examId).collect { result ->
                if (result is NetworkResult.Success) {
                    val map = result.data
                    val encData = map["encrypted_data"] as? String
                    val iv = map["iv"] as? String
                    if (!encData.isNullOrBlank() && !iv.isNullOrBlank()) {
                        vault.saveEncryptedPayload(examId, encData, iv)
                        _uiState.value = _uiState.value.copy(isPreFetched = true)
                    }
                }
            }
        }
    }

    /**
     * Retrieve decryption key and unlock questions from vault instantaneously.
     */
    fun unlockFromVaultWithKey(examId: Long, vault: CbtEncryptedVault) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cbtRepository.getDecryptionKey(examId).collect { result ->
                if (result is NetworkResult.Success) {
                    val map = result.data
                    val key = map["key"] as? String
                    if (!key.isNullOrBlank()) {
                        val decryptedJson = vault.decryptExamPayload(examId, key)
                        if (!decryptedJson.isNullOrBlank()) {
                            try {
                                val parsed = gson.fromJson<Map<String, Any>>(
                                    decryptedJson,
                                    object : TypeToken<Map<String, Any>>() {}.type
                                )
                                val questionsRaw = parsed["questions"]
                                val questionsJson = gson.toJson(questionsRaw)
                                val questionsList: List<CbtQuestionItem> = gson.fromJson(
                                    questionsJson,
                                    object : TypeToken<List<CbtQuestionItem>>() {}.type
                                )
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isVaultDecrypted = true,
                                    currentExamQuestions = questionsList,
                                    currentQuestionIndex = 0
                                )
                                return@collect
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                // Fallback to standard online load
                loadQuestions(examId)
            }
        }
    }

    fun selectOption(questionId: Long, optionKey: String, examId: Long? = null) {
        val updatedAnswers = _uiState.value.selectedAnswers + (questionId.toString() to optionKey)
        _uiState.value = _uiState.value.copy(selectedAnswers = updatedAnswers)
        persistSnapshot(_uiState.value)

        if (examId != null) {
            scheduleMicroSync(examId)
        }
    }

    private fun scheduleMicroSync(examId: Long) {
        autoSyncJob?.cancel()
        autoSyncJob = viewModelScope.launch {
            delay(1500) // Debounce 1.5 seconds
            cbtRepository.microSyncAnswers(examId, _uiState.value.selectedAnswers).collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(lastSyncedAt = System.currentTimeMillis())
                }
            }
        }
    }

    fun goToQuestion(index: Int) {
        if (index in 0 until _uiState.value.currentExamQuestions.size) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = index)
            savedStateHandle[KEY_QUESTION_INDEX] = index
        }
    }

    /**
     * [violationType] is one of CbtAntiCheatEngine's ExamViolationType.name
     * values (e.g. "SCREENSHOT_ATTEMPT") — reported to the backend so the
     * proctor sees it live (StudentCheatedEvent), in addition to updating
     * the local count that submitExam() sends as totalViolations.
     */
    fun recordViolation(examId: Long? = null, violationType: String? = null) {
        _uiState.value = _uiState.value.copy(violationCount = _uiState.value.violationCount + 1)
        savedStateHandle[KEY_VIOLATION_COUNT] = _uiState.value.violationCount

        if (examId != null && violationType != null) {
            viewModelScope.launch {
                cbtRepository.logViolation(examId, violationType).collect { }
            }
        }
    }

    fun handleProctorCommand(command: ProctorCommand) {
        _uiState.value = _uiState.value.copy(proctorIntervention = command)
        val extraMinutes = command.extraMinutes
        if (command.action == "EXTEND_TIME" && extraMinutes != null) {
            _uiState.value = _uiState.value.copy(
                remainingSeconds = _uiState.value.remainingSeconds + (extraMinutes * 60)
            )
        }
    }

    fun dismissProctorIntervention() {
        _uiState.value = _uiState.value.copy(proctorIntervention = null)
    }

    /**
     * FASE 72.2: subscribes to this student's private proctor-intervention
     * channel and starts listening for ProctorInterventionEvent, without
     * pausing the exam timer — interventions render as a dismissible
     * overlay (see handleProctorCommand()/CbtExamRoomScreen's dialog), not
     * a blocking gate. No-op if studentId isn't known yet (e.g. token
     * validation response didn't resolve one) or the socket is unavailable.
     */
    fun startLiveProctoring(examId: Long, studentId: Long?) {
        if (webSocketManager == null || studentId == null) return
        webSocketManager.subscribeToChannel("private-cbt.exam.$examId.student.$studentId")

        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                if (event is WebSocketEvent.ProctorInterventionReceived && event.examId == examId) {
                    // Sets uiState.proctorIntervention — CbtExamRoomScreen's existing
                    // (previously dead) dialog renders it and, for FORCE_SUBMIT,
                    // itself calls submitExam() from the "Kumpulkan Sekarang" button;
                    // triggering submit here too would race it.
                    handleProctorCommand(
                        ProctorCommand(
                            action = event.actionType,
                            message = event.message,
                            extraMinutes = event.extraMinutes
                        )
                    )
                }
            }
        }

        startHeartbeat(examId)
    }

    /**
     * Pings the backend every 15s so the proctor dashboard can distinguish a
     * quiet-but-alive student from one whose app died / lost connection.
     * Distinct from forceCloseExam(), which reports an explicit lifecycle
     * violation.
     */
    private fun startHeartbeat(examId: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                cbtRepository.sendHeartbeat(examId)
                delay(15_000)
            }
        }
    }

    fun stopLiveProctoring() {
        heartbeatJob?.cancel()
    }

    fun submitExam(examId: Long, vault: CbtEncryptedVault? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            pauseExamTimer()
            stopLiveProctoring()
            val request = CbtSubmitRequest(
                examId = examId,
                answers = _uiState.value.selectedAnswers,
                totalViolations = _uiState.value.violationCount,
                timeSpentSeconds = 5400 - _uiState.value.remainingSeconds
            )

            cbtRepository.submitExam(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        vault?.clearVault(examId)
                        clearSnapshot()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSubmitted = true,
                            submitResult = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        // FASE 69.3: a genuine connectivity failure (no HTTP code — the
                        // request never reached the server) queues the submission for
                        // automatic replay instead of stranding the student's answers.
                        // A real server-side rejection (validation, auth) still surfaces
                        // as an error — that must not be silently treated as "submitted".
                        if (result.code == null) {
                            offlineActionQueue.queueCbtSubmit(request)
                            vault?.clearVault(examId)
                            clearSnapshot()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSubmitted = true,
                                isQueuedOffline = true
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    private fun clearSnapshot() {
        savedStateHandle.remove<String>(KEY_ANSWERS)
        savedStateHandle.remove<Int>(KEY_QUESTION_INDEX)
        savedStateHandle.remove<Long>(KEY_REMAINING_SECONDS)
        savedStateHandle.remove<Int>(KEY_VIOLATION_COUNT)
    }

    companion object {
        private const val KEY_ANSWERS = "cbt_snapshot_answers"
        private const val KEY_QUESTION_INDEX = "cbt_snapshot_question_index"
        private const val KEY_REMAINING_SECONDS = "cbt_snapshot_remaining_seconds"
        private const val KEY_VIOLATION_COUNT = "cbt_snapshot_violation_count"
    }

    // === FASE 26: Token Validation & Force Close Functions ===

    fun validateExamToken(examId: Long, token: String, onValidated: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(tokenValidationState = TokenValidationState.Loading)
            cbtRepository.validateExamToken(examId, token.trim().uppercase()).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        if (result.data.valid) {
                            _uiState.value = _uiState.value.copy(
                                tokenValidated = true,
                                tokenValidationState = TokenValidationState.Success(result.data.message),
                                studentId = result.data.studentId,
                                maxViolations = result.data.maxViolations
                            )
                            onValidated()
                        } else {
                            _uiState.value = _uiState.value.copy(
                                tokenValidationState = TokenValidationState.Error(result.data.message)
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            tokenValidationState = TokenValidationState.Error(
                                result.message ?: "Token tidak valid atau ujian belum aktif"
                            )
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(tokenValidationState = TokenValidationState.Loading)
                    }
                }
            }
        }
    }

    fun forceCloseExam(examId: Long, reason: String, vault: CbtEncryptedVault? = null) {
        stopLiveProctoring()
        _uiState.value = _uiState.value.copy(
            isForceClosedBySystem = true,
            forceCloseReason = reason,
            tokenValidated = false
        )

        viewModelScope.launch {
            val currentAnswers = _uiState.value.selectedAnswers.toMap()
            cbtRepository.forceCloseExam(examId, reason, currentAnswers).collect {
                vault?.clearVault(examId)
            }
        }
    }

    fun resetTokenValidationState() {
        _uiState.value = _uiState.value.copy(
            tokenValidationState = TokenValidationState.Idle,
            tokenValidated = false,
            isForceClosedBySystem = false,
            forceCloseReason = null
        )
    }

    fun getExamById(examId: Long): CbtExamItem? {
        return _uiState.value.exams.firstOrNull { it.id == examId }
    }
}

