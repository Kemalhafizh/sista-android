package com.sultanagung1.sista.data.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * FASE 77: the rules behind the class-session screens, free of Android so
 * they run as plain JVM unit tests. Screens and ViewModels only call these;
 * none of them re-derives a time window or a label on its own.
 *
 * The server stays the authority (FASE 117.6): these rules decide what the
 * app *offers*, and every action is re-validated server-side.
 */
object ClassSessionRules {

    /** "Mulai Kelas" opens this many minutes before the scheduled start (117.6 rule 1). */
    const val START_EARLY_MINUTES = 10
    /** Timer turns amber at 5 minutes left and red at 1 minute (77.3.3). */
    const val WARNING_SECONDS = 5 * 60L
    const val CRITICAL_SECONDS = 60L
    /** The server auto-closes a session this long after the scheduled end (117.5). */
    const val AUTO_CLOSE_GRACE_SECONDS = 5 * 60L

    /** Keep showing the last QR (marked stale) this long after it expired; after that, show "no connection". */
    const val QR_STALE_LIMIT_MS = 60_000L
    const val QR_RETRY_BASE_MS = 5_000L
    const val QR_RETRY_MAX_MS = 30_000L

    const val TEACHER_LIST_POLL_MS = 30_000L
    const val ATTENDANCE_POLL_MS = 10_000L
    const val STUDENT_ACTIVE_POLL_MS = 60_000L

    const val OVERRIDE_REASON_MIN_CHARS = 10
    const val END_NOTES_MAX_CHARS = 500
    const val TOPIC_MAX_CHARS = 200

    /** Choices a teacher has in the manual list (77.4); `telat` is only set by the server. */
    val TEACHER_MARK_OPTIONS = listOf(
        SessionAttendanceStatus.HADIR,
        SessionAttendanceStatus.SAKIT,
        SessionAttendanceStatus.IZIN,
        SessionAttendanceStatus.ALPHA
    )

    // ── Time ────────────────────────────────────────────────────────────

    private val HH_MM = Regex("""(\d{1,2}):(\d{2})""")

    /** "07:05", "07:05:00" or an ISO timestamp → minutes of day; null if unparsable. */
    fun minutesOf(time: String?): Int? {
        if (time.isNullOrBlank()) return null
        val source = time.substringAfter('T', time)
        val match = HH_MM.find(source) ?: return null
        val (h, m) = match.destructured
        val hours = h.toInt()
        val minutes = m.toInt()
        if (hours > 23 || minutes > 59) return null
        return hours * 60 + minutes
    }

    fun formatMinutes(minutesOfDay: Int): String {
        val wrapped = ((minutesOfDay % 1440) + 1440) % 1440
        return String.format(Locale.US, "%02d:%02d", wrapped / 60, wrapped % 60)
    }

    /** "HH:mm" of an ISO timestamp or time string, as sent (server local time, WIB). */
    fun clockOf(time: String?): String? = minutesOf(time)?.let(::formatMinutes)

    fun timeRange(start: String?, end: String?): String {
        val s = clockOf(start) ?: "--:--"
        val e = clockOf(end) ?: "--:--"
        return "$s – $e"
    }

    // ── 77.2 Start button ───────────────────────────────────────────────

    sealed interface StartAction {
        /** Session already started/finished: show "Buka Sesi" or nothing instead. */
        data object Hidden : StartAction
        data object Available : StartAction
        data class NotYet(val opensAt: String) : StartAction
        /** The slot ended and was never started. The server would refuse it too. */
        data object Missed : StartAction
    }

    fun startActionFor(session: ClassSessionDto, nowMinutes: Int): StartAction {
        if (session.effectiveStatus != ClassSessionStatus.SCHEDULED) return StartAction.Hidden
        val start = minutesOf(session.scheduledStart)
        val end = minutesOf(session.scheduledEnd)
        // Unparsable times: let the server decide rather than lock the teacher out.
        if (start == null || end == null) return StartAction.Available
        // The server's own window (config CLASS_SESSION_START_EARLY_MINUTES); 10 min if absent.
        val opensAt = minutesOf(session.startableFrom) ?: (start - START_EARLY_MINUTES)
        return when {
            nowMinutes < opensAt -> StartAction.NotYet(formatMinutes(opensAt))
            nowMinutes >= end -> StartAction.Missed
            else -> StartAction.Available
        }
    }

