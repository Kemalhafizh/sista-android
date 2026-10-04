package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class DailyAssessmentItem(
    val id: Long,
    val title: String,
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("classroom_id") val classroomId: Long? = null,
    @SerializedName("assessment_date") val assessmentDate: String,
    val kkm: Double = 75.0,
    @SerializedName("max_score") val maxScore: Double = 100.0,
    val type: String = "ulangan_harian",
    val description: String? = null,
    @SerializedName("scores_count") val scoresCount: Int = 0,
    /** Students with an entered score; blank rows the web creates are not counted. */
    @SerializedName("scored_count") val scoredCount: Int? = null,
    val subject: NamedRef? = null,
    val classroom: NamedRef? = null
)

data class CreateAssessmentRequest(
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("classroom_id") val classroomId: Long,
    val title: String,
    @SerializedName("assessment_date") val assessmentDate: String,
    val kkm: Double = 75.0,
    @SerializedName("max_score") val maxScore: Double = 100.0,
    val type: String = "ulangan_harian",
    val description: String? = null
)

data class StudentScoreInput(
    @SerializedName("student_id") val studentId: Long,
    val score: Double,
    val notes: String? = null
)

data class BatchScoreRequest(
    val scores: List<StudentScoreInput>
)

data class BatchScoreResult(
    @SerializedName("total_saved") val totalSaved: Int,
    @SerializedName("students_under_kkm") val studentsUnderKkm: Int,
    val kkm: Double
)

data class RemedialItem(
    val id: Long,
    val uuid: String? = null,
    @SerializedName("assessment_title") val assessmentTitle: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("original_score") val originalScore: Double,
    val kkm: Double,
    @SerializedName("remedial_score") val remedialScore: Double? = null,
    @SerializedName("remedial_type") val remedialType: String = "tugas_tambahan",
    val status: String = "pending",
    val deadline: String? = null,
    @SerializedName("teacher_notes") val teacherNotes: String? = null
)

/**
 * GET assessments/student. Title, subject and date are null when the assessment
 * or its subject is gone; kkm and isTuntas are null when the assessment has no
 * KKM (the server no longer assumes 75).
 */
data class StudentAssessmentHistoryItem(
    val id: Long,
    @SerializedName("assessment_title") val assessmentTitle: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("assessment_date") val assessmentDate: String? = null,
    val kkm: Double? = null,
    @SerializedName("original_score") val originalScore: Double,
    @SerializedName("final_score") val finalScore: Double,
    @SerializedName("is_tuntas") val isTuntas: Boolean? = null
)

data class AssessmentResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T
)

/** GET assessments/teacher: `{assessments: [...], remedial_dashboard: {...}}`. */
data class TeacherAssessmentsData(
    val assessments: List<DailyAssessmentItem> = emptyList(),
    @SerializedName("remedial_dashboard") val remedialDashboard: RemedialDashboard? = null
)
