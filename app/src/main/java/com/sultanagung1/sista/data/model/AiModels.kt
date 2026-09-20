package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class AiTutorSessionRequest(
    @SerializedName("subject") val subject: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("difficulty_level") val difficultyLevel: String = "Medium"
)

data class AiTutorSessionResponse(
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("greeting_message") val greetingMessage: String,
    @SerializedName("topic") val topic: String
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
