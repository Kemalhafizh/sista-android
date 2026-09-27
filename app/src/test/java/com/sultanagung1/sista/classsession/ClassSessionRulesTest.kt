package com.sultanagung1.sista.classsession

import com.sultanagung1.sista.data.model.ClassSessionContract.ErrorCode
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionError
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness
import com.sultanagung1.sista.data.model.ClassSessionRules.ScanOutcome
import com.sultanagung1.sista.data.model.ClassSessionRules.StartAction
import com.sultanagung1.sista.data.model.ClassSessionRules.TimerTone
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import com.sultanagung1.sista.data.model.SessionAttendanceStatus.ALPHA
import com.sultanagung1.sista.data.model.SessionAttendanceStatus.HADIR
import com.sultanagung1.sista.data.model.SessionAttendanceStatus.IZIN
import com.sultanagung1.sista.data.model.SessionAttendanceStatus.SAKIT
import com.sultanagung1.sista.data.model.SessionAttendanceStatus.TELAT
import com.sultanagung1.sista.data.model.SessionCheckInMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** FASE 77.8.1: the rules the class-session screens are built on. */
class ClassSessionRulesTest {

    private fun slot(
        start: String = "08:30",
        end: String = "09:15",
        status: ClassSessionStatus? = null,
        jamKe: Int = 3
    ) = ClassSessionDto(scheduleId = 1, scheduledStart = start, scheduledEnd = end, status = status, jamKe = jamKe)

    private fun minutes(hhmm: String) = ClassSessionRules.minutesOf(hhmm)!!

    // ── time parsing ────────────────────────────────────────────────────

    @Test
    fun parsesClockStringsAndIsoTimestamps() {
        assertEquals(8 * 60 + 30, ClassSessionRules.minutesOf("08:30"))
        assertEquals(7 * 60 + 5, ClassSessionRules.minutesOf("7:05:00"))
        assertEquals(8 * 60 + 32, ClassSessionRules.minutesOf("2026-10-07T08:32:10+07:00"))
        assertNull(ClassSessionRules.minutesOf(null))
        assertNull(ClassSessionRules.minutesOf("bukan jam"))
        assertNull(ClassSessionRules.minutesOf("25:00"))
        assertEquals("08:32", ClassSessionRules.clockOf("2026-10-07T08:32:10+07:00"))
        assertEquals("08:30 – 09:15", ClassSessionRules.timeRange("08:30:00", "09:15:00"))
    }

    // ── 77.2 start window (cases 1–2) ───────────────────────────────────

    @Test
    fun startOpensTenMinutesBeforeAndClosesAtTheEnd() {
        val s = slot()
        assertEquals(StartAction.NotYet("08:20"), ClassSessionRules.startActionFor(s, minutes("08:19")))
        assertEquals(StartAction.Available, ClassSessionRules.startActionFor(s, minutes("08:20")))
        assertEquals(StartAction.Available, ClassSessionRules.startActionFor(s, minutes("09:14")))
        assertEquals(StartAction.Missed, ClassSessionRules.startActionFor(s, minutes("09:15")))
    }

    @Test
    fun startIsHiddenOnceTheSessionExists() {
        for (status in listOf(ClassSessionStatus.ACTIVE, ClassSessionStatus.COMPLETED, ClassSessionStatus.AUTO_CLOSED, ClassSessionStatus.CANCELLED)) {
            assertEquals(status.name, StartAction.Hidden, ClassSessionRules.startActionFor(slot(status = status), minutes("08:40")))
        }
        // A missing status means "not started yet".
        assertEquals(StartAction.Available, ClassSessionRules.startActionFor(slot(status = null), minutes("08:40")))
    }

    @Test
    fun unparsableTimesLeaveTheDecisionToTheServer() {
        assertEquals(StartAction.Available, ClassSessionRules.startActionFor(slot(start = "?", end = "?"), 0))
    }

    @Test
    fun activeFirstThenUpcomingThenFinished() {
        val done = slot(start = "07:00", status = ClassSessionStatus.COMPLETED, jamKe = 1)
        val later = slot(start = "10:00", jamKe = 5)
        val active = slot(start = "08:30", status = ClassSessionStatus.ACTIVE, jamKe = 3)
        val sooner = slot(start = "09:15", jamKe = 4)
        val auto = slot(start = "07:45", status = ClassSessionStatus.AUTO_CLOSED, jamKe = 2)
        assertEquals(
            listOf(active, sooner, later, done, auto),
            ClassSessionRules.sortForTeacher(listOf(done, later, active, sooner, auto))
        )
    }

