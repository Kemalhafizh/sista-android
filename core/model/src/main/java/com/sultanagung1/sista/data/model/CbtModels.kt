package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * GET student/cbt/exams — mirrors ApiStudentController::cbtExams(). Only
 * published (or completed) exams of the student's class are listed. What the
 * server leaves out is null here, not a guessed subject, duration or pass mark.
 */
data class CbtExamItem(
    @SerializedName("id") val id: Long,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("subject") val subject: String? = null,
    /** uts, uas, ulangan_harian or try_out, upper-cased by the server. */
    @SerializedName("type") val type: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_questions") val totalQuestions: Int? = null,
    @SerializedName("passing_score") val passingScore: Double? = null,
    @SerializedName("status") val status: String? = null, // "published" or "completed" — not a lifecycle state
    /** ISO-8601. */
    @SerializedName("starts_at") val startsAt: String? = null,
    @SerializedName("ends_at") val endsAt: String? = null,
    /** upcoming, ongoing or ended; older servers only send [isOngoing]. */
    @SerializedName("availability_code") val availabilityCode: String? = null,
    @SerializedName("is_ongoing") val isOngoing: Boolean = false,
    @SerializedName("has_attempted") val hasAttempted: Boolean = false,
    /** in_progress, submitted, graded, blocked_violation, timeout or force_closed. */
    @SerializedName("attempt_status") val attemptStatus: String? = null,
    @SerializedName("score") val score: Double? = null
)

/** The "exam" object nested in GET student/cbt/exams/{id}/questions's response. */
data class CbtExamMeta(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("total_questions") val totalQuestions: Int? = null,
    /** When this student's attempt runs out (ISO-8601); null before the token is entered. */
    @SerializedName("ends_at") val endsAt: String? = null,
    @SerializedName("remaining_seconds") val remainingSeconds: Long? = null
)

data class CbtExamQuestionsResponse(
    @SerializedName("exam") val exam: CbtExamMeta,
    @SerializedName("questions") val questions: List<CbtQuestionItem>
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

/**
 * POST teacher/cbt/exams — mirrors ApiTeacherController::storeExamWithQuestions().
 * The previous shape sent `subject_name`, `target_class` and a client-made
 * `token`, none of which the server accepts: the subject and class are real
 * ids (from the teacher's own schedule), and the entry token is only ever
 * issued by the server (GET teacher/cbt/exams/{id}/token, 5-minute lifetime).
 */
data class TeacherCreateExamRequest(
    @SerializedName("title") val title: String,
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("classroom_id") val classroomId: Long,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("passing_score") val passingScore: Double,
    @SerializedName("max_violations") val maxViolations: Int,
    @SerializedName("mobile_only") val mobileOnly: Boolean,
    @SerializedName("shuffle_questions") val shuffleQuestions: Boolean,
    @SerializedName("shuffle_options") val shuffleOptions: Boolean,
    @SerializedName("questions") val questions: List<TeacherQuestionPayload>
)

data class TeacherQuestionPayload(
    @SerializedName("question_text") val questionText: String,
    // Must be an http(s) URL returned by POST teacher/cbt/upload-image —
    // the server rejects a device-local content:// path.
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("options") val options: List<CbtOptionItem>,
    @SerializedName("correct_answer") val correctAnswer: String
)

/** Data of the POST teacher/cbt/exams 201 response. */
data class TeacherCreatedExam(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("classroom") val classroom: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("total_questions") val totalQuestions: Int,
    // Same rule the token endpoint enforces — only open the proctor screen when true.
    @SerializedName("can_view_token") val canViewToken: Boolean = false
)

/** Data of the POST teacher/cbt/upload-image response. */
data class CbtQuestionImageUpload(
    @SerializedName("path") val path: String,
    @SerializedName("image_url") val imageUrl: String
)

/** A picked image read into memory, ready to send as multipart `image`. */
class CbtImageAttachment(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
)

data class CbtSubmitRequest(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("answers") val answers: Map<String, String>,
    @SerializedName("total_violations") val totalViolations: Int,
    /** Null when the exam's duration or the clock is unknown, rather than a guess. */
    @SerializedName("time_spent_seconds") val timeSpentSeconds: Long? = null
)

/** POST student/cbt/exams/{id}/submit response — mirrors ApiStudentController::cbtSubmitExam() exactly. */
data class CbtSubmitResponse(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("correct_answers") val correctAnswers: Int,
    @SerializedName("wrong_answers") val wrongAnswers: Int,
    @SerializedName("score") val score: Double,
    @SerializedName("passed") val passed: Boolean,
    @SerializedName("submitted_at") val submittedAt: String
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
    @SerializedName("attempt_status") val attemptStatus: String? = null,
    // FASE 72.2: the student's own numeric id, needed to subscribe to
    // private-cbt.exam.{examId}.student.{studentId} for proctor interventions.
    @SerializedName("student_id") val studentId: Long? = null,
    // The exam's real lockout threshold — must drive CbtAntiCheatEngine's
    // maxViolationsAllowed instead of a hardcoded value, since it's
    // configurable per-exam and the backend enforces this exact number.
    @SerializedName("max_violations") val maxViolations: Int? = null,
    /** The attempt's own clock: its duration from the first start, cut off by the exam's end. */
    @SerializedName("ends_at") val endsAt: String? = null,
    @SerializedName("remaining_seconds") val remainingSeconds: Long? = null
)

data class CbtTokenInfoResponse(
    @SerializedName("exam_id") val examId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_expires_at") val tokenExpiresAt: String? = null,
    // From the server; a token without them is shown without a countdown, not as 5 minutes.
    @SerializedName("remaining_seconds") val remainingSeconds: Int? = null,
    @SerializedName("is_expired") val isExpired: Boolean = false,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null
)

/**
 * A student the anti-cheat locked out of an exam (`teacher/cbt/exams/{id}/locked-students`).
 * [reasonCode] is app_minimized, app_closed, split_screen or back_button_pressed;
 * [reason] is the server's label for it in the user's language.
 */
data class LockedExamStudent(
    @SerializedName("student_id") val studentId: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("nis") val nis: String? = null,
    @SerializedName("classroom") val classroom: String? = null,
    @SerializedName("reason_code") val reasonCode: String? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("violation_count") val violationCount: Int? = null,
    @SerializedName("locked_at") val lockedAt: String? = null,
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

// === FASE 87: Teacher Proctor — Live Token Distribution & Student Reset ===

data class CbtApiEnvelope<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

/**
 * GET teacher/cbt/exams — mirrors CbtAccessControlApiController::teacherExams().
 * Only exams this teacher can proctor (UTS/UAS as operator, others as creator),
 * published and not yet ended.
 */
data class TeacherCbtExamItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("classroom") val classroom: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int = 0,
    @SerializedName("total_questions") val totalQuestions: Int = 0,
    @SerializedName("mobile_only") val mobileOnly: Boolean = false,
    @SerializedName("is_ongoing") val isOngoing: Boolean = false
)

data class CbtResetStudentRequest(
    @SerializedName("student_id") val studentId: Long
)

data class CbtResetStudentData(
    @SerializedName("exam_id") val examId: Long = 0,
    @SerializedName("student_id") val studentId: Long = 0,
    @SerializedName("attempt_status") val attemptStatus: String? = null,
    @SerializedName("force_closed") val forceClosed: Boolean = false
)

sealed class TokenValidationState {
    object Idle : TokenValidationState()
    object Loading : TokenValidationState()
    data class Success(val message: String) : TokenValidationState()
    data class Error(val message: String) : TokenValidationState()
}

