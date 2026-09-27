package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class AiTutorSessionRequest(
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("difficulty_level") val difficultyLevel: String = "intermediate",
    @SerializedName("learning_objective") val learningObjective: String? = null
)

/** One message row as AiTutorController returns it — shared shape for session greeting and chat replies. */
data class AiTutorMessageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("session_id") val sessionId: Long? = null,
    @SerializedName("sender") val sender: String, // "ai" | "student"
    @SerializedName("message") val message: String,
    @SerializedName("socratic_scaffold_type") val scaffoldType: String? = null,
    @SerializedName("comprehension_score") val comprehensionScore: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    fun toChatMessage(): AiChatMessage = AiChatMessage(
        id = id,
        sender = if (sender.equals("ai", ignoreCase = true)) "AI" else "USER",
        message = message
    )
}

data class AiTutorSessionData(
    @SerializedName("id") val id: Long,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("messages") val messages: List<AiTutorMessageDto> = emptyList()
)

// AiTutorController@startSession/getHistory return the session under a
// top-level "session" key, NOT wrapped in the usual {data: ...} envelope.
data class AiTutorSessionEnvelope(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("session") val session: AiTutorSessionData? = null
)

data class AiTutorReplyData(
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("student_message") val studentMessage: AiTutorMessageDto,
    @SerializedName("ai_response") val aiResponse: AiTutorMessageDto,
    @SerializedName("current_comprehension") val currentComprehension: Double? = null
)

data class AiMessageRequest(
    @SerializedName("message") val message: String
)

data class AiChatMessage(
    @SerializedName("id") val id: Long = System.currentTimeMillis(),
    @SerializedName("sender") val sender: String, // USER, AI
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String = "Baru saja"
)

data class EssaySubmissionRequest(
    @SerializedName("title") val title: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("essay_text") val essayText: String
)

data class EssayFeedbackResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("grammar_score") val grammarScore: Double,
    @SerializedName("coherence_score") val coherenceScore: Double,
    @SerializedName("plagiarism_score") val plagiarismScore: Double,
    @SerializedName("overall_score") val overallScore: Double,
    @SerializedName("feedback_summary") val feedbackSummary: String,
    @SerializedName("suggestions") val suggestions: List<String>
)