    // ── 77.3 timer ──────────────────────────────────────────────────────

    @Test
    fun timerTurnsAmberAtFiveMinutesAndRedAtOne() {
        assertEquals(TimerTone.NORMAL, ClassSessionRules.timerTone(301))
        assertEquals(TimerTone.WARNING, ClassSessionRules.timerTone(300))
        assertEquals(TimerTone.WARNING, ClassSessionRules.timerTone(61))
        assertEquals(TimerTone.CRITICAL, ClassSessionRules.timerTone(60))
        assertEquals(TimerTone.CRITICAL, ClassSessionRules.timerTone(1))
        assertEquals(TimerTone.OVERTIME, ClassSessionRules.timerTone(0))
        assertEquals(TimerTone.OVERTIME, ClassSessionRules.timerTone(-30))
    }

    @Test
    fun remainingSecondsAndCountdownText() {
        val now = (8 * 60 + 42) * 60L + 45 // 08:42:45
        assertEquals(32 * 60L + 15, ClassSessionRules.remainingSeconds("09:15", now))
        assertEquals("32:15", ClassSessionRules.formatCountdown(32 * 60L + 15))
        assertEquals("1:05:00", ClassSessionRules.formatCountdown(3900))
        assertEquals("-02:10", ClassSessionRules.formatCountdown(-130))
        assertNull(ClassSessionRules.remainingSeconds(null, now))
    }

    // ── 77.3.2 QR (case 4) ──────────────────────────────────────────────

    @Test
    fun qrStaysOnScreenBrieflyAfterExpiryThenAdmitsItIsGone() {
        assertEquals(QrFreshness.LOADING, ClassSessionRules.qrFreshness(null, 1_000))
        assertEquals(QrFreshness.FRESH, ClassSessionRules.qrFreshness(10_000, 9_999))
        assertEquals(QrFreshness.STALE, ClassSessionRules.qrFreshness(10_000, 10_000))
        assertEquals(QrFreshness.STALE, ClassSessionRules.qrFreshness(10_000, 69_999))
        assertEquals(QrFreshness.UNAVAILABLE, ClassSessionRules.qrFreshness(10_000, 70_000))
    }

    @Test
    fun qrRetryBacksOffFromFiveSecondsToThirty() {
        assertEquals(listOf(5_000L, 5_000L, 10_000L, 20_000L, 30_000L, 30_000L),
            (0..5).map { ClassSessionRules.qrRetryDelayMs(it) })
        assertEquals(30_000L, ClassSessionRules.qrRetryDelayMs(1_000))
        assertEquals(28_000L, ClassSessionRules.nextQrFetchDelayMs(28))
        assertEquals(1_000L, ClassSessionRules.nextQrFetchDelayMs(0))
        assertEquals(1_000L, ClassSessionRules.nextQrFetchDelayMs(-5))
    }

    @Test
    fun onlyClassSessionQrCodesAreSent() {
        assertTrue(ClassSessionRules.isClassSessionQr("sista-cs:v1:abc:def"))
        assertTrue(ClassSessionRules.isClassSessionQr("  sista-cs:v1:abc  "))
        assertFalse(ClassSessionRules.isClassSessionQr("https://example.com"))
        assertFalse(ClassSessionRules.isClassSessionQr("SA1-1234"))
        assertFalse(ClassSessionRules.isClassSessionQr(null))
    }

    @Test
    fun scanGateSendsEachPayloadOnceAndNeverResendsARefusedOne() {
        val gate = ClassSessionRules.ScanGate(cooldownMs = 3_000)
        assertTrue(gate.shouldSubmit("sista-cs:v1:a:1", 10_000))
        // The camera sees the same QR many times a second.
        assertFalse(gate.shouldSubmit("sista-cs:v1:a:1", 10_200))
        assertFalse(gate.shouldSubmit(" sista-cs:v1:a:1 ", 12_999))
        assertTrue(gate.shouldSubmit("sista-cs:v1:a:1", 13_000))
        // The next rotation is a different payload: sent straight away.
        assertTrue(gate.shouldSubmit("sista-cs:v1:a:2", 13_100))
        // Once refused (e.g. expired), never again, however long we wait.
        gate.markRefused("sista-cs:v1:a:2")
        assertFalse(gate.shouldSubmit("sista-cs:v1:a:2", 60_000))
        assertTrue(gate.shouldSubmit("sista-cs:v1:a:3", 60_000))
    }

