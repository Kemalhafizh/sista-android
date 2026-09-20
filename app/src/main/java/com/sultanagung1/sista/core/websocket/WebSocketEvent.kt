package com.sultanagung1.sista.core.websocket

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

sealed class WebSocketEvent {
    data class ChatMessageReceived(
        val messageId: String,
        val conversationId: String,
        val senderId: String,
        val senderName: String,
        val text: String,
        val timestamp: String
    ) : WebSocketEvent()

    data class LiveAttendanceRecorded(
        val studentId: String,
        val studentName: String,
        val checkInTime: String,
        val status: String,
        val gate: String
    ) : WebSocketEvent()

    data class EmergencyAlertTriggered(
        val alertId: String,
        val title: String,
        val message: String,
        val location: String,
        val timestamp: String
    ) : WebSocketEvent()

    data class AnnouncementBroadcast(
        val announcementId: String,
        val title: String,
        val summary: String,
        val category: String,
        val priority: String,
        val timestamp: String
    ) : WebSocketEvent()

    data class UserTypingStatus(
        val conversationId: String,
        val senderId: String,
        val isTyping: Boolean
    ) : WebSocketEvent()
}
