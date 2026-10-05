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
    /** The exam list was asked for at least once; before that an empty list means nothing. */
    val examsLoaded: Boolean = false,
    val exams: List<CbtExamItem> = emptyList(),
    /** The exam as GET questions describes it, with this student's clock. */
    val examMeta: CbtExamMeta? = null,
    val currentExamQuestions: List<CbtQuestionItem> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    /**
     * Seconds left on this student's attempt, as the server counts it (its
     * duration from the first start, cut off by the exam's end). Null until
     * the server has said, so the screen shows "–" instead of a made-up clock.
     */
    val remainingSeconds: Long? = null,
    val violationCount: Int = 0,
    val isSubmitted: Boolean = false,
    /** The answers were sent because the time ran out. */
    val isTimeUp: Boolean = false,
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
    // also resolved from validateExamToken().
    val maxViolations: Int? = null,
    /** log-violation answered is_blocked: the server closed and graded the attempt. */
    val isBlockedByServer: Boolean = false,
    // === FASE 69: Resilient submission — a true network failure (not a
    // server-side rejection) queues the submission offline instead of
    // just showing an error the student can do nothing about. ===
    val isQueuedOffline: Boolean = false
) : UiState {
    /** Nothing more can be answered on this device. */
    val isOver: Boolean get() = isSubmitted || isForceClosedBySystem || isBlockedByServer
}


