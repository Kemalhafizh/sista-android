package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/*
 * FASE 77 — Sesi Kelas Hidup & Presensi Per-Mapel.
 *
 * Wire models for the backend FASE 117 endpoints (`teacher/class-sessions/…`,
 * `student/class-session/…`, `admin/class-sessions/…`). The exact contract,
 * including the error codes and the QR payload format, is written down in
 * android_implementation.md → "77.0 Kontrak API". Every field the server may
 * omit is nullable or has a default, so a partial response never crashes Gson
 * into a non-null Kotlin field.
 */

enum class ClassSessionStatus {
    @SerializedName("scheduled") SCHEDULED,
    @SerializedName("active") ACTIVE,
    @SerializedName("completed") COMPLETED,
    @SerializedName("cancelled") CANCELLED,
    @SerializedName("auto_closed") AUTO_CLOSED;

    val isFinished: Boolean get() = this == COMPLETED || this == CANCELLED || this == AUTO_CLOSED
}

enum class SessionAttendanceStatus(val wireValue: String) {
    @SerializedName("hadir") HADIR("hadir"),
    @SerializedName("telat") TELAT("telat"),
    @SerializedName("sakit") SAKIT("sakit"),
    @SerializedName("izin") IZIN("izin"),
    @SerializedName("alpha") ALPHA("alpha");

    /** Counted as physically present in the class. */
    val isPresent: Boolean get() = this == HADIR || this == TELAT
}

enum class SessionCheckInMethod {
    @SerializedName("qr_scan") QR_SCAN,
    @SerializedName("manual_teacher") MANUAL_TEACHER,
    @SerializedName("auto_alpha") AUTO_ALPHA,
    @SerializedName("admin_override") ADMIN_OVERRIDE
}

/**
 * One teaching slot of the day. `GET teacher/class-sessions/today` returns one
 * item per schedule slot, started or not: before the teacher presses
 * "Mulai Kelas" there is no `class_sessions` row yet, so [sessionId] is null
 * and [status] is `scheduled`. Admin listings always carry a [sessionId].
 *
 * The five `*_count` fields are disjoint and add up to [totalStudents].
 */
data class ClassSessionDto(
    @SerializedName("schedule_id") val scheduleId: Long = 0,
    @SerializedName("session_id") val sessionId: Long? = null,
    @SerializedName("session_uuid") val sessionUuid: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("session_date") val sessionDate: String? = null,
    @SerializedName("jam_ke") val jamKe: Int? = null,
    @SerializedName("scheduled_start") val scheduledStart: String? = null,
    @SerializedName("scheduled_end") val scheduledEnd: String? = null,
    @SerializedName("actual_start") val actualStart: String? = null,
    @SerializedName("actual_end") val actualEnd: String? = null,
    @SerializedName("status") val status: ClassSessionStatus? = null,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("hadir_count") val hadirCount: Int = 0,
    @SerializedName("telat_count") val telatCount: Int = 0,
    @SerializedName("sakit_count") val sakitCount: Int = 0,
    @SerializedName("izin_count") val izinCount: Int = 0,
    @SerializedName("alpha_count") val alphaCount: Int = 0,
    @SerializedName("is_auto_closed") val isAutoClosed: Boolean = false
) {
    /** A missing status means the slot has not been started. */
    val effectiveStatus: ClassSessionStatus get() = status ?: ClassSessionStatus.SCHEDULED
}

/**
 * `GET teacher/class-sessions/{sessionId}/qr`. [qrPayload] is opaque to the
 * app: it is drawn as-is and a student's scan sends it back as-is. It always
 * starts with [ClassSessionContract.QR_PAYLOAD_PREFIX].
 */
data class ClassSessionQrDto(
    @SerializedName("qr_payload") val qrPayload: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("remaining_seconds") val remainingSeconds: Int = 0,
    @SerializedName("rotation_seconds") val rotationSeconds: Int = ClassSessionContract.DEFAULT_QR_ROTATION_SECONDS
)

data class SessionAttendanceDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("student_id") val studentId: Long = 0,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("student_nis") val studentNis: String? = null,
    @SerializedName("status") val status: SessionAttendanceStatus? = null,
    @SerializedName("check_in_method") val checkInMethod: SessionCheckInMethod? = null,
    @SerializedName("checked_in_at") val checkedInAt: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("is_override") val isOverride: Boolean = false,
    @SerializedName("override_by_name") val overrideByName: String? = null,
    @SerializedName("override_at") val overrideAt: String? = null,
    @SerializedName("override_reason") val overrideReason: String? = null
) {
    /** Rows are created as `alpha` when the session starts. */
    val effectiveStatus: SessionAttendanceStatus get() = status ?: SessionAttendanceStatus.ALPHA
}

/** `GET student/class-session/active` — `data` is null when no class of the student is running. */
data class ActiveClassSessionDto(
    @SerializedName("session_id") val sessionId: Long = 0,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("scheduled_start") val scheduledStart: String? = null,
    @SerializedName("scheduled_end") val scheduledEnd: String? = null,
    @SerializedName("already_checked_in") val alreadyCheckedIn: Boolean = false,
    @SerializedName("checked_in_at") val checkedInAt: String? = null
)

