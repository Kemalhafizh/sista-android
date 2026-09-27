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
        val opensAt = start - START_EARLY_MINUTES
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

    fun isClassSessionQr(raw: String?): Boolean =
        raw != null && raw.trim().startsWith(ClassSessionContract.QR_PAYLOAD_PREFIX)

    // ── Attendance counts ───────────────────────────────────────────────

    data class Counts(
        val hadir: Int = 0,
        val telat: Int = 0,
        val sakit: Int = 0,
        val izin: Int = 0,
        val alpha: Int = 0,
        val total: Int = 0
    ) {
        val present: Int get() = hadir + telat
        /** 0.0–1.0; 0 when the class has no students. */
        val presenceRate: Double get() = if (total <= 0) 0.0 else present.toDouble() / total
    }

    fun countsOf(rows: List<SessionAttendanceDto>): Counts {
        val byStatus = rows.groupingBy { it.effectiveStatus }.eachCount()
        return Counts(
            hadir = byStatus[SessionAttendanceStatus.HADIR] ?: 0,
            telat = byStatus[SessionAttendanceStatus.TELAT] ?: 0,
            sakit = byStatus[SessionAttendanceStatus.SAKIT] ?: 0,
            izin = byStatus[SessionAttendanceStatus.IZIN] ?: 0,
            alpha = byStatus[SessionAttendanceStatus.ALPHA] ?: 0,
            total = rows.size
        )
    }

    fun countsOf(session: ClassSessionDto): Counts = Counts(
        hadir = session.hadirCount,
        telat = session.telatCount,
        sakit = session.sakitCount,
        izin = session.izinCount,
        alpha = session.alphaCount,
        total = session.totalStudents
    )

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

    // ── Labels ──────────────────────────────────────────────────────────

    fun label(status: SessionAttendanceStatus): String = when (status) {
        SessionAttendanceStatus.HADIR -> "Hadir"
        SessionAttendanceStatus.TELAT -> "Telat"
        SessionAttendanceStatus.SAKIT -> "Sakit"
        SessionAttendanceStatus.IZIN -> "Izin"
        SessionAttendanceStatus.ALPHA -> "Alpha"
    }

    fun label(status: ClassSessionStatus): String = when (status) {
        ClassSessionStatus.SCHEDULED -> "Terjadwal"
        ClassSessionStatus.ACTIVE -> "Sedang Berlangsung"
        ClassSessionStatus.COMPLETED -> "Selesai"
        ClassSessionStatus.CANCELLED -> "Dibatalkan"
        ClassSessionStatus.AUTO_CLOSED -> "Ditutup Otomatis"
    }

    /** "Scan QR 08:32", "Manual 08:35", "Belum absen". */
    fun checkInLabel(row: SessionAttendanceDto): String {
        val at = clockOf(row.checkedInAt)
        return when (row.checkInMethod) {
            SessionCheckInMethod.QR_SCAN -> if (at != null) "Scan QR $at" else "Scan QR"
            SessionCheckInMethod.MANUAL_TEACHER -> if (at != null) "Manual $at" else "Manual guru"
            SessionCheckInMethod.ADMIN_OVERRIDE -> "Koreksi admin"
            SessionCheckInMethod.AUTO_ALPHA, null -> "Belum absen"
        }
    }

    // ── Error messages ──────────────────────────────────────────────────

    const val NOT_DEPLOYED_MESSAGE =
        "Fitur Sesi Kelas belum aktif di server sekolah. Hubungi admin/TU untuk mengaktifkannya."

    /** Messages shared by every screen; screen-specific codes are handled before this. */
    fun genericMessage(error: ClassSessionError): String = when (error.kind) {
        ClassSessionErrorKind.NOT_DEPLOYED -> NOT_DEPLOYED_MESSAGE
        ClassSessionErrorKind.NETWORK -> "Tidak ada koneksi ke server. Periksa internet lalu coba lagi."
        ClassSessionErrorKind.UNAUTHORIZED -> "Sesi login berakhir. Silakan masuk ulang."
        ClassSessionErrorKind.FORBIDDEN -> error.message.ifBlank { "Akun Anda tidak punya akses ke fitur ini." }
        else -> error.message.ifBlank { "Terjadi kesalahan. Coba lagi." }
    }

    /** 77.8 cases 2–3: why "Mulai Kelas" was refused. */
    fun startFailureMessage(error: ClassSessionError): String = when (error.errorCode) {
        ClassSessionContract.ErrorCode.OUTSIDE_SCHEDULE_WINDOW ->
            "Kelas hanya bisa dimulai mulai $START_EARLY_MINUTES menit sebelum jadwal hingga jam pelajaran berakhir."
        ClassSessionContract.ErrorCode.NOT_YOUR_SCHEDULE -> "Jadwal ini bukan milik Anda."
        else -> genericMessage(error)
    }

    sealed interface ScanOutcome {
        val message: String
        /** Keep the camera running so the student can try again straight away. */
        val keepScanning: Boolean

        data class Retry(override val message: String) : ScanOutcome { override val keepScanning = true }
        data class AlreadyRecorded(override val message: String, val checkedInAt: String?) : ScanOutcome {
            override val keepScanning = false
        }
        data class Blocked(override val message: String) : ScanOutcome { override val keepScanning = false }
    }

    /** 77.5.2 error handling. */
    fun scanOutcome(error: ClassSessionError): ScanOutcome {
        val code = error.errorCode ?: when (error.kind) {
            ClassSessionErrorKind.GONE -> ClassSessionContract.ErrorCode.QR_EXPIRED
            else -> null
        }
        return when (code) {
            ClassSessionContract.ErrorCode.QR_EXPIRED ->
                ScanOutcome.Retry("QR sudah berganti. Scan ulang QR yang sedang tampil di layar guru.")
            ClassSessionContract.ErrorCode.QR_INVALID ->
                ScanOutcome.Retry("QR tidak dikenali. Pastikan Anda memindai QR sesi kelas dari layar guru.")
            ClassSessionContract.ErrorCode.NOT_ENROLLED ->
                ScanOutcome.Blocked("Anda bukan siswa kelas ini. Hubungi guru pengampu.")
            ClassSessionContract.ErrorCode.ALREADY_CHECKED_IN -> {
                val at = clockOf(error.checkedInAt)
                ScanOutcome.AlreadyRecorded(
                    if (at != null) "Anda sudah tercatat hadir pukul $at WIB." else "Anda sudah tercatat hadir di sesi ini.",
                    error.checkedInAt
                )
            }
            ClassSessionContract.ErrorCode.SESSION_CLOSED, ClassSessionContract.ErrorCode.SESSION_NOT_ACTIVE ->
                ScanOutcome.Blocked("Sesi kelas sudah selesai. Minta koreksi ke Waka Kurikulum/TU.")
            else -> when (error.kind) {
                ClassSessionErrorKind.NETWORK -> ScanOutcome.Retry(genericMessage(error))
                else -> ScanOutcome.Blocked(genericMessage(error))
            }
        }
    }

    /** 77.6.3: server-side refusals of an override, in words for the admin. */
    fun overrideFailureMessage(error: ClassSessionError): String = when (error.errorCode) {
        ClassSessionContract.ErrorCode.TRANSITION_NOT_ALLOWED ->
            "Perubahan status ini tidak diizinkan. Kehadiran yang sudah tercatat tidak bisa diubah menjadi alpha."
        ClassSessionContract.ErrorCode.REASON_TOO_SHORT ->
            "Alasan koreksi minimal $OVERRIDE_REASON_MIN_CHARS karakter."
        else -> genericMessage(error)
    }
}