/**
 * FASE 69.1: [savedStateHandle] survives process death (the LMK killing the
 * app while a student briefly switches away during an exam):
 * currentQuestionIndex/selectedAnswers/remainingSeconds are restored instead
 * of resetting to a blank exam. The countdown lives here (a single tick
 * source); its starting point always comes from the server.
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
    private var timerExamId: Long? = null
    private var submitInFlight = false
    private val gson = Gson()

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
            remainingSeconds = savedStateHandle.get<Long>(KEY_REMAINING_SECONDS),
            violationCount = savedStateHandle.get<Int>(KEY_VIOLATION_COUNT) ?: 0
        )
    }

    /** Persists exactly the fields a process-death restore needs — called after every mutation to those fields. */
    private fun persistSnapshot(state: CbtUiState) {
        savedStateHandle[KEY_ANSWERS] = gson.toJson(state.selectedAnswers)
        savedStateHandle[KEY_QUESTION_INDEX] = state.currentQuestionIndex
        state.remainingSeconds?.let { savedStateHandle[KEY_REMAINING_SECONDS] = it }
        savedStateHandle[KEY_VIOLATION_COUNT] = state.violationCount
    }

    /**
     * (Re)starts the countdown from [seconds] the server reported. Uses an
     * elapsed-realtime deadline rather than a naive per-second decrement.
     * When it reaches zero the answers are sent, as the web does.
     */
    fun startExamTimer(examId: Long, seconds: Long) {
        timerJob?.cancel()
        timerExamId = examId
        val start = seconds.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(remainingSeconds = start)
        savedStateHandle[KEY_REMAINING_SECONDS] = start
        val deadlineElapsedRealtime = android.os.SystemClock.elapsedRealtime() + start * 1000
        timerJob = viewModelScope.launch {
            while ((_uiState.value.remainingSeconds ?: 0) > 0) {
                delay(1000)
                val secondsLeft = ((deadlineElapsedRealtime - android.os.SystemClock.elapsedRealtime()) / 1000).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(remainingSeconds = secondsLeft)
                savedStateHandle[KEY_REMAINING_SECONDS] = secondsLeft
            }
            onTimeUp(examId)
        }
    }

    /**
     * Starts the clock from the token's answer only when nothing fresher is
     * known (a restored snapshot, or the questions' own clock).
     */
    fun startExamTimerIfUnknown(examId: Long, seconds: Long?) {
        val known = _uiState.value.remainingSeconds
        when {
            known != null -> if (timerJob?.isActive != true) startExamTimer(examId, known)
            seconds != null -> startExamTimer(examId, seconds)
        }
    }

    private fun onTimeUp(examId: Long) {
        val state = _uiState.value
        if (state.isOver || submitInFlight) return
        _uiState.value = state.copy(isTimeUp = true)
        submitExam(examId)
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
            cbtRepository.getExams().collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        examsLoaded = true,
                        exams = result.data,
                        errorMessage = null
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        examsLoaded = true,
                        errorMessage = result.message
                    )
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
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
                        val restoredIndex = _uiState.value.currentQuestionIndex
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            examMeta = result.data.exam,
                            currentExamQuestions = result.data.questions,
                            currentQuestionIndex = restoredIndex.coerceIn(0, (result.data.questions.size - 1).coerceAtLeast(0)),
                            errorMessage = null
                        )
                        // The server's clock wins over anything restored on the device.
                        result.data.exam.remainingSeconds?.let { startExamTimer(examId, it) }
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
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
                if (result is NetworkResult.Loading) return@collect
                if (result is NetworkResult.Success) {
                    val key = result.data["key"] as? String
                    val decryptedJson = key?.takeIf { it.isNotBlank() }?.let { vault.decryptExamPayload(examId, it) }
                    if (!decryptedJson.isNullOrBlank()) {
                        try {
                            val parsed = gson.fromJson<Map<String, Any>>(
                                decryptedJson,
                                object : TypeToken<Map<String, Any>>() {}.type
                            )
                            val questionsList: List<CbtQuestionItem> = gson.fromJson(
                                gson.toJson(parsed["questions"]),
                                object : TypeToken<List<CbtQuestionItem>>() {}.type
                            )
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isVaultDecrypted = true,
                                currentExamQuestions = questionsList,
                                currentQuestionIndex = _uiState.value.currentQuestionIndex
                                    .coerceIn(0, (questionsList.size - 1).coerceAtLeast(0))
                            )
                            return@collect
                        } catch (_: Exception) {
                            // A payload that doesn't parse falls back to the online load below.
                        }
                    }
                }
                loadQuestions(examId)
            }
        }
    }

    fun selectOption(questionId: Long, optionKey: String, examId: Long? = null) {
        if (_uiState.value.isOver) return
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
     * proctor sees it live (StudentCheatedEvent). When the server answers
     * is_blocked it has closed and graded the attempt; the screen locks.
     */
    fun recordViolation(examId: Long? = null, violationType: String? = null) {
        _uiState.value = _uiState.value.copy(violationCount = _uiState.value.violationCount + 1)
        savedStateHandle[KEY_VIOLATION_COUNT] = _uiState.value.violationCount

        if (examId != null && violationType != null) {
            viewModelScope.launch {
                cbtRepository.logViolation(examId, violationType).collect { result ->
                    if (result is NetworkResult.Success && result.data) {
                        pauseExamTimer()
                        stopLiveProctoring()
                        clearSnapshot()
                        _uiState.value = _uiState.value.copy(isBlockedByServer = true)
                    }
                }
            }
        }
    }

    fun handleProctorCommand(command: ProctorCommand) {
        _uiState.value = _uiState.value.copy(proctorIntervention = command)
        val extraMinutes = command.extraMinutes
        val remaining = _uiState.value.remainingSeconds
        val examId = timerExamId
        if (command.action == "EXTEND_TIME" && extraMinutes != null && remaining != null && examId != null) {
            startExamTimer(examId, remaining + extraMinutes * 60L)
        }
    }

    fun dismissProctorIntervention() {
        _uiState.value = _uiState.value.copy(proctorIntervention = null)
    }

    /**
     * FASE 72.2: subscribes to this student's private proctor-intervention
     * channel and starts listening for ProctorInterventionEvent, without
     * pausing the exam timer — interventions render as a dismissible
     * overlay, not a blocking gate. No-op if studentId isn't known yet or the
     * socket is unavailable.
     */
    fun startLiveProctoring(examId: Long, studentId: Long?) {
        if (webSocketManager == null || studentId == null) return
        webSocketManager.subscribeToChannel("private-cbt.exam.$examId.student.$studentId")

        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                if (event is WebSocketEvent.ProctorInterventionReceived && event.examId == examId) {
                    // For FORCE_SUBMIT the dialog's own button submits; submitting
                    // here too would race it.
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
        if (submitInFlight || _uiState.value.isOver) return
        submitInFlight = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            pauseExamTimer()
            stopLiveProctoring()
            val state = _uiState.value
            val request = CbtSubmitRequest(
                examId = examId,
                answers = state.selectedAnswers,
                totalViolations = state.violationCount,
                timeSpentSeconds = timeSpent(state.examMeta?.durationMinutes, state.remainingSeconds)
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
                        // A real server-side rejection still surfaces as an error.
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
            submitInFlight = false
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

        /** Seconds used of the exam's duration; null when either is unknown. */
        fun timeSpent(durationMinutes: Int?, remainingSeconds: Long?): Long? {
            if (durationMinutes == null || remainingSeconds == null) return null
            return (durationMinutes * 60L - remainingSeconds).coerceAtLeast(0)
        }
    }

    // === FASE 26: Token Validation & Force Close Functions ===

    /** The server's reason is shown when a token is refused (wrong, expired, finished, locked...). */
    fun validateExamToken(examId: Long, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(tokenValidationState = TokenValidationState.Loading)
            cbtRepository.validateExamToken(examId, token.trim().uppercase()).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        tokenValidated = true,
                        tokenValidationState = TokenValidationState.Success(result.data.message),
                        studentId = result.data.studentId,
                        maxViolations = result.data.maxViolations,
                        remainingSeconds = result.data.remainingSeconds
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        tokenValidationState = TokenValidationState.Error(result.message)
                    )
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(
                        tokenValidationState = TokenValidationState.Loading
                    )
                }
            }
        }
    }

    fun forceCloseExam(examId: Long, reason: String, vault: CbtEncryptedVault? = null) {
        stopLiveProctoring()
        pauseExamTimer()
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
