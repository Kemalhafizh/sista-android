package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/*
 * FASE 77 — Sesi Kelas Hidup & Presensi Per-Mapel.
 *
 * Wire models for backend FASE 117 as merged in sistem-terpadu
 * (ClassSessionResource, SessionAttendanceResource, ClassSessionService;
 * contract: sistem-terpadu docs/modules/class_sessions.md). Every field the
 * server may omit is nullable or has a default, so a partial response never
 * crashes Gson into a non-null Kotlin field.
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
    @SerializedName("auto_alpha") AUTO_ALPHA
}

/**
 * One lesson of the day (`ClassSessionResource`). `teacher/class-sessions/today`
 * materialises a row per timetable entry, so even a lesson that has not been
 * started has an [sessionId] and `status: scheduled`.
 *
 * [presentCount] is hadir + telat, [absentCount] is alpha; sakit/izin are in
 * neither (they are `total_students - present - absent`).
 */
data class ClassSessionDto(
    @SerializedName("id") val sessionId: Long? = null,
    @SerializedName("uuid") val sessionUuid: String? = null,
    @SerializedName("schedule_id") val scheduleId: Long = 0,
    @SerializedName("subject_id") val subjectId: Long? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("classroom_id") val classroomId: Long? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
    @SerializedName("teacher_id") val teacherId: Long? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("session_date") val sessionDate: String? = null,
    @SerializedName("jam_ke") val jamKe: Int? = null,
    @SerializedName("scheduled_start") val scheduledStart: String? = null,
    @SerializedName("scheduled_end") val scheduledEnd: String? = null,
    /** "HH:mm" from which "Mulai Kelas" is allowed (server config, default start − 10 min). */
    @SerializedName("startable_from") val startableFrom: String? = null,
    /** When the scheduler closes a forgotten session (default end + 5 min). */
    @SerializedName("auto_close_at") val autoCloseAt: String? = null,
    @SerializedName("actual_start") val actualStart: String? = null,
    @SerializedName("actual_end") val actualEnd: String? = null,
    @SerializedName("status") val status: ClassSessionStatus? = null,
    /** Server's view at response time; the app re-evaluates the window locally between polls. */
    @SerializedName("can_start") val canStart: Boolean = false,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("present_count") val presentCount: Int = 0,
    @SerializedName("absent_count") val absentCount: Int = 0,
    @SerializedName("is_auto_closed") val isAutoClosed: Boolean = false,
    /** Set when ending the session with a topic created a teaching-journal draft. */
    @SerializedName("teaching_journal_id") val teachingJournalId: Long? = null
) {
    /** A missing status means the slot has not been started. */
    val effectiveStatus: ClassSessionStatus get() = status ?: ClassSessionStatus.SCHEDULED
}

/**
 * `GET teacher/class-sessions/{sessionId}/qr`. [qrToken] ("<session uuid>.<signature>")
 * is drawn as-is and a student's scan sends it back as-is. The same token is
 * served to every device during a rotation window.
 */
data class ClassSessionQrDto(
    @SerializedName("qr_token") val qrToken: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("session_uuid") val sessionUuid: String? = null,
    @SerializedName("remaining_seconds") val remainingSeconds: Int = 0,
    @SerializedName("rotation_seconds") val rotationSeconds: Int = ClassSessionContract.DEFAULT_QR_ROTATION_SECONDS
)

data class SessionAttendanceDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("class_session_id") val classSessionId: Long? = null,
    @SerializedName("student_id") val studentId: Long = 0,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("student_nis") val studentNis: String? = null,
    @SerializedName("status") val status: SessionAttendanceStatus? = null,
    @SerializedName("check_in_method") val checkInMethod: SessionCheckInMethod? = null,
    @SerializedName("checked_in_at") val checkedInAt: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("is_override") val isOverride: Boolean = false,
    /** Name of who corrected it. */
    @SerializedName("override_by") val overrideBy: String? = null,
    @SerializedName("override_at") val overrideAt: String? = null,
    @SerializedName("override_reason") val overrideReason: String? = null
) {
    /** Rows are created as `alpha` when the session starts. */
    val effectiveStatus: SessionAttendanceStatus get() = status ?: SessionAttendanceStatus.ALPHA
}

/** `GET student/class-session/active` — `data` is null when no class of the student is running. */
data class ActiveClassSessionDto(
    @SerializedName("session_id") val sessionId: Long = 0,
    @SerializedName("session_uuid") val sessionUuid: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("scheduled_start") val scheduledStart: String? = null,
    @SerializedName("scheduled_end") val scheduledEnd: String? = null,
    /** The student's row in this session (alpha until scanned or marked). */
    @SerializedName("attendance_status") val attendanceStatus: SessionAttendanceStatus? = null,
    /** False once the student is recorded present (hadir/telat). */
    @SerializedName("can_scan_qr") val canScanQr: Boolean = true
) {
    val alreadyCheckedIn: Boolean get() = !canScanQr
}

