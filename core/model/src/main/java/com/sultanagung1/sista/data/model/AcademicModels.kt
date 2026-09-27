package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    @SerializedName("id") val id: Long,
    @SerializedName("day") val day: String,
    @SerializedName("session_start") val startTime: String,
    @SerializedName("session_end") val endTime: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("teacher_name") val teacherName: String,
    /** The class this lesson belongs to (e.g. "XII MIPA 1") — not a room. */
    @SerializedName("classroom_name") val room: String,
    /** The physical room, when the schedule has one. */
    @SerializedName("room_name") val roomName: String? = null
)

/** Minimal projection of NotificationResource.php — enough to compute an unread count without pulling in the full notification-center model. */
data class NotificationSummaryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("read_at") val readAt: String? = null
)

/**
 * GET student/grades — mirrors GradeResource exactly. One entry per graded
 * assessment (e.g. one UH, one UTS, one UAS), not a per-subject rollup — the
 * backend's Grade model has no assignment/midterm/final/GPA/class-rank
 * concept, so those are grouped and derived client-side in GradesScreen
 * rather than expected from the API.
 */
data class GradeEntry(
    @SerializedName("id") val id: Long,
    @SerializedName("subject") val subject: String,
    @SerializedName("type") val type: String, // e.g. "UH", "UTS", "UAS" — whatever the teacher recorded
    @SerializedName("score") val score: Double,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String
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
