package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class CounselingSessionItem(
    val id: Long,
    val uuid: String? = null,
    @SerializedName("student_id") val studentId: Long? = null,
    @SerializedName("counselor_id") val counselorId: Long? = null,
    val category: String = "akademik",
    val status: String = "scheduled", // requested, scheduled, completed, cancelled
    @SerializedName("scheduled_at") val scheduledAt: String? = null,
    val reason: String? = null,
    @SerializedName("session_notes") val sessionNotes: String? = null,
    @SerializedName("action_plan") val actionPlan: String? = null,
    @SerializedName("is_confidential") val isConfidential: Boolean = true,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CounselingAtRiskStudentItem(
    @SerializedName("student_id") val studentId: Long,
    val name: String,
    val nisn: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("risk_level") val riskLevel: String, // 'tinggi', 'sedang', 'rendah'
    @SerializedName("discipline_score") val disciplineScore: Int,
    val reason: String
)

data class CounselingStatistics(
    @SerializedName("total_this_month") val totalThisMonth: Int = 0,
    @SerializedName("completed_this_month") val completedThisMonth: Int = 0,
    @SerializedName("pending_requests") val pendingRequests: Int = 0
)

data class CounselorDashboardData(
    @SerializedName("today_sessions") val todaySessions: List<CounselingSessionItem> = emptyList(),
    @SerializedName("at_risk_students") val atRiskStudents: List<CounselingAtRiskStudentItem> = emptyList(),
    val statistics: CounselingStatistics = CounselingStatistics()
)

data class CreateCounselingSessionRequest(
    @SerializedName("student_id") val studentId: Long,
    val category: String,
    val notes: String? = null,
    @SerializedName("action_plan") val actionPlan: String? = null,
    @SerializedName("is_confidential") val isConfidential: Boolean = true
)

data class RequestAppointmentRequest(
    val topic: String,
    @SerializedName("preferred_date") val preferredDate: String? = null,
    val category: String = "pribadi"
)

data class CounselingApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