/** `POST student/class-session/scan-qr` success payload. */
data class ScanQrResultDto(
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: SessionAttendanceStatus? = null,
    @SerializedName("session_id") val sessionId: Long? = null,
    @SerializedName("session_subject") val subjectName: String? = null,
    @SerializedName("session_class") val classroomName: String? = null,
    @SerializedName("checked_in_at") val checkedInAt: String? = null
)

data class BulkAttendanceResultDto(
    @SerializedName("updated") val updated: Int = 0,
    @SerializedName("failed") val failed: Int = 0,
    @SerializedName("errors") val errors: List<String>? = null
)

/** `GET admin/class-sessions` — `{ items, current_page, last_page, per_page, total }`. */
data class ClassSessionPageDto(
    @SerializedName("items") val items: List<ClassSessionDto>? = null,
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 20,
    @SerializedName("total") val total: Int = 0
)

data class AttendanceReportDto(
    @SerializedName("summary") val summary: AttendanceReportSummaryDto? = null,
    @SerializedName("details") val details: List<AttendanceReportRowDto>? = null
)

data class AttendanceReportSummaryDto(
    @SerializedName("total_sessions") val totalSessions: Int = 0,
    @SerializedName("total_records") val totalRecords: Int = 0,
    @SerializedName("average_presence_rate") val averagePresenceRate: Double = 0.0,
    @SerializedName("total_hadir") val totalHadir: Int = 0,
    @SerializedName("total_telat") val totalTelat: Int = 0,
    @SerializedName("total_sakit") val totalSakit: Int = 0,
    @SerializedName("total_izin") val totalIzin: Int = 0,
    @SerializedName("total_alpha") val totalAlpha: Int = 0
)

/** One row of the report; which name is filled depends on `group_by`. */
data class AttendanceReportRowDto(
    @SerializedName("group_id") val groupId: Long? = null,
    @SerializedName("student_nis") val studentNis: String? = null,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("hadir") val hadir: Int = 0,
    @SerializedName("telat") val telat: Int = 0,
    @SerializedName("sakit") val sakit: Int = 0,
    @SerializedName("izin") val izin: Int = 0,
    @SerializedName("alpha") val alpha: Int = 0,
    @SerializedName("total") val total: Int = 0,
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
    @SerializedName("qr_token") val qrToken: String
)

data class OverrideAttendanceRequest(
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String
)

// ── Errors ──────────────────────────────────────────────────────────────────

/**
 * Error envelopes are `{ success: false, message, data, errors? }`. The server
 * sends no machine error code; the rejection is recognised from the HTTP status
 * and the (stable, documented) message — see [ClassSessionRules.rejectionOf].
 * `data` carries `session_id` when a lesson was already started/ended, and
 * `status`/`checked_in_at` on a second scan.
 */
data class ClassSessionErrorDataDto(
    @SerializedName("session_id") val sessionId: Long? = null,
    @SerializedName("status") val status: SessionAttendanceStatus? = null,
    @SerializedName("checked_in_at") val checkedInAt: String? = null
)

enum class ClassSessionErrorKind {
    /** HTTP 404 outside the API envelope: the route does not exist on this server (FASE 117 not deployed). */
    NOT_DEPLOYED,
    NETWORK,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    VALIDATION,
    RATE_LIMITED,
    SERVER,
    UNKNOWN
}

/** Business-rule refusals of backend FASE 117 the app reacts to. */
enum class ClassSessionRejection {
    OUTSIDE_SCHEDULE_WINDOW,
    NOT_YOUR_SCHEDULE,
    SESSION_ALREADY_STARTED,
    SESSION_ALREADY_ENDED,
    SESSION_NOT_STARTED,
    SESSION_ENDED,
    QR_NOT_ACTIVE,
    MANUAL_NOT_ACTIVE,
    QR_INVALID,
    QR_EXPIRED,
    NOT_ENROLLED,
    ALREADY_CHECKED_IN,
    TRANSITION_NOT_ALLOWED
}

data class ClassSessionError(
    val kind: ClassSessionErrorKind,
    /** The server's own message (Indonesian, user-facing), or empty. */
    val message: String,
    val httpCode: Int? = null,
    val rejection: ClassSessionRejection? = null,
    val checkedInAt: String? = null,
    /** The lesson that already exists when "Mulai Kelas" was pressed twice. */
    val existingSessionId: Long? = null
)

sealed interface ClassSessionResult<out T> {
    data class Success<out T>(val data: T) : ClassSessionResult<T>
    data class Failure(val error: ClassSessionError) : ClassSessionResult<Nothing>
}

object ClassSessionContract {
    const val DEFAULT_QR_ROTATION_SECONDS = 30

    /** `qr_token` = "<session uuid>.<hex signature>" (ClassSessionService::qrTokenFor). */
    val QR_TOKEN_PATTERN = Regex("""^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\.[0-9a-fA-F]{16,128}$""")
}
