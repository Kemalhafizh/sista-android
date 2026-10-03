package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Models for In-App Messaging & Announcements
 */
data class ConversationItem(
    // Synthetic — GET parent/messages (ApiParentController::messages) returns a
    // flat message log, not a conversation entity with its own id, so this is
    // built client-side as "{recipientId}_{studentId}" (see ChatRepository).
    @SerializedName("id") val id: String,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("recipient_name") val recipientName: String,
    @SerializedName("recipient_role") val recipientRole: String, // "Wali Kelas XII MIPA 1", "Guru BK", "Ustadz Tahfidz"
    // Every ParentMessage row is keyed by (sender_id, recipient_id, student_id)
    // on the backend, and App\Events\ChatMessageSent broadcasts on
    // chat.{studentId} (the numeric PK) — needed to open the right private
    // WebSocket channel.
    @SerializedName("student_id") val studentId: String,
    // uuid counterpart of studentId — needed to call POST parent/messages,
    // which (like every other child-scoped endpoint) takes a uuid, never the
    // raw numeric id.
    @SerializedName("student_uuid") val studentUuid: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("last_message_time") val lastMessageTime: String,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("is_online") val isOnline: Boolean = false
)

data class ChatMessage(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    // Numeric student PK — resolves a just-sent message's real ConversationItem
    // identity (see ChatViewModel.sendMessage) and opens the matching
    // private-chat.{studentId} WebSocket channel. Blank for a WebSocket-pushed
    // message, which already arrives inside an already-resolved conversation.
    @SerializedName("student_id") val studentId: String = "",
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("text") val text: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("is_me") val isMe: Boolean = false,
    // Only "sent"/"read" are real — the backend has no "delivered" concept
    // (no delivered_at column, no delivery-ack event), so that state is never
    // actually produced; don't reintroduce it in UI as if it were tracked.
    @SerializedName("status") val status: String = "read",
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
    @SerializedName("attachment_type") val attachmentType: String? = null // "image" or "pdf"
)

/** A picked image/PDF ready to upload alongside a chat message — see ChatRepository.sendMessage(). */
data class ChatAttachment(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String
)

/** GET/POST parent/messages response item — mirrors ParentMessageResource exactly. */
data class ParentMessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("sender_role") val senderRole: String,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("recipient_name") val recipientName: String,
    @SerializedName("recipient_role") val recipientRole: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("student_uuid") val studentUuid: String?,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("message") val message: String,
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
    @SerializedName("attachment_type") val attachmentType: String? = null,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_at_human") val createdAtHuman: String?
)

/**
 * Real teacher directory from GET evaluations/teachers (EvaluationMobileApiController)
 * — the only backend endpoint that returns actual guru/bk id+name pairs any
 * authenticated user (including parents) can call. Its "subject" field is
 * itself a hardcoded backend placeholder ("Guru Mata Pelajaran" for every
 * teacher), not a real per-teacher subject.
 */
data class TeacherDirectoryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("subject") val subject: String
)

/**
 * A school announcement from `announcements`. The newer fields are null on
 * an older server; [isRead] and [acknowledgedAt] are this account's own.
 */
data class AnnouncementItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String = "",
    @SerializedName("content") val content: String = "",
    /** akademik, keuangan, kegiatan, keislaman, umum — as the web form stores it. */
    @SerializedName("category") val category: String = "umum",
    @SerializedName("author") val author: String = "",
    /** Server-formatted "2 jam yang lalu"; prefer [publishedAt]. */
    @SerializedName("date") val date: String = "",
    /** low, normal, urgent. */
    @SerializedName("priority") val priority: String = "normal",
    @SerializedName("cover_image_url") val coverImageUrl: String? = null,
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("is_pinned") val isPinned: Boolean = false,
    /** "Semua", "Siswa", "Wali murid · X-2", … */
    @SerializedName("audience") val audience: String? = null,
    @SerializedName("require_acknowledgement") val requireAcknowledgement: Boolean = false,
    @SerializedName("is_read") val isRead: Boolean? = null,
    @SerializedName("acknowledged_at") val acknowledgedAt: String? = null,
)
