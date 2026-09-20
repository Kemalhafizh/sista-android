package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    @SerializedName("id") val id: Long,
    @SerializedName("day") val day: String,
    @SerializedName("session_start") val startTime: String,
    @SerializedName("session_end") val endTime: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("teacher_name") val teacherName: String,
    @SerializedName("classroom_name") val room: String
)

/** Minimal projection of NotificationResource.php — enough to compute an unread count without pulling in the full notification-center model. */
data class NotificationSummaryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("read_at") val readAt: String? = null
)

data class GradeItem(
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("teacher_name") val teacherName: String,
    @SerializedName("assignment_score") val assignmentScore: Double,
    @SerializedName("midterm_score") val midtermScore: Double,
    @SerializedName("final_score") val finalScore: Double,
    @SerializedName("average_score") val averageScore: Double,
    @SerializedName("letter_grade") val letterGrade: String,
    @SerializedName("status") val status: String
)

data class AcademicSummary(
    @SerializedName("gpa") val gpa: Double,
    @SerializedName("total_credits") val totalCredits: Int,
    @SerializedName("rank_in_class") val rankInClass: Int,
    @SerializedName("total_students") val totalStudents: Int,
    @SerializedName("grades") val grades: List<GradeItem>
)

data class SubjectMaterial(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String, // PDF, VIDEO, SLIDE
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("created_at") val createdAt: String
)

data class AssignmentItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("is_submitted") val isSubmitted: Boolean,
    @SerializedName("score") val score: Double? = null
)
