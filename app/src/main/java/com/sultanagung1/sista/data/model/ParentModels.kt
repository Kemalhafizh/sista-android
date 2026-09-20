package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Parent / Wali Murid role in SMA Islam Sultan Agung 1 Semarang
 */
data class ChildSummary(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("nisn") val nisn: String,
    @SerializedName("name") val name: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("homeroom_teacher") val homeroomTeacher: String,
    @SerializedName("homeroom_phone") val homeroomPhone: String = "6281234567890",
    @SerializedName("counselor_name") val counselorName: String = "Ustadzah Fatimah, S.Psi (Guru BK)",
    @SerializedName("counselor_phone") val counselorPhone: String = "6289876543210",
    @SerializedName("today_attendance_status") val todayAttendanceStatus: String = "Hadir Tepat Waktu",
    @SerializedName("today_checkin_time") val todayCheckinTime: String = "06:42 WIB",
    @SerializedName("mutabaah_score") val mutabaahScore: Int = 85,
    @SerializedName("gpa_score") val gpaScore: Double = 91.8,
    @SerializedName("attendance_percentage") val attendancePercentage: Double = 98.4,
    @SerializedName("pending_spp_amount") val pendingSppAmount: Long = 0L,
    @SerializedName("spp_status") val sppStatus: String = "Lunas"
)

data class ChildAttendanceLog(
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String,
    @SerializedName("check_in_time") val checkInTime: String,
    @SerializedName("check_out_time") val checkOutTime: String?,
    @SerializedName("gate") val gate: String = "Gerbang Utama Kampus Sultan Agung",
    @SerializedName("is_punctual") val isPunctual: Boolean = true
)

data class ChildGradeSubject(
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("score") val score: Double,
    @SerializedName("kktp") val kktp: Double = 75.0,
    @SerializedName("predicate") val predicate: String = "A",
    @SerializedName("teacher_notes") val teacherNotes: String = "Sangat aktif dan memahami konsep pembelajaran dengan baik."
)

data class ParentDashboardData(
    @SerializedName("parent_name") val parentName: String,
    @SerializedName("children") val children: List<ChildSummary>,
    @SerializedName("recent_announcements") val recentAnnouncements: List<SchoolAnnouncementItem> = emptyList()
)

data class SchoolAnnouncementItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("category") val category: String,
    @SerializedName("summary") val summary: String
)
