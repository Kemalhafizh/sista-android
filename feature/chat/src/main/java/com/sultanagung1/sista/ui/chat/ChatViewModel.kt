package com.sultanagung1.sista.ui.chat

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.ChatAttachment
import com.sultanagung1.sista.data.model.ChatMessage
import com.sultanagung1.sista.data.model.ConversationItem
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.data.model.TeacherDirectoryItem
import com.sultanagung1.sista.data.repository.ChatRepository
import com.sultanagung1.sista.data.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ConversationItem> = emptyList(),
    val activeConversation: ConversationItem? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isRecipientTyping: Boolean = false,
    val availableTeachers: List<TeacherDirectoryItem> = emptyList(),
    val isLoadingTeachers: Boolean = false,
    // Needed to pick which child a brand-new consultation is about — every
    // send requires a student_uuid, and a parent can have more than one child.
    val availableChildren: List<ParentChildItem> = emptyList(),
    val isLoadingChildren: Boolean = false,
    // "New Consultation" (picking a teacher + child) only makes sense for a
    // parent — a teacher/BK only ever replies to a thread a parent already
    // started, since parent/children (needed to list students) is role:parent
    // only. Defaults true so the FAB doesn't flash-hide before role loads.
    val isParentRole: Boolean = true,
    val errorMessage: String? = null
)


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val parentRepository: ParentRepository,
    private val sessionManager: SessionManager,
    private val webSocketManager: ReverbWebSocketManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val role = sessionManager.userRoleFlow.first().orEmpty()
            val isParent = !(role.equals("guru", true) || role.equals("teacher", true) || role.equals("bk", true))
            _uiState.update { it.copy(isParentRole = isParent) }
        }
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
        viewModelScope.launch {
            // Screen.Chat gets its own fresh ChatViewModel instance (Hilt scopes
            // hiltViewModel() to the destination's back stack entry), so the
            // in-memory list from ConversationListScreen's instance usually isn't
            // populated yet here — fetch it directly rather than relying on it.
            var conv = _uiState.value.conversations.find { it.id == conversationId }
            if (conv == null) {
                chatRepository.getConversations().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _uiState.update { it.copy(conversations = result.data) }
                            conv = result.data.find { it.id == conversationId }
                        }
                        is NetworkResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                        is NetworkResult.Loading -> Unit
                    }
                }
            }
            conv?.let { resolved ->
                _uiState.update { it.copy(activeConversation = resolved, isRecipientTyping = false) }
                // FASE 71: real-time — subscribe to this child's private chat
                // channel so a reply from the teacher shows up without waiting
                // on a REST poll.
                if (resolved.studentId.isNotBlank()) {
                    webSocketManager?.subscribeToChannel("private-chat.${resolved.studentId}")
                }
            }
            loadMessages(conversationId)
        }
    }

    /**
     * Starts a chat with a teacher chosen from the real directory, about a
     * specific child (New Consultation) — a parent can have more than one
     * child, and POST parent/messages requires student_uuid, so both must be
     * chosen explicitly rather than guessed. The numeric studentId isn't known
     * yet (only the backend assigns it, on the first real message), so
     * subscribing to the private-chat WebSocket channel is deferred to
     * sendMessage() once that id comes back.
     */
    fun startConsultationWith(teacher: TeacherDirectoryItem, child: ParentChildItem) {
        val conv = ConversationItem(
            id = "${teacher.id}_${child.uuid}",
            recipientId = teacher.id.toString(),
            recipientName = teacher.name,
            recipientRole = teacher.subject,
            studentId = "",
            studentUuid = child.uuid,
            lastMessage = "",
            lastMessageTime = "Baru saja",
            unreadCount = 0,
            isOnline = false
        )
        _uiState.update { it.copy(activeConversation = conv, messages = emptyList(), isRecipientTyping = false) }
    }

    fun loadTeacherDirectory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTeachers = true) }
            chatRepository.getTeacherDirectory().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoadingTeachers = false, availableTeachers = result.data)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoadingTeachers = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun loadChildrenForConsultation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingChildren = true) }
            parentRepository.getChildren().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoadingChildren = false, availableChildren = result.data)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoadingChildren = false, errorMessage = result.message)
                    }
                }
            }
        }
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

    fun sendMessage(text: String, attachment: ChatAttachment? = null) {
        if (text.isBlank() && attachment == null) return
        val conv = _uiState.value.activeConversation ?: return
        if (conv.studentUuid.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            chatRepository.sendMessage(conv.recipientId, conv.studentUuid, text, attachment).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val sent = result.data
                        _uiState.update { state ->
                            val active = state.activeConversation
                            // A brand-new consultation's placeholder id/studentId
                            // (from startConsultationWith) only gets resolved to
                            // the backend's real numeric student_id once this
                            // first send comes back — reconcile it now, and only
                            // then subscribe to the private-chat WebSocket channel.
                            val resolvedConv = if (active != null && active.id != sent.conversationId) {
                                webSocketManager?.subscribeToChannel("private-chat.${sent.studentId}")
                                active.copy(id = sent.conversationId, studentId = sent.studentId)
                            } else active
                            state.copy(
                                activeConversation = resolvedConv,
                                messages = state.messages + sent,
                                isSending = false
                            )
                        }
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSending = false, errorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    /**
     * Broadcasts a "typing" whisper on the active conversation's private
     * channel so the other party sees the animated indicator. Needs a
     * resolved numeric studentId (see openConversation()) since that's the
     * only channel name the WebSocket connection is actually subscribed to —
     * silently does nothing before that resolves (e.g. mid-first-message of
     * a brand-new consultation).
     */
    fun sendTypingStatus(isTyping: Boolean) {
        val conv = _uiState.value.activeConversation ?: return
        if (conv.studentId.isBlank()) return
        viewModelScope.launch {
            val myId = sessionManager.userIdFlow.first().orEmpty()
            webSocketManager?.sendTypingStatus(conv.studentId, myId, isTyping)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
                                status = "read",
                                attachmentUrl = event.attachmentUrl,
                                attachmentType = event.attachmentType
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
