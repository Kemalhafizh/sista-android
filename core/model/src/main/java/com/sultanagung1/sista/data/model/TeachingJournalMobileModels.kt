package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/** Real weekly recurring teaching slot — GET teacher/schedule (ScheduleResource). */
data class TeacherScheduleSlot(
    val id: Long,
    val day: String?,
    @SerializedName("session_start") val sessionStart: String?,
    @SerializedName("session_end") val sessionEnd: String?,
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("subject_name") val subjectName: String?,
    @SerializedName("classroom_id") val classroomId: Long,
    @SerializedName("classroom_name") val classroomName: String?,
    @SerializedName("room_name") val roomName: String? = null
)

/** Real dated journal entry — GET/POST teacher/journals (TeachingJournalResource). */
data class TeachingJournalEntry(
    val uuid: String,
    @SerializedName("teaching_date") val teachingDate: String?,
    @SerializedName("jam_ke") val jamKe: Int,
    @SerializedName("classroom_id") val classroomId: Long,
    @SerializedName("classroom_name") val classroomName: String?,
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("subject_name") val subjectName: String?,
    val topic: String?,
    @SerializedName("learning_activity") val learningActivity: String?,
    @SerializedName("learning_method") val learningMethod: String?,
    val obstacles: String? = null,
    val notes: String? = null,
    @SerializedName("students_present") val studentsPresent: Int,
    @SerializedName("students_absent") val studentsAbsent: Int,
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
    val status: String?
)

/** Real request body — POST teacher/journals. */
data class StoreJournalRequest(
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("classroom_id") val classroomId: Long,
    @SerializedName("teaching_date") val teachingDate: String,
    @SerializedName("jam_ke") val jamKe: Int,
    val topic: String,
    @SerializedName("learning_activity") val learningActivity: String,
    @SerializedName("learning_method") val learningMethod: String,
    val obstacles: String? = null,
    val notes: String? = null,
    @SerializedName("students_present") val studentsPresent: Int,
    @SerializedName("students_absent") val studentsAbsent: Int
)

// --- UI-facing shapes consumed by TeachingJournalMobileScreen / JournalFormScreen ---
// Kept separate from the wire models above so the screens stay simple, but every
// instance is now built from real TeacherScheduleSlot/TeachingJournalEntry data —
// no more hardcoded sample fallback.

data class JournalScheduleItem(
    val id: String,
    val subject: String,
    val className: String,
    val isFilled: Boolean,
    val subjectId: Long,
    val classroomId: Long,
    val jamKe: Int,
    /** "07:00–08:30" from the schedule; null for a filed journal or a slot without times. */
    val timeRange: String? = null,
    val date: String? = null,
    val topic: String? = null,
    val method: String? = null,
    val media: String? = null,
    val attendancePresent: Int? = null,
    val attendanceAbsent: Int? = null,
    val isCompetencyAchieved: Boolean = true,
    val notes: String? = null,
    val followUp: String? = null,
    /** A real journal entry can no longer be edited — the backend exposes no update endpoint. */
    val isEditable: Boolean = true,
    /** `teaching_journals.status` (draft, submitted, reviewed) once a journal exists. */
    val status: String? = null
)

data class JournalSummaryCompliance(
    val totalScheduled: Int,
    val filledCount: Int,
    val compliancePercentage: Int,
    val unfilledDaysWarning: Boolean = false
)