    /** Active first, then upcoming by start time, then finished (77.2.2). */
    fun sortForTeacher(sessions: List<ClassSessionDto>): List<ClassSessionDto> =
        sessions.sortedWith(
            compareBy<ClassSessionDto> { groupRank(it.effectiveStatus) }
                .thenBy { minutesOf(it.scheduledStart) ?: Int.MAX_VALUE }
                .thenBy { it.jamKe ?: Int.MAX_VALUE }
        )

    private fun groupRank(status: ClassSessionStatus): Int = when (status) {
        ClassSessionStatus.ACTIVE -> 0
        ClassSessionStatus.SCHEDULED -> 1
        else -> 2
    }

    /** 77.7.2: the one class-session action the teacher dashboard offers. */
    sealed interface DashboardAction {
        data class ReturnToClass(val session: ClassSessionDto) : DashboardAction
        data class StartClass(val session: ClassSessionDto) : DashboardAction
        data class NextClass(val session: ClassSessionDto, val opensAt: String) : DashboardAction
        /** Classes today, none left to start. */
        data object AllDone : DashboardAction
        data object NoClassesToday : DashboardAction
    }

    fun dashboardAction(sessions: List<ClassSessionDto>, nowMinutes: Int): DashboardAction {
        if (sessions.isEmpty()) return DashboardAction.NoClassesToday
        sessions.firstOrNull { it.effectiveStatus == ClassSessionStatus.ACTIVE && it.sessionId != null }
            ?.let { return DashboardAction.ReturnToClass(it) }
        val upcoming = sortForTeacher(sessions).filter { it.effectiveStatus == ClassSessionStatus.SCHEDULED }
        upcoming.firstOrNull { startActionFor(it, nowMinutes) == StartAction.Available }
            ?.let { return DashboardAction.StartClass(it) }
        upcoming.firstNotNullOfOrNull { s -> (startActionFor(s, nowMinutes) as? StartAction.NotYet)?.let { s to it.opensAt } }
            ?.let { (s, opensAt) -> return DashboardAction.NextClass(s, opensAt) }
        return DashboardAction.AllDone
    }

    // ── 77.3 Session timer ──────────────────────────────────────────────

    enum class TimerTone { NORMAL, WARNING, CRITICAL, OVERTIME }

    fun timerTone(remainingSeconds: Long): TimerTone = when {
        remainingSeconds <= 0 -> TimerTone.OVERTIME
        remainingSeconds <= CRITICAL_SECONDS -> TimerTone.CRITICAL
        remainingSeconds <= WARNING_SECONDS -> TimerTone.WARNING
        else -> TimerTone.NORMAL
    }

    /** Seconds until [scheduledEnd]; negative once it has passed. Null if the end time is unknown. */
    fun remainingSeconds(scheduledEnd: String?, nowSecondsOfDay: Long): Long? {
        val end = minutesOf(scheduledEnd) ?: return null
        return end * 60L - nowSecondsOfDay
    }

