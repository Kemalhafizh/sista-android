package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * GET student/class-session/attendance and
 * GET parent/child/{uuid}/class-session-attendance (sistem-terpadu
 * ClassSessionService::dayAttendanceFor): one day's lessons and the
 * student's per-lesson QR attendance (FASE 117).
 */
data class LessonAttendanceDay(
    /** yyyy-MM-dd, the day the server answered for. */
    @SerializedName("date") val date: String? = null,
    @SerializedName("sessions") val sessions: List<LessonAttendanceItem>? = null,
    @SerializedName("summary") val summary: LessonAttendanceSummary? = null,
)

data class LessonAttendanceItem(
    @SerializedName("session_id") val sessionId: Long,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("jam_ke") val jamKe: Int? = null,
    /** HH:mm */
    @SerializedName("scheduled_start") val scheduledStart: String? = null,
    @SerializedName("scheduled_end") val scheduledEnd: String? = null,
    /** scheduled / active / completed / auto_closed */
    @SerializedName("session_status") val sessionStatus: String? = null,
    /** hadir / telat / sakit / izin / alpha; null while nothing is recorded. */
    @SerializedName("status_code") val statusCode: String? = null,
    /** [statusCode] in the user's language. */
    @SerializedName("status") val status: String? = null,
    @SerializedName("check_in_method") val checkInMethod: String? = null,
    /** ISO-8601, set when the student scanned. */
    @SerializedName("checked_in_at") val checkedInAt: String? = null,
    /** The class is running and the student has not scanned yet: the automatic "alpha" can still become "hadir". */
    @SerializedName("awaiting_scan") val awaitingScan: Boolean = false,
)

/** Counts of what has been recorded; lessons still [LessonAttendanceItem.awaitingScan] are not in them. */
data class LessonAttendanceSummary(
    @SerializedName("sessions") val sessions: Int? = null,
    @SerializedName("recorded") val recorded: Int? = null,
    @SerializedName("hadir") val hadir: Int? = null,
    @SerializedName("telat") val telat: Int? = null,
    @SerializedName("sakit") val sakit: Int? = null,
    @SerializedName("izin") val izin: Int? = null,
    @SerializedName("alpha") val alpha: Int? = null,
)
