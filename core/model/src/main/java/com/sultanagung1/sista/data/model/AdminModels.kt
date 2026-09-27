package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Admin / Principal / Kepsek role in SMA Islam Sultan Agung 1 Semarang.
 *
 * AdminMobileDashboardController::dashboard only returns total_students,
 * unpaid_billings, attendance_rate, and a pending_approvals COUNT (not a
 * list) — there is no backend source for principal_name, academic_year,
 * teachers_present_today, active_cbt_exams_count, spp_collection_rate,
 * critical_alerts, or a detailed pending-approvals list. Fields below that
 * have no backend source are intentionally absent rather than defaulted to
 * a plausible-looking number.
 */
data class AdminDashboardData(
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("unpaid_billings") val unpaidBillingsTotal: Double = 0.0,
    @SerializedName("attendance_rate") val attendanceRateToday: Double? = null,
    @SerializedName("pending_approvals") val pendingApprovalsCount: Int = 0
)

/**
 * FASE 71.4 follow-up (2026-09-22): AdminMobileDashboardController::kpiDetail()
 * now computes sppPaymentRatioByCohort and cbtServerUsage for real (previously
 * the backend comment literally said "Dummy or expanded KPI details").
 * teacherAttendanceRate stays null on purpose — there is still no backing
 * data anywhere in the schema for a teacher's own attendance (Attendance is
 * student_id-scoped only); teacherAttendanceRateNote carries the backend's
 * explanation for why, instead of a fabricated percentage.
 */
data class SchoolKpiSummary(
    @SerializedName("monthly_revenue") val monthlyRevenue: Double = 0.0,
    @SerializedName("active_teachers") val activeTeachers: Int = 0,
    @SerializedName("avg_student_points") val avgStudentPoints: Double = 0.0,
    @SerializedName("spp_payment_ratio_by_cohort") val sppPaymentRatioByCohort: List<SppCohortRatio> = emptyList(),
    @SerializedName("cbt_server_usage") val cbtServerUsage: CbtServerUsage? = null,
    @SerializedName("teacher_attendance_rate") val teacherAttendanceRate: Double? = null,
    @SerializedName("teacher_attendance_rate_note") val teacherAttendanceRateNote: String? = null
)

data class SppCohortRatio(
    @SerializedName("academic_year") val academicYear: String,
    @SerializedName("total_billings") val totalBillings: Int,
    @SerializedName("paid_billings") val paidBillings: Int,
    @SerializedName("paid_ratio_percent") val paidRatioPercent: Double
)

data class CbtServerUsage(
    @SerializedName("active_sessions") val activeSessions: Int,
    @SerializedName("sessions_today") val sessionsToday: Int
)

/** FASE 71.4 follow-up: real pending-approvals list (previously only a count). */
data class PendingApprovalItem(
    @SerializedName("id") val id: Long,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("type_name") val typeName: String,
    @SerializedName("requester_name") val requesterName: String,
    @SerializedName("requester_role") val requesterRole: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("current_step") val currentStep: Int,
    @SerializedName("total_steps") val totalSteps: Int? = null,
    @SerializedName("due_at") val dueAt: String? = null,
    @SerializedName("is_overdue") val isOverdue: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

/** FASE 71.4 — POST body for AdminMobileDashboardController::broadcastEmergency. */
data class EmergencyBroadcastRequest(
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("location") val location: String? = null
)

data class EmergencyBroadcastData(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("location") val location: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("issued_by") val issuedBy: String
)
