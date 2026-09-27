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
    val errorMessage: String? = null,
    /**
     * FASE 76.7: the student message whose request failed. Shown with a
     * "gagal terkirim" mark and resent as-is by [AiViewModel.retryLastMessage];
     * the old retry button only hid the error and resent nothing.
     */
    val failedMessageId: Long? = null
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
                message = "Assalamu'alaikum! Saya **Sultan AI Tutor**. Saya tidak memberi jawaban jadi; saya bertanya balik supaya kamu menemukan jawabannya sendiri. Materi apa yang ingin kamu pahami hari ini?"
            )
        )
        _uiState.value = _uiState.value.copy(messages = initialMessages)
    }

    fun sendMessage(userText: String) {
        // One request at a time: a second tap while waiting used to start a
        // parallel request and interleave the replies.
        if (userText.isBlank() || _uiState.value.isLoading) return

        val userMessage = AiChatMessage(sender = "USER", message = userText)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            errorMessage = null,
            failedMessageId = null
        )
        deliver(userMessage)
    }

    /** Resend the message that failed (same text, same bubble), instead of making the student retype it. */
    fun retryLastMessage() {
        val state = _uiState.value
        val failed = state.messages.firstOrNull { it.id == state.failedMessageId } ?: run {
            clearError()
            return
        }
        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null, failedMessageId = null)
        deliver(failed)
    }

    private fun markFailed(userMessage: AiChatMessage, message: String?) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message ?: "Gagal mengirim pertanyaan ke Sultan AI Tutor.",
            failedMessageId = userMessage.id
        )
    }

    private fun deliver(userMessage: AiChatMessage) {
        val userText = userMessage.message

        viewModelScope.launch {
            val existingSessionId = _uiState.value.sessionId
            val sessionId = existingSessionId ?: run {
                var resolvedId: Long? = null
                aiRepository.startTutorSession(subjectName = "Umum", topic = userText.take(255)).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> resolvedId = result.data.id
                        is NetworkResult.Error -> markFailed(userMessage, result.message)
                        is NetworkResult.Loading -> Unit
                    }
                }
                resolvedId
            }

            if (sessionId == null) {
                if (_uiState.value.errorMessage == null) {
                    markFailed(userMessage, "Gagal memulai sesi belajar dengan Sultan AI Tutor.")
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
                        markFailed(userMessage, result.message)
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, failedMessageId = null)
    }
}
