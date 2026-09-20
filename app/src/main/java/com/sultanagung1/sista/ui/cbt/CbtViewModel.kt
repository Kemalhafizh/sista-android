package com.sultanagung1.sista.ui.cbt

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.security.CbtEncryptedVault
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
    val tokenValidationState: TokenValidationState = TokenValidationState.Idle
) : UiState


@HiltViewModel
class CbtViewModel @Inject constructor(private val cbtRepository: CbtRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CbtUiState())
    val uiState: StateFlow<CbtUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CbtUiEffect>()
    val effect: SharedFlow<CbtUiEffect> = _effect.asSharedFlow()

    private var autoSyncJob: Job? = null
    private val gson = Gson()

    init {
        loadExams()
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            cbtRepository.getExamQuestions(examId).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentExamQuestions = result.data,
                        currentQuestionIndex = 0
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
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
        }
    }

    fun recordViolation() {
        _uiState.value = _uiState.value.copy(violationCount = _uiState.value.violationCount + 1)
    }

    fun handleProctorCommand(command: ProctorCommand) {
        _uiState.value = _uiState.value.copy(proctorIntervention = command)
        if (command.action == "EXTEND_TIME" && command.extraMinutes != null) {
            _uiState.value = _uiState.value.copy(
                remainingSeconds = _uiState.value.remainingSeconds + (command.extraMinutes * 60)
            )
        }
    }

    fun dismissProctorIntervention() {
        _uiState.value = _uiState.value.copy(proctorIntervention = null)
    }

    fun submitExam(examId: Long, vault: CbtEncryptedVault? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val request = CbtSubmitRequest(
                examId = examId,
                answers = _uiState.value.selectedAnswers,
                totalViolations = _uiState.value.violationCount,
                timeSpentSeconds = 5400 - _uiState.value.remainingSeconds
            )

            cbtRepository.submitExam(request).collect { result ->
                if (result is NetworkResult.Success) {
                    vault?.clearVault(examId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmitted = true,
                        submitResult = result.data
                    )
                } else if (result is NetworkResult.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
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
                                tokenValidationState = TokenValidationState.Success(result.data.message)
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

