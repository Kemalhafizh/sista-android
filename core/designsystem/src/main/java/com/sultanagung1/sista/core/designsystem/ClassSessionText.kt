package com.sultanagung1.sista.core.designsystem

import androidx.annotation.StringRes
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.data.model.ClassSessionError
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionRejection
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import com.sultanagung1.sista.data.model.SessionCheckInMethod
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The words of the class-session screens (teacher, student, admin), in the
 * app's language. [ClassSessionRules] decides; this says it. The server's own
 * message is shown as it is whenever it sent one: it already speaks the
 * user's language.
 */
object ClassSessionText {

    @StringRes
    fun label(status: SessionAttendanceStatus): Int = when (status) {
        SessionAttendanceStatus.HADIR -> R.string.cs_status_hadir
        SessionAttendanceStatus.TELAT -> R.string.cs_status_telat
        SessionAttendanceStatus.SAKIT -> R.string.cs_status_sakit
        SessionAttendanceStatus.IZIN -> R.string.cs_status_izin
        SessionAttendanceStatus.ALPHA -> R.string.cs_status_alpha
    }

    @StringRes
    fun label(status: ClassSessionStatus): Int = when (status) {
        ClassSessionStatus.SCHEDULED -> R.string.cs_session_scheduled
        ClassSessionStatus.ACTIVE -> R.string.cs_session_active
        ClassSessionStatus.COMPLETED -> R.string.cs_session_completed
        ClassSessionStatus.CANCELLED -> R.string.cs_session_cancelled
        ClassSessionStatus.AUTO_CLOSED -> R.string.cs_session_auto_closed
    }

    /** "Scan QR 08:32", "Manual 08:35", "Belum absen". */
    fun checkIn(row: SessionAttendanceDto): UiText {
        val at = ClassSessionRules.clockOf(row.checkedInAt)
        return when (row.checkInMethod) {
            SessionCheckInMethod.QR_SCAN -> at?.let { UiText.Res(R.string.cs_check_in_qr, it) } ?: UiText.Res(R.string.cs_check_in_qr_bare)
            SessionCheckInMethod.MANUAL_TEACHER -> at?.let { UiText.Res(R.string.cs_check_in_manual, it) } ?: UiText.Res(R.string.cs_check_in_manual_bare)
            SessionCheckInMethod.AUTO_ALPHA, null -> UiText.Res(R.string.cs_not_checked_in)
        }
    }

    /** "2026-10-08T07:10:00+07:00" → "8 Okt 2026, 07:10" / "8 Oct 2026, 07:10"; "2026-10-08" → "8 Okt 2026". */
    fun date(iso: String?, locale: Locale = displayLocale()): String? {
        val day = iso?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val date = day.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
        val time = if (iso.contains('T')) ClassSessionRules.clockOf(iso.substringAfter('T')) else null
        return if (time != null) "$date, $time" else date
    }

    /** "Dikoreksi oleh Bu Ani pada 8 Okt 2026, 07:10"; null when the row was not corrected. */
    fun overrideAudit(row: SessionAttendanceDto, locale: Locale = displayLocale()): UiText? {
        if (!row.isOverride) return null
        val by: Any = row.overrideBy?.takeIf { it.isNotBlank() } ?: UiText.Res(R.string.cs_admin)
        val at = date(row.overrideAt, locale)
        return if (at != null) UiText.Res(R.string.cs_override_by_at, by, at) else UiText.Res(R.string.cs_override_by, by)
    }

    val notDeployed: UiText = UiText.Res(R.string.cs_not_deployed)

    val notAClassQr: UiText = UiText.Res(R.string.cs_not_class_qr)

    /** Messages shared by every screen: the server's words when it gave some. */
    fun generic(error: ClassSessionError): UiText = when (error.kind) {
        ClassSessionErrorKind.NOT_DEPLOYED -> notDeployed
        ClassSessionErrorKind.NETWORK -> UiText.Res(R.string.cs_err_network)
        ClassSessionErrorKind.UNAUTHORIZED -> UiText.Res(R.string.cs_err_unauthorized)
        ClassSessionErrorKind.RATE_LIMITED -> UiText.Res(R.string.cs_err_rate_limited)
        ClassSessionErrorKind.FORBIDDEN -> serverOr(error, R.string.cs_err_forbidden)
        else -> serverOr(error, R.string.cs_err_generic)
    }

    /** 77.8 cases 2–3: why "Mulai Kelas" was refused (the server says it best, e.g. "Kelas baru bisa dimulai pukul 06:50."). */
    fun startFailure(error: ClassSessionError): UiText = when {
        error.message.isNotBlank() && error.rejection != null -> UiText.Raw(error.message)
        error.rejection == ClassSessionRejection.OUTSIDE_SCHEDULE_WINDOW ->
            UiText.Res(R.string.cs_start_window, ClassSessionRules.START_EARLY_MINUTES)
        error.rejection == ClassSessionRejection.NOT_YOUR_SCHEDULE -> UiText.Res(R.string.cs_start_not_yours)
        else -> generic(error)
    }

    /** 77.5.2: what the student reads after a refused scan. */
    fun scan(outcome: ClassSessionRules.ScanOutcome): UiText = when (outcome) {
        is ClassSessionRules.ScanOutcome.AlreadyRecorded ->
            ClassSessionRules.clockOf(outcome.checkedInAt)?.let { UiText.Res(R.string.cs_scan_already_at, it) }
                ?: UiText.Res(R.string.cs_scan_already)
        is ClassSessionRules.ScanOutcome.Retry -> scanProblem(outcome.error)
        is ClassSessionRules.ScanOutcome.Blocked -> scanProblem(outcome.error)
    }

    private fun scanProblem(error: ClassSessionError): UiText = when (error.rejection) {
        ClassSessionRejection.QR_EXPIRED -> UiText.Res(R.string.cs_scan_expired)
        ClassSessionRejection.QR_INVALID -> UiText.Res(R.string.cs_scan_invalid)
        ClassSessionRejection.NOT_ENROLLED -> UiText.Res(R.string.cs_scan_not_enrolled)
        ClassSessionRejection.SESSION_ENDED -> UiText.Res(R.string.cs_scan_ended)
        ClassSessionRejection.SESSION_NOT_STARTED -> UiText.Res(R.string.cs_scan_not_started)
        else -> generic(error)
    }

    /** 77.6.3: server-side refusals of an override, in words for the admin. */
    fun overrideFailure(error: ClassSessionError): UiText =
        if (error.rejection == ClassSessionRejection.TRANSITION_NOT_ALLOWED && error.message.isBlank()) {
            UiText.Res(R.string.cs_override_not_allowed)
        } else {
            generic(error)
        }

    private fun serverOr(error: ClassSessionError, @StringRes fallback: Int): UiText =
        error.message.takeIf { it.isNotBlank() }?.let { UiText.Raw(it) } ?: UiText.Res(fallback)
}
