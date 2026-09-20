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

data class SchoolKpiSummary(
    @SerializedName("monthly_revenue") val monthlyRevenue: Double = 0.0,
    @SerializedName("active_teachers") val activeTeachers: Int = 0,
    @SerializedName("avg_student_points") val avgStudentPoints: Double = 0.0
)