    /** "32:15", "1:05:00", or "-02:10" once overtime. */
    fun formatCountdown(seconds: Long): String {
        val sign = if (seconds < 0) "-" else ""
        val total = abs(seconds)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) String.format(Locale.US, "%s%d:%02d:%02d", sign, h, m, s) else String.format(Locale.US, "%s%02d:%02d", sign, m, s)
    }

    // ── 77.3.2 Rotating QR ──────────────────────────────────────────────

    enum class QrFreshness {
        /** Nothing fetched yet. */
        LOADING,
        FRESH,
        /** Expired but recent: keep it on screen with "mungkin kedaluwarsa — memperbarui…". */
        STALE,
        /** No valid QR for too long: tell the teacher to check the connection. */
        UNAVAILABLE
    }

    /** All times are `SystemClock.elapsedRealtime()`-style monotonic millis. */
    fun qrFreshness(expiresAtMs: Long?, nowMs: Long): QrFreshness = when {
        expiresAtMs == null -> QrFreshness.LOADING
        nowMs < expiresAtMs -> QrFreshness.FRESH
        nowMs - expiresAtMs < QR_STALE_LIMIT_MS -> QrFreshness.STALE
        else -> QrFreshness.UNAVAILABLE
    }

    /** 5 s, 10 s, 20 s, then every 30 s. */
    fun qrRetryDelayMs(consecutiveFailures: Int): Long {
        if (consecutiveFailures <= 0) return QR_RETRY_BASE_MS
        val shift = min(consecutiveFailures - 1, 10)
        return min(QR_RETRY_BASE_MS shl shift, QR_RETRY_MAX_MS)
    }

    /** Refetch when the server says the token rotates; never busy-loop on a bad value. */
    fun nextQrFetchDelayMs(remainingSeconds: Int): Long = max(1, remainingSeconds) * 1000L

    /** "<session uuid>.<signature>"; anything else is some other QR (a gate pass, a URL…). */
    fun isClassSessionQr(raw: String?): Boolean =
        raw != null && ClassSessionContract.QR_TOKEN_PATTERN.matches(raw.trim())

    /**
     * 77.5: the camera reports the same QR many times a second. Send a payload
     * at most once per [cooldownMs], and never again once the server refused
     * it (an expired QR stays expired; the next one on the teacher's screen is
     * a different payload).
     */
    class ScanGate(private val cooldownMs: Long = 3_000L) {
        private var lastPayload: String? = null
        private var lastSentAt = 0L
        private val refused = LinkedHashSet<String>()

        fun shouldSubmit(payload: String, nowMs: Long): Boolean {
            val p = payload.trim()
            if (p in refused) return false
            if (p == lastPayload && nowMs - lastSentAt < cooldownMs) return false
            lastPayload = p
            lastSentAt = nowMs
            return true
        }

        fun markRefused(payload: String) {
            refused += payload.trim()
            // Payloads rotate every 30 s; a short memory is enough.
            while (refused.size > 20) refused.remove(refused.first())
        }
    }

    // ── Attendance counts ───────────────────────────────────────────────

    /**
     * Presence of one session. From the roster every status is known
     * ([breakdown]); from a session summary only present/alpha/total are
     * (`present_count` = hadir + telat, `absent_count` = alpha), and sakit/izin
     * are the rest.
     */
    data class Counts(
        val present: Int = 0,
        val alpha: Int = 0,
        val total: Int = 0,
        val breakdown: Breakdown? = null
    ) {
        data class Breakdown(val hadir: Int, val telat: Int, val sakit: Int, val izin: Int)

        /** Sakit + izin. */
        val excused: Int get() = breakdown?.let { it.sakit + it.izin } ?: (total - present - alpha).coerceAtLeast(0)
        /** 0.0–1.0; 0 when the class has no students. */
        val presenceRate: Double get() = if (total <= 0) 0.0 else present.toDouble() / total
    }

    fun countsOf(rows: List<SessionAttendanceDto>): Counts {
        val byStatus = rows.groupingBy { it.effectiveStatus }.eachCount()
        val hadir = byStatus[SessionAttendanceStatus.HADIR] ?: 0
        val telat = byStatus[SessionAttendanceStatus.TELAT] ?: 0
        return Counts(
            present = hadir + telat,
            alpha = byStatus[SessionAttendanceStatus.ALPHA] ?: 0,
            total = rows.size,
            breakdown = Counts.Breakdown(
                hadir = hadir,
                telat = telat,
                sakit = byStatus[SessionAttendanceStatus.SAKIT] ?: 0,
                izin = byStatus[SessionAttendanceStatus.IZIN] ?: 0
            )
        )
    }

    fun countsOf(session: ClassSessionDto): Counts =
        Counts(present = session.presentCount, alpha = session.absentCount, total = session.totalStudents)

    fun presencePercent(rate: Double): Int = (rate.coerceIn(0.0, 1.0) * 100).let { kotlin.math.round(it).toInt() }

    // ── 77.4 Manual attendance ──────────────────────────────────────────

    /** Only edits that differ from what the server has; reverting an edit removes it. */
    fun pendingChanges(
        original: Map<Long, SessionAttendanceStatus>,
        edits: Map<Long, SessionAttendanceStatus>
    ): Map<Long, SessionAttendanceStatus> = edits.filter { (studentId, status) -> original[studentId] != status }

    fun filterStudents(
        rows: List<SessionAttendanceDto>,
        query: String,
        status: SessionAttendanceStatus?,
        statusOf: (SessionAttendanceDto) -> SessionAttendanceStatus = { it.effectiveStatus }
    ): List<SessionAttendanceDto> {
        val q = query.trim()
        return rows.filter { row ->
            (status == null || statusOf(row) == status) &&
                (q.isEmpty() ||
                    row.studentName.orEmpty().contains(q, ignoreCase = true) ||
                    row.studentNis.orEmpty().contains(q, ignoreCase = true))
        }
    }

    // ── 77.6 Admin override ─────────────────────────────────────────────

    /**
     * 77.6.3: a recorded presence is never turned into an absence. `alpha`
     * may become hadir/sakit/izin; sakit/izin/telat may only become hadir.
     */
    fun overrideTargets(current: SessionAttendanceStatus): List<SessionAttendanceStatus> = when (current) {
        SessionAttendanceStatus.ALPHA -> listOf(
            SessionAttendanceStatus.HADIR,
            SessionAttendanceStatus.SAKIT,
            SessionAttendanceStatus.IZIN
        )
        SessionAttendanceStatus.SAKIT,
        SessionAttendanceStatus.IZIN,
        SessionAttendanceStatus.TELAT -> listOf(SessionAttendanceStatus.HADIR)
        SessionAttendanceStatus.HADIR -> emptyList()
    }

    fun isOverrideReasonValid(reason: String): Boolean = reason.trim().length >= OVERRIDE_REASON_MIN_CHARS

    // ── Errors ──────────────────────────────────────────────────────────
    // Words for the screen (labels, messages) live in core/designsystem
    // ClassSessionText, in the app's language. This file only decides.

    /**
     * Which refusal the server reported, from its `error_code`
     * (sistem-terpadu docs/modules/class_sessions.md §4). The `message` is in
     * the user's language and is never parsed.
     */
    fun rejectionOf(errorCode: String?): ClassSessionRejection? = when (errorCode) {
        "outside_schedule_window" -> ClassSessionRejection.OUTSIDE_SCHEDULE_WINDOW
        "not_your_schedule" -> ClassSessionRejection.NOT_YOUR_SCHEDULE
        "session_already_started" -> ClassSessionRejection.SESSION_ALREADY_STARTED
        "session_already_ended" -> ClassSessionRejection.SESSION_ALREADY_ENDED
        "session_not_started" -> ClassSessionRejection.SESSION_NOT_STARTED
        "session_ended" -> ClassSessionRejection.SESSION_ENDED
        "qr_not_active" -> ClassSessionRejection.QR_NOT_ACTIVE
        "manual_not_active" -> ClassSessionRejection.MANUAL_NOT_ACTIVE
        "qr_invalid" -> ClassSessionRejection.QR_INVALID
        "qr_expired" -> ClassSessionRejection.QR_EXPIRED
        "not_enrolled" -> ClassSessionRejection.NOT_ENROLLED
        "already_checked_in" -> ClassSessionRejection.ALREADY_CHECKED_IN
        "transition_not_allowed" -> ClassSessionRejection.TRANSITION_NOT_ALLOWED
        else -> null
    }

    /** What the scanner does after a refused scan; the words come from ClassSessionText. */
    sealed interface ScanOutcome {
        /** Keep the camera running so the student can try again straight away. */
        val keepScanning: Boolean

        data class Retry(val error: ClassSessionError) : ScanOutcome { override val keepScanning = true }
        data class AlreadyRecorded(val checkedInAt: String?) : ScanOutcome { override val keepScanning = false }
        data class Blocked(val error: ClassSessionError) : ScanOutcome { override val keepScanning = false }
    }

    /** 77.5.2 error handling. */
    fun scanOutcome(error: ClassSessionError): ScanOutcome = when (error.rejection) {
        ClassSessionRejection.QR_EXPIRED,
        ClassSessionRejection.QR_INVALID,
        ClassSessionRejection.SESSION_NOT_STARTED -> ScanOutcome.Retry(error)
        ClassSessionRejection.ALREADY_CHECKED_IN -> ScanOutcome.AlreadyRecorded(error.checkedInAt)
        ClassSessionRejection.NOT_ENROLLED,
        ClassSessionRejection.SESSION_ENDED -> ScanOutcome.Blocked(error)
        else -> when (error.kind) {
            ClassSessionErrorKind.NETWORK, ClassSessionErrorKind.RATE_LIMITED -> ScanOutcome.Retry(error)
            else -> ScanOutcome.Blocked(error)
        }
    }

    /** Report row name for the chosen `group_by`. */
    fun reportLabel(row: AttendanceReportRowDto): String =
        listOfNotNull(
            row.studentName?.let { name -> row.studentNis?.let { "$name ($it)" } ?: name },
            row.classroomName.takeIf { row.studentName == null },
            row.subjectName.takeIf { row.studentName == null && row.classroomName == null }
        ).firstOrNull() ?: "—"
}
