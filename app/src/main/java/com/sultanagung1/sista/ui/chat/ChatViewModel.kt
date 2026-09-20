package com.sultanagung1.sista.ui.chat

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.ChatMessage
import com.sultanagung1.sista.data.model.ConversationItem
import com.sultanagung1.sista.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ConversationItem> = emptyList(),
    val activeConversation: ConversationItem? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isRecipientTyping: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val webSocketManager: ReverbWebSocketManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
        listenToWebSocket()
    }

    fun loadConversations() {
        viewModelScope.launch {
            chatRepository.getConversations().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(conversations = result.data, isLoading = false, errorMessage = null)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun openConversation(conversationId: String) {
        val conv = _uiState.value.conversations.find { it.id == conversationId }
            ?: ConversationItem(
                id = conversationId,
                recipientId = "t1",
                recipientName = "Ustadz Drs. H. Bambang Suherman",
                recipientRole = "Wali Kelas XII MIPA 1",
                lastMessage = "",
                lastMessageTime = "Baru saja",
                unreadCount = 0,
                isOnline = true
            )

        _uiState.update { it.copy(activeConversation = conv, isRecipientTyping = false) }
        loadMessages(conversationId)
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(messages = result.data, isLoading = false)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val conv = _uiState.value.activeConversation ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            chatRepository.sendMessage(conv.id, conv.recipientId, text).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages + result.data,
                                isSending = false
                            )
                        }
                    }
                    else -> _uiState.update { it.copy(isSending = false) }
                }
            }
        }
    }

    private fun listenToWebSocket() {
        if (webSocketManager == null) return
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is WebSocketEvent.ChatMessageReceived -> {
                        if (_uiState.value.activeConversation?.id == event.conversationId) {
                            val newMsg = ChatMessage(
                                id = event.messageId,
                                conversationId = event.conversationId,
                                senderId = event.senderId,
                                senderName = event.senderName,
                                text = event.text,
                                timestamp = event.timestamp,
                                isMe = false,
                                status = "read"
                            )
                            _uiState.update { it.copy(messages = it.messages + newMsg, isRecipientTyping = false) }
                        }
                    }
                    is WebSocketEvent.UserTypingStatus -> {
                        if (_uiState.value.activeConversation?.id == event.conversationId) {
                            _uiState.update { it.copy(isRecipientTyping = event.isTyping) }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