    @Test
    fun scanGateForgetsOldRefusals() {
        val gate = ClassSessionRules.ScanGate(cooldownMs = 0)
        (1..25).forEach { gate.markRefused("p$it") }
        assertTrue("oldest refusal dropped", gate.shouldSubmit("p1", 1))
        assertFalse("recent refusal kept", gate.shouldSubmit("p25", 1))
    }

    // ── counts ──────────────────────────────────────────────────────────

    private fun row(id: Long, status: SessionAttendanceStatus?, name: String = "Siswa $id", nis: String = "1000$id") =
        SessionAttendanceDto(id = id, studentId = id, studentName = name, studentNis = nis, status = status)

    @Test
    fun countsTreatLateAsPresentAndMissingStatusAsAlpha() {
        val rows = listOf(row(1, HADIR), row(2, HADIR), row(3, TELAT), row(4, SAKIT), row(5, IZIN), row(6, ALPHA), row(7, null))
        val c = ClassSessionRules.countsOf(rows)
        assertEquals(ClassSessionRules.Counts(hadir = 2, telat = 1, sakit = 1, izin = 1, alpha = 2, total = 7), c)
        assertEquals(3, c.present)
        assertEquals(43, ClassSessionRules.presencePercent(c.presenceRate))
        assertEquals(0.0, ClassSessionRules.Counts().presenceRate, 0.0)
    }

    // ── 77.4 manual attendance (cases 9–10) ─────────────────────────────

    @Test
    fun onlyRealChangesAreSaved() {
        val original = mapOf(1L to ALPHA, 2L to ALPHA, 3L to HADIR)
        val edits = mapOf(1L to HADIR, 2L to ALPHA, 3L to HADIR, 4L to SAKIT)
        // Student 2 and 3 were set back to what the server has; 4 is new.
        assertEquals(mapOf(1L to HADIR, 4L to SAKIT), ClassSessionRules.pendingChanges(original, edits))
    }

    @Test
    fun searchByNameOrNisAndFilterByStatus() {
        val rows = listOf(row(1, HADIR, "Ahmad Fauzi", "12345"), row(2, ALPHA, "Budi Santoso", "12346"), row(3, HADIR, "Citra Dewi", "12347"))
        assertEquals(listOf(2L), ClassSessionRules.filterStudents(rows, "budi", null).map { it.id })
        assertEquals(listOf(3L), ClassSessionRules.filterStudents(rows, "12347", null).map { it.id })
        assertEquals(listOf(1L, 3L), ClassSessionRules.filterStudents(rows, "", HADIR).map { it.id })
        // The filter follows unsaved edits, not the server copy.
        val edited = mapOf(2L to HADIR)
        assertEquals(listOf(1L, 2L, 3L),
            ClassSessionRules.filterStudents(rows, "", HADIR) { edited[it.studentId] ?: it.effectiveStatus }.map { it.id })
    }

    @Test
    fun teacherCannotPickLateByHand() {
        assertEquals(listOf(HADIR, SAKIT, IZIN, ALPHA), ClassSessionRules.TEACHER_MARK_OPTIONS)
    }

    @Test
    fun checkInLabels() {
        assertEquals("Scan QR 08:32", ClassSessionRules.checkInLabel(SessionAttendanceDto(checkInMethod = SessionCheckInMethod.QR_SCAN, checkedInAt = "2026-10-07T08:32:00+07:00")))
        assertEquals("Manual 08:35", ClassSessionRules.checkInLabel(SessionAttendanceDto(checkInMethod = SessionCheckInMethod.MANUAL_TEACHER, checkedInAt = "08:35")))
        assertEquals("Belum absen", ClassSessionRules.checkInLabel(SessionAttendanceDto(checkInMethod = SessionCheckInMethod.AUTO_ALPHA)))
        assertEquals("Belum absen", ClassSessionRules.checkInLabel(SessionAttendanceDto()))
    }

    // ── 77.6 override (cases 13–15) ─────────────────────────────────────

    @Test
    fun presenceIsNeverOverriddenIntoAbsence() {
        assertEquals(listOf(HADIR, SAKIT, IZIN), ClassSessionRules.overrideTargets(ALPHA))
        assertEquals(listOf(HADIR), ClassSessionRules.overrideTargets(SAKIT))
        assertEquals(listOf(HADIR), ClassSessionRules.overrideTargets(IZIN))
        assertEquals(listOf(HADIR), ClassSessionRules.overrideTargets(TELAT))
        assertTrue(ClassSessionRules.overrideTargets(HADIR).isEmpty())
        for (status in SessionAttendanceStatus.values()) {
            assertFalse("$status → alpha", ALPHA in ClassSessionRules.overrideTargets(status))
        }
    }

