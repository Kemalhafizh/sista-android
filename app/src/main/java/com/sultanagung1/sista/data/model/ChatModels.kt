package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Models for In-App Messaging & Announcements
 */
data class ConversationItem(
    @SerializedName("id") val id: String,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("recipient_name") val recipientName: String,
    @SerializedName("recipient_role") val recipientRole: String, // "Wali Kelas XII MIPA 1", "Guru BK", "Ustadz Tahfidz"
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("last_message_time") val lastMessageTime: String,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("is_online") val isOnline: Boolean = false
)

data class ChatMessage(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("text") val text: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("is_me") val isMe: Boolean = false,
    @SerializedName("status") val status: String = "read", // "sent", "delivered", "read"
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
    @SerializedName("attachment_type") val attachmentType: String? = null // "image", "document"
)

data class SendMessageRequest(
    @SerializedName("conversation_id") val conversationId: String?,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("message") val message: String,
    @SerializedName("attachment_type") val attachmentType: String? = null
)

data class AnnouncementItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String, // "Darurat", "Akademik", "Kesiswaan", "Ibadah", "Keuangan"
    @SerializedName("author") val author: String, // "Kepala Sekolah", "Humas YBWSA", "Waka Kurikulum"
    @SerializedName("date") val date: String,
    @SerializedName("priority") val priority: String = "normal", // "emergency", "important", "normal"
    @SerializedName("cover_image_url") val coverImageUrl: String? = null,
    @SerializedName("attachment_url") val attachmentUrl: String? = null
)
