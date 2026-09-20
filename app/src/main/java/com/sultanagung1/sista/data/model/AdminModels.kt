package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Admin / Principal / Kepsek role in SMA Islam Sultan Agung 1 Semarang
 */
data class SchoolKpiSummary(
    @SerializedName("total_students") val totalStudents: Int = 1080,
    @SerializedName("total_teachers") val totalTeachers: Int = 64,
    @SerializedName("attendance_rate_today") val attendanceRateToday: Double = 98.4,
    @SerializedName("spp_collection_rate") val sppCollectionRate: Double = 94.2,
    @SerializedName("teachers_present_today") val teachersPresentToday: Int = 62,
    @SerializedName("active_cbt_exams_count") val activeCbtExamsCount: Int = 3
)

data class CriticalAlertItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("severity") val severity: String, // critical, warning, info
    @SerializedName("description") val description: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("action_label") val actionLabel: String = "Tinjau"
)

data class ApprovalRequestItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String, // Cuti Guru, Pengadaan Lab, Izin Kegiatan
    @SerializedName("requester_name") val requesterName: String,
    @SerializedName("department") val department: String,
    @SerializedName("submitted_date") val submittedDate: String,
    @SerializedName("description") val description: String,
    @SerializedName("status") var status: String = "Menunggu Persetujuan" // Menunggu Persetujuan, Disetujui, Ditolak
)

data class AdminDashboardData(
    @SerializedName("principal_name") val principalName: String,
    @SerializedName("academic_year") val academicYear: String = "2025/2026 Ganjil",
    @SerializedName("kpi") val kpi: SchoolKpiSummary,
    @SerializedName("critical_alerts") val criticalAlerts: List<CriticalAlertItem>,
    @SerializedName("pending_approvals") val pendingApprovals: List<ApprovalRequestItem>
)
