package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class CbtExamItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("status") val status: String // UPCOMING, ACTIVE, FINISHED
)

data class CbtQuestionItem(
    @SerializedName("id") val id: Long,
    @SerializedName("number") val number: Int,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("image_url", alternate = ["question_image", "image"]) val imageUrl: String? = null,
    @SerializedName("options") val options: List<CbtOptionItem>,
    @SerializedName("selected_option_id") var selectedOptionId: String? = null,
    @SerializedName("is_doubtful") var isDoubtful: Boolean = false,
    @SerializedName("type") val type: String = "multiple_choice",
    @SerializedName("essay_submission_url") var essaySubmissionUrl: String? = null
)

data class CbtOptionItem(
    @SerializedName("key") val key: String, // A, B, C, D, E
    @SerializedName("text") val text: String,
    @SerializedName("image_url", alternate = ["image", "option_image"]) val imageUrl: String? = null
)

// === Teacher Exam & Question Authoring Models ===

data class TeacherCreateQuestionInput(
    val id: String = java.util.UUID.randomUUID().toString(),
    var questionText: String = "",
    var imageUrl: String? = null,
    var options: List<TeacherCreateOptionInput> = listOf(
        TeacherCreateOptionInput(key = "A", text = ""),
        TeacherCreateOptionInput(key = "B", text = ""),
        TeacherCreateOptionInput(key = "C", text = ""),
        TeacherCreateOptionInput(key = "D", text = ""),
        TeacherCreateOptionInput(key = "E", text = "")
    ),
    var correctAnswer: String = "A",
    var scoreWeight: Double = 10.0,
    var explanation: String = ""
)

data class TeacherCreateOptionInput(
    val key: String,
    var text: String = "",
    var imageUrl: String? = null
)

data class TeacherCreateExamRequest(
    @SerializedName("title") val title: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("target_class") val targetClass: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("passing_score") val passingScore: Double,
    @SerializedName("token") val token: String,
    @SerializedName("questions") val questions: List<TeacherQuestionPayload>
)

data class TeacherQuestionPayload(
    @SerializedName("question_text") val questionText: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("options") val options: List<CbtOptionItem>,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("weight") val weight: Double = 10.0
)

data class CbtSubmitRequest(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("answers") val answers: Map<String, String>,
    @SerializedName("total_violations") val totalViolations: Int,
    @SerializedName("time_spent_seconds") val timeSpentSeconds: Long
)

data class CbtSubmitResponse(
    @SerializedName("status") val status: String,
    @SerializedName("score") val score: Double,
    @SerializedName("passed") val passed: Boolean,
    @SerializedName("message") val message: String
)

// === FASE 25: CBT Enterprise Maximization Models ===

data class CbtEncryptedPayloadResponse(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("encrypted_data") val encryptedData: String,
    @SerializedName("iv") val iv: String,
    @SerializedName("format") val format: String,
    @SerializedName("fetched_at") val fetchedAt: String
)

data class CbtDecryptionKeyResponse(
    @SerializedName("accessible") val accessible: Boolean,
    @SerializedName("exam_id") val examId: Long?,
    @SerializedName("key") val key: String?,
    @SerializedName("valid_until") val validUntil: String?,
    @SerializedName("message") val message: String? = null
)

data class CbtMicroSyncRequest(
    @SerializedName("answers") val answers: Map<String, String>
)

data class CbtMicroSyncResponse(
    @SerializedName("synced_count") val syncedCount: Int,
    @SerializedName("synced_at") val syncedAt: String,
    @SerializedName("status") val status: String
)

data class CbtEssayUploadResponse(
    @SerializedName("submission_id") val submissionId: Long,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("question_id") val questionId: Long
)

data class ProctorCommand(
    val action: String, // WARNING_MODAL, FORCE_SUBMIT, EXTEND_TIME
    val message: String? = null,
    val extraMinutes: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// === FASE 26: Token-Gated & Force-Close Models ===

data class CbtTokenValidationRequest(
    @SerializedName("token") val token: String
)

data class CbtTokenValidationResponse(
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("exam_id") val examId: Long? = null,
    @SerializedName("attempt_status") val attemptStatus: String? = null
)

data class CbtTokenInfoResponse(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_expires_at") val tokenExpiresAt: String? = null,
    @SerializedName("remaining_seconds") val remainingSeconds: Int = 300,
    @SerializedName("is_expired") val isExpired: Boolean = false,
    @SerializedName("duration_minutes") val durationMinutes: Int = 5
)

data class CbtForceCloseRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("answers_snapshot") val answersSnapshot: Map<String, String>
)

data class CbtForceCloseResponse(
    @SerializedName("status") val status: String,
    @SerializedName("answers_saved") val answersSaved: Boolean,
    @SerializedName("force_close_reason") val forceCloseReason: String? = null
)

sealed class TokenValidationState {
    object Idle : TokenValidationState()
    object Loading : TokenValidationState()
    data class Success(val message: String) : TokenValidationState()
    data class Error(val message: String) : TokenValidationState()
}