    @Test
    fun overrideReasonNeedsTenRealCharacters() {
        assertFalse(ClassSessionRules.isOverrideReasonValid(""))
        assertFalse(ClassSessionRules.isOverrideReasonValid("   terlambat   ".padEnd(20)))
        assertFalse(ClassSessionRules.isOverrideReasonValid("123456789"))
        assertTrue(ClassSessionRules.isOverrideReasonValid("1234567890"))
        assertTrue(ClassSessionRules.isOverrideReasonValid("Terlambat karena upacara pramuka."))
    }

    @Test
    fun auditTrailIsReadable() {
        assertEquals("8 Okt 2026, 07:10", ClassSessionRules.formatDateId("2026-10-08T07:10:00+07:00"))
        assertEquals("7 Okt 2026", ClassSessionRules.formatDateId("2026-10-07"))
        assertNull(ClassSessionRules.formatDateId("kemarin"))
        assertNull(ClassSessionRules.formatDateId("2026-13-01"))
        assertEquals(
            "Dikoreksi oleh Bu Ani pada 8 Okt 2026, 07:10",
            ClassSessionRules.overrideAuditLabel(SessionAttendanceDto(isOverride = true, overrideByName = "Bu Ani", overrideAt = "2026-10-08T07:10:00+07:00"))
        )
        assertEquals("Dikoreksi oleh admin", ClassSessionRules.overrideAuditLabel(SessionAttendanceDto(isOverride = true)))
        assertNull(ClassSessionRules.overrideAuditLabel(SessionAttendanceDto(isOverride = false, overrideByName = "x")))
    }

    // ── error wording (cases 2, 3, 6–8) ─────────────────────────────────

    private fun err(kind: ClassSessionErrorKind, code: String? = null, checkedInAt: String? = null, message: String = "") =
        ClassSessionError(kind = kind, message = message, errorCode = code, checkedInAt = checkedInAt)

    @Test
    fun scanErrorsAreExplainedAndSayWhetherToKeepScanning() {
        val expired = ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.GONE, ErrorCode.QR_EXPIRED))
        assertTrue(expired is ScanOutcome.Retry && expired.keepScanning)
        assertTrue(expired.message.contains("QR sudah berganti"))

        // A bare 410 without an error code is still an expired QR.
        assertTrue(ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.GONE)) is ScanOutcome.Retry)

        val notMine = ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.FORBIDDEN, ErrorCode.NOT_ENROLLED))
        assertTrue(notMine is ScanOutcome.Blocked && !notMine.keepScanning)
        assertTrue(notMine.message.contains("bukan siswa kelas ini"))

        val twice = ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.CONFLICT, ErrorCode.ALREADY_CHECKED_IN, "2026-10-07T08:03:00+07:00"))
        assertTrue(twice is ScanOutcome.AlreadyRecorded)
        assertEquals("Anda sudah tercatat hadir pukul 08:03 WIB.", twice.message)

        val closed = ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.CONFLICT, ErrorCode.SESSION_CLOSED))
        assertTrue(closed is ScanOutcome.Blocked && closed.message.contains("Waka Kurikulum/TU"))

        val offline = ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.NETWORK))
        assertTrue(offline is ScanOutcome.Retry)
    }

    @Test
    fun startRefusalsAreExplained() {
        assertTrue(ClassSessionRules.startFailureMessage(err(ClassSessionErrorKind.VALIDATION, ErrorCode.OUTSIDE_SCHEDULE_WINDOW)).contains("10 menit sebelum"))
        assertEquals("Jadwal ini bukan milik Anda.", ClassSessionRules.startFailureMessage(err(ClassSessionErrorKind.FORBIDDEN, ErrorCode.NOT_YOUR_SCHEDULE)))
    }

    @Test
    fun missingBackendIsSaidPlainly() {
        assertEquals(ClassSessionRules.NOT_DEPLOYED_MESSAGE, ClassSessionRules.genericMessage(err(ClassSessionErrorKind.NOT_DEPLOYED, message = "The route api/v1/teacher/class-sessions/today could not be found.")))
        assertEquals(ClassSessionRules.NOT_DEPLOYED_MESSAGE, ClassSessionRules.scanOutcome(err(ClassSessionErrorKind.NOT_DEPLOYED)).message)
        // The server's own words win when it gave some.
        assertEquals("Validasi gagal.", ClassSessionRules.genericMessage(err(ClassSessionErrorKind.VALIDATION, message = "Validasi gagal.")))
    }
}