/** `POST student/class-session/scan-qr` success payload. */
data class ScanQrResultDto(
    @SerializedName("status") val status: SessionAttendanceStatus? = null,
    @SerializedName("checked_in_at") val checkedInAt: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null
)

data class BulkAttendanceResultDto(
    @SerializedName("updated") val updated: Int = 0,
    @SerializedName("failed") val failed: Int = 0,
    @SerializedName("errors") val errors: List<String>? = null
)

/** Laravel `LengthAwarePaginator` as returned inside the `data` envelope. */
data class ClassSessionPageDto(
    @SerializedName("data") val items: List<ClassSessionDto>? = null,
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("total") val total: Int = 0
)

data class AttendanceReportDto(
    @SerializedName("summary") val summary: AttendanceReportSummaryDto? = null,
    @SerializedName("details") val details: List<AttendanceReportRowDto>? = null
)

data class AttendanceReportSummaryDto(
    @SerializedName("total_sessions") val totalSessions: Int = 0,
    @SerializedName("average_presence_rate") val averagePresenceRate: Double = 0.0,
    @SerializedName("total_hadir") val totalHadir: Int = 0,
    @SerializedName("total_telat") val totalTelat: Int = 0,
    @SerializedName("total_sakit") val totalSakit: Int = 0,
    @SerializedName("total_izin") val totalIzin: Int = 0,
    @SerializedName("total_alpha") val totalAlpha: Int = 0
)

/** One row of the report; [label] is the student, class or subject name depending on `group_by`. */
data class AttendanceReportRowDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("hadir") val hadir: Int = 0,
    @SerializedName("telat") val telat: Int = 0,
    @SerializedName("sakit") val sakit: Int = 0,
    @SerializedName("izin") val izin: Int = 0,
    @SerializedName("alpha") val alpha: Int = 0,
    @SerializedName("presence_rate") val presenceRate: Double = 0.0
)

// ── Requests ────────────────────────────────────────────────────────────────

data class StartClassSessionRequest(
    @SerializedName("topic") val topic: String? = null
)

data class EndClassSessionRequest(
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("topic") val topic: String? = null
)

data class AttendanceMarkRequest(
    @SerializedName("student_id") val studentId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String? = null
)

data class BulkAttendanceRequest(
    @SerializedName("attendances") val attendances: List<AttendanceMarkRequest>
)

data class ScanClassQrRequest(
    @SerializedName("qr_payload") val qrPayload: String
)

data class OverrideAttendanceRequest(
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String
)

// ── Errors ──────────────────────────────────────────────────────────────────

/**
 * Error envelopes are `{ success: false, message, data: { error_code, ... } }`
 * (or Laravel's own `{ message, errors }` for validation). [errorCode] is one
 * of [ClassSessionContract.ErrorCode]; when the server sends none, the app
 * falls back to the HTTP status.
 */
data class ClassSessionErrorDataDto(
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("checked_in_at") val checkedInAt: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("session") val session: ClassSessionDto? = null
)

enum class ClassSessionErrorKind {
    /** HTTP 404 without an error code: the route does not exist, i.e. FASE 117 is not deployed. */
    NOT_DEPLOYED,
    NETWORK,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    GONE,
    VALIDATION,
    SERVER,
    UNKNOWN
}

data class ClassSessionError(
    val kind: ClassSessionErrorKind,
    val message: String,
    val httpCode: Int? = null,
    val errorCode: String? = null,
    val checkedInAt: String? = null,
    val subjectName: String? = null,
    val existingSession: ClassSessionDto? = null
)

sealed interface ClassSessionResult<out T> {
    data class Success<out T>(val data: T) : ClassSessionResult<T>
    data class Failure(val error: ClassSessionError) : ClassSessionResult<Nothing>
}

object ClassSessionContract {
    /** Every class-session QR starts with this; anything else is some other QR. */
    const val QR_PAYLOAD_PREFIX = "sista-cs:"
    const val DEFAULT_QR_ROTATION_SECONDS = 30

    object ErrorCode {
        const val OUTSIDE_SCHEDULE_WINDOW = "outside_schedule_window"
        const val NOT_YOUR_SCHEDULE = "not_your_schedule"
        const val SESSION_ALREADY_EXISTS = "session_already_exists"
        const val SESSION_NOT_ACTIVE = "session_not_active"
        const val SESSION_NOT_FOUND = "session_not_found"
        const val QR_INVALID = "qr_invalid"
        const val QR_EXPIRED = "qr_expired"
        const val NOT_ENROLLED = "not_enrolled"
        const val ALREADY_CHECKED_IN = "already_checked_in"
        const val SESSION_CLOSED = "session_closed"
        const val TRANSITION_NOT_ALLOWED = "transition_not_allowed"
        const val REASON_TOO_SHORT = "reason_too_short"
    }
}
