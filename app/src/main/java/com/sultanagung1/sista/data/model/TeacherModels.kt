package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Teacher / Guru role in SMA Islam Sultan Agung 1 Semarang
 */
data class TeacherClassItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("total_students") val totalStudents: Int,
    @SerializedName("academic_year") val academicYear: String = "2025/2026",
    @SerializedName("phase") val phase: String = "Fase F (Kelas XII)"
)

data class TeacherScheduleItem(
    @SerializedName("id") val id: String,
    @SerializedName("day") val day: String,
    @SerializedName("time_slot") val timeSlot: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("room") val room: String,
    @SerializedName("is_active_now") val isActiveNow: Boolean = false,
    @SerializedName("attendance_completed") val attendanceCompleted: Boolean = false
)

data class StudentAttendanceInputItem(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("nisn") val nisn: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String = "L",
    @SerializedName("status") var status: String = "Hadir", // Hadir, Izin, Sakit, Alpha
    @SerializedName("notes") var notes: String = ""
)

data class ClassAttendanceSubmitRequest(
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("class_id") val classId: String,
    @SerializedName("date") val date: String,
    @SerializedName("attendances") val attendances: List<StudentAttendanceInputItem>
)

data class TeachingJournalItem(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("competency_code") val competencyCode: String,
    @SerializedName("notes") val notes: String,
    @SerializedName("attendance_summary") val attendanceSummary: String = "35 Hadir, 1 Sakit",
    @SerializedName("is_signed") val isSigned: Boolean = true
)

data class TeachingJournalCreateRequest(
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("competency_code") val competencyCode: String,
    @SerializedName("notes") val notes: String
)

data class TeacherDashboardData(
    @SerializedName("teacher_name") val teacherName: String,
    @SerializedName("nip") val nip: String,
    @SerializedName("teaching_hours_this_week") val teachingHoursThisWeek: Int = 24,
    @SerializedName("total_classes") val totalClasses: Int = 5,
    @SerializedName("today_schedules") val todaySchedules: List<TeacherScheduleItem>,
    @SerializedName("recent_journals") val recentJournals: List<TeachingJournalItem>
)
