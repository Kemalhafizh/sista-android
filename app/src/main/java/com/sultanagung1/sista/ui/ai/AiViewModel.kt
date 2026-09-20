package com.sultanagung1.sista.ui.ai

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.AiChatMessage
import com.sultanagung1.sista.data.model.EssayFeedbackResponse
import com.sultanagung1.sista.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiUiState(
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val sessionId: Long? = null,
    val messages: List<AiChatMessage> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val essayFeedback: EssayFeedbackResponse? = null,
    val errorMessage: String? = null
)


@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    init {
        initDefaultChat()
        loadAiSuggestions()
    }

    private fun loadAiSuggestions() {
        viewModelScope.launch {
            aiRepository.getTutorSuggestions().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(suggestions = result.data)
                }
                // On error, leave suggestions empty rather than showing stale/fabricated prompts.
            }
        }
    }

    private fun initDefaultChat() {
        val initialMessages = listOf(
            AiChatMessage(
                sender = "AI",
                message = "Assalamu'alaikum! Saya **Sultan AI Tutor**, asisten belajarmu di SMA Islam Sultan Agung 1 Semarang. Apa materi atau soal yang ingin kamu diskusikan hari ini?"
            )
        )
        _uiState.value = _uiState.value.copy(messages = initialMessages)
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = AiChatMessage(sender = "USER", message = userText)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + userMessage, isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val existingSessionId = _uiState.value.sessionId
            val sessionId = existingSessionId ?: run {
                var resolvedId: Long? = null
                aiRepository.startTutorSession(subjectName = "Umum", topic = userText.take(255)).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> resolvedId = result.data.id
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                        }
                        is NetworkResult.Loading -> Unit
                    }
                }
                resolvedId
            }

            if (sessionId == null) {
                if (_uiState.value.errorMessage == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Gagal memulai sesi belajar dengan Sultan AI Tutor.")
                }
                return@launch
            }
            if (_uiState.value.sessionId == null) {
                _uiState.value = _uiState.value.copy(sessionId = sessionId)
            }

            aiRepository.sendMessage(sessionId, userText).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            messages = _uiState.value.messages + result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        // Never fabricate a substantive AI answer on failure — surface the
                        // real error so the user can retry instead of trusting a fake reply.
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
