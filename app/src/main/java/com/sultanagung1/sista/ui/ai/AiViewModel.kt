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
    val sessionId: Long? = 1L,
    val messages: List<AiChatMessage> = emptyList(),
    val suggestions: List<String> = listOf(
        "Jelaskan konsep Limit Trigonometri",
        "Hukum bacaan Mad Lazim Mukhaffaf Kilmi",
        "Contoh soal UTBK Penalaran Matematika",
        "Kerangka esai Peradaban Islam Andalusia"
    ),
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
            try {
                aiRepository.getSmartSuggestions().collect { /* processed in background if needed */ }
            } catch (_: Exception) {}
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
        val updated = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(messages = updated, isLoading = true)

        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId ?: 1L
            aiRepository.sendMessage(sessionId, userText).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            messages = _uiState.value.messages + result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        // Fallback response for demonstration if backend AI is processing
                        val fallbackAi = AiChatMessage(
                            sender = "AI",
                            message = "Mari kita telaah bersama: Untuk konsep tersebut, langkah pertama adalah mengidentifikasi besaran yang diketahui, lalu terapkan hukum kekekalan energi mekanik: E_m1 = E_m2. Coba hitung nilai energi potensial awalnya terlebih dahulu!"
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            messages = _uiState.value.messages + fallbackAi
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
}
