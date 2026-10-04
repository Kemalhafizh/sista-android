package com.sultanagung1.sista.widget

import com.sultanagung1.sista.core.widget.AttendanceSnapshot
import com.sultanagung1.sista.core.widget.BillingSnapshot
import com.sultanagung1.sista.core.widget.ScheduleSnapshot
import com.sultanagung1.sista.core.widget.WidgetLesson
import com.sultanagung1.sista.core.widget.WidgetSnapshot
import com.sultanagung1.sista.widget.WidgetTestStrings.render
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/** Snapshot → widget text, including every "we don't know" case. */
class WidgetTextMapperTest {

    private val zone = ZoneId.of("Asia/Jakarta")

    /** Sunday 27 Sep 2026, 07:30 WIB. */
    private val sundayMorning = ZonedDateTime.of(2026, 9, 27, 7, 30, 0, 0, zone)

    /** Monday 28 Sep 2026, 07:30 WIB. */
    private val mondayMorning = ZonedDateTime.of(2026, 9, 28, 7, 30, 0, 0, zone)

    private fun millis(day: Int, hour: Int, minute: Int) =
        ZonedDateTime.of(2026, 9, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    private val student = WidgetSnapshot(ownerUserId = "12", role = "student")
    private val parent = WidgetSnapshot(ownerUserId = "40", role = "parent")

    // ---------------------------------------------------------------- attendance

    @Test
    fun attendanceWithoutAnySnapshotSaysNoData() {
        val content = WidgetTextMapper.attendance(null, sundayMorning)
        assertEquals("Belum ada data", render(content.status))
        assertEquals("Buka Sulaone untuk memuat data", render(content.detail))
        assertNull(content.updated)
    }

    @Test
    fun attendanceNotLoadedYetSaysNoData() {
        val content = WidgetTextMapper.attendance(student, sundayMorning)
        assertEquals("Belum ada data", render(content.status))
        assertEquals("Buka Sulaone untuk memuat data", render(content.detail))
    }

    @Test
    fun attendanceForStaffIsNotApplicable() {
        for (role in listOf("teacher", "guru", "admin", "kepala_sekolah")) {
            val content = WidgetTextMapper.attendance(WidgetSnapshot(role = role), sundayMorning)
            assertEquals(role, "Belum ada data", render(content.status))
            assertEquals(role, "Tersedia untuk akun siswa & wali murid", render(content.detail))
        }
    }

    @Test
    fun attendanceTodayWithCheckInTime() {
        val snapshot = student.copy(
            attendance = AttendanceSnapshot(
                date = "2026-09-27", statusCode = "H", statusLabel = null,
                checkInTime = "06:45", fetchedAt = millis(27, 7, 10)
            )
        )
        val content = WidgetTextMapper.attendance(snapshot, sundayMorning)
        assertEquals("Hadir hari ini (06:45)", render(content.status))
        assertNull(content.detail)
        assertEquals("Diperbarui 07:10", render(content.updated))
    }

    @Test
    fun attendanceTodayWithoutCheckInTime() {
        val snapshot = student.copy(
            attendance = AttendanceSnapshot(
                date = "2026-09-27", statusCode = "s", statusLabel = null, fetchedAt = millis(27, 7, 0)
            )
        )
        assertEquals("Sakit hari ini", render(WidgetTextMapper.attendance(snapshot, sundayMorning).status))
    }

    @Test
    fun olderRecordIsNeverShownAsToday() {
        val snapshot = student.copy(
            attendance = AttendanceSnapshot(
                date = "2026-09-26", statusCode = "H", statusLabel = null,
                checkInTime = "06:40", fetchedAt = millis(26, 18, 0)
            )
        )
        val content = WidgetTextMapper.attendance(snapshot, sundayMorning)
        assertEquals("Belum tercatat hari ini", render(content.status))
        assertEquals("Terakhir: Hadir (26/09)", render(content.detail))
        assertEquals("Diperbarui 26/09 18:00", render(content.updated))
    }

    @Test
    fun parentSeesTheSelectedChildByName() {
        val today = parent.copy(
            attendance = AttendanceSnapshot(
                childUuid = "c-1", childName = "Budi", date = "2026-09-27",
                statusCode = "I", statusLabel = "Izin", fetchedAt = millis(27, 7, 20)
            )
        )
        val content = WidgetTextMapper.attendance(today, sundayMorning)
        assertEquals("Izin hari ini", render(content.status))
        assertEquals("Budi", render(content.detail))

        val older = today.copy(attendance = today.attendance!!.copy(date = "2026-09-25", statusCode = "A"))
        val olderContent = WidgetTextMapper.attendance(older, sundayMorning)
        assertEquals("Belum tercatat hari ini", render(olderContent.status))
        assertEquals("Budi • terakhir Alpa (25/09)", render(olderContent.detail))
    }

    @Test
    fun gpsCheckInStatusWordIsUnderstood() {
        // GeofenceAttendanceService stores status = 'hadir', not 'H'.
        val snapshot = student.copy(
            attendance = AttendanceSnapshot(date = "2026-09-27", statusCode = "hadir", statusLabel = null, fetchedAt = millis(27, 7, 0))
        )
        assertEquals("Hadir hari ini", render(WidgetTextMapper.attendance(snapshot, sundayMorning).status))
    }

    @Test
    fun emptyHistorySaysNothingRecorded() {
        val snapshot = student.copy(
            attendance = AttendanceSnapshot(date = null, statusCode = null, statusLabel = null, fetchedAt = millis(27, 7, 0))
        )
        val content = WidgetTextMapper.attendance(snapshot, sundayMorning)
        assertEquals("Belum ada presensi tercatat", render(content.status))
        assertNull(content.detail)
    }

    @Test
    fun unknownStatusCodeFallsBackToServerLabelThenDash() {
        val withLabel = parent.copy(
            attendance = AttendanceSnapshot(
                childUuid = "c-1", date = "2026-09-27", statusCode = "D",
                statusLabel = "Dispensasi", fetchedAt = millis(27, 7, 0)
            )
        )
        assertEquals("Dispensasi hari ini", render(WidgetTextMapper.attendance(withLabel, sundayMorning).status))

        val noLabel = student.copy(
            attendance = AttendanceSnapshot(date = "2026-09-27", statusCode = null, statusLabel = null, fetchedAt = millis(27, 7, 0))
        )
        assertEquals("– hari ini", render(WidgetTextMapper.attendance(noLabel, sundayMorning).status))
    }

    // ---------------------------------------------------------------- SPP

    @Test
    fun sppWithoutDataShowsDashNotPaid() {
        for (snapshot in listOf(null, student, parent)) {
            val content = WidgetTextMapper.spp(snapshot, sundayMorning)
            assertEquals("–", render(content.status))
            assertEquals("Buka Sulaone untuk memuat data", render(content.detail))
            assertNull(content.updated)
        }
    }

    @Test
    fun sppStudentWithOpenBills() {
        val snapshot = student.copy(
            billing = BillingSnapshot(unpaidCount = 3, unpaidTotal = 2_550_000.0, invoiceCount = 8, fetchedAt = millis(27, 7, 5))
        )
        val content = WidgetTextMapper.spp(snapshot, sundayMorning)
        assertEquals("Belum dibayar: 3 tagihan", render(content.status))
        assertEquals("Sisa Rp 2.550.000", render(content.detail))
        assertEquals("Diperbarui 07:05", render(content.updated))
    }

    @Test
    fun sppStudentWithNothingOpen() {
        val snapshot = student.copy(
            billing = BillingSnapshot(unpaidCount = 0, unpaidTotal = 0.0, invoiceCount = 8, fetchedAt = millis(27, 7, 5))
        )
        assertEquals("Tidak ada tunggakan", render(WidgetTextMapper.spp(snapshot, sundayMorning).status))
    }

    @Test
    fun sppStudentWithoutAnyInvoice() {
        val snapshot = student.copy(
            billing = BillingSnapshot(unpaidCount = 0, unpaidTotal = 0.0, invoiceCount = 0, fetchedAt = millis(27, 7, 5))
        )
        assertEquals("Belum ada tagihan tercatat", render(WidgetTextMapper.spp(snapshot, sundayMorning).status))
    }

    @Test
    fun sppParentShowsChildAndNoInventedAmount() {
        val snapshot = parent.copy(
            billing = BillingSnapshot(
                childUuid = "c-1", childName = "Budi", unpaidCount = 2,
                unpaidTotal = null, invoiceCount = null, fetchedAt = millis(27, 7, 5)
            )
        )
        val content = WidgetTextMapper.spp(snapshot, sundayMorning)
        assertEquals("Belum dibayar: 2 tagihan", render(content.status))
        assertEquals("Budi", render(content.detail))

        val clear = snapshot.copy(billing = snapshot.billing!!.copy(unpaidCount = 0))
        assertEquals("Tidak ada tunggakan", render(WidgetTextMapper.spp(clear, sundayMorning).status))
    }

    @Test
    fun sppForStaffIsNotApplicable() {
        val content = WidgetTextMapper.spp(WidgetSnapshot(role = "teacher"), sundayMorning)
        assertEquals("–", render(content.status))
        assertEquals("Tersedia untuk akun siswa & wali murid", render(content.detail))
    }

    @Test
    fun sppNeverSaysLunas() {
        val cases = listOf(
            null,
            student,
            student.copy(billing = BillingSnapshot(unpaidCount = 0, unpaidTotal = 0.0, invoiceCount = 4, fetchedAt = 0)),
            student.copy(billing = BillingSnapshot(unpaidCount = 0, unpaidTotal = 0.0, invoiceCount = 0, fetchedAt = 0)),
            parent.copy(billing = BillingSnapshot(childUuid = "c", unpaidCount = 0, unpaidTotal = null, invoiceCount = null, fetchedAt = 0))
        )
        for (snapshot in cases) {
            val content = WidgetTextMapper.spp(snapshot, sundayMorning)
            val shown = listOfNotNull(render(content.status), render(content.detail), render(content.updated))
            assertFalse(shown.toString(), shown.any { it.contains("LUNAS", ignoreCase = true) })
        }
    }

    @Test
    fun rupiahUsesIndonesianGrouping() {
        assertEquals("Rp 850.000", WidgetTextMapper.rupiah(850_000.0))
        assertEquals("Rp 0", WidgetTextMapper.rupiah(0.0))
        assertEquals("Rp 1.250.001", WidgetTextMapper.rupiah(1_250_000.6))
    }

    // ---------------------------------------------------------------- schedule

    private fun lesson(day: String, start: String, subject: String?, room: String? = "XII MIPA 1") =
        WidgetLesson(day = day, start = start, end = "", subject = subject, room = room)

    @Test
    fun scheduleWithoutDataSaysNoData() {
        for (snapshot in listOf(null, student)) {
            val content = WidgetTextMapper.schedule(snapshot, mondayMorning)
            assertEquals("–", render(content.count))
            assertEquals(listOf("Belum ada data", "Buka Sulaone untuk memuat data"), content.lines.map(::render))
        }
    }

    @Test
    fun scheduleForNonStudentIsNotApplicable() {
        for (role in listOf("parent", "orang_tua", "teacher", "admin")) {
            val content = WidgetTextMapper.schedule(WidgetSnapshot(role = role), mondayMorning)
            assertEquals(role, listOf("Tersedia untuk akun siswa"), content.lines.map(::render))
        }
    }

    @Test
    fun scheduleShowsOnlyTodaySortedByStart() {
        val snapshot = student.copy(
            schedule = ScheduleSnapshot(
                lessons = listOf(
                    lesson("Senin", "08:30:00", "Fisika"),
                    lesson("Selasa", "07:00:00", "Biologi"),
                    lesson("senin", "07:00:00", "Matematika"),
                    lesson("Senin", "10:15:00", "PAI", room = " ")
                ),
                fetchedAt = millis(27, 20, 0)
            )
        )
        val content = WidgetTextMapper.schedule(snapshot, mondayMorning)
        assertEquals("3 sesi", render(content.count))
        assertEquals(
            listOf("07:00  Matematika (XII MIPA 1)", "08:30  Fisika (XII MIPA 1)", "10:15  PAI"),
            content.lines.map(::render)
        )
    }

    @Test
    fun scheduleWithMoreLessonsThanSlotsSummarisesTheRest() {
        val lessons = (7..11).map { lesson("Senin", "%02d:00".format(it), "Mapel $it") }
        val content = WidgetTextMapper.schedule(student.copy(schedule = ScheduleSnapshot(lessons, 0)), mondayMorning)
        assertEquals("5 sesi", render(content.count))
        assertEquals(
            listOf("07:00  Mapel 7 (XII MIPA 1)", "08:00  Mapel 8 (XII MIPA 1)", "+3 pelajaran lainnya"),
            content.lines.map(::render)
        )
    }

    @Test
    fun scheduleShowsADashForAMissingSubject() {
        val snapshot = student.copy(
            schedule = ScheduleSnapshot(
                listOf(lesson("Senin", "07:00:00", null), lesson("Senin", "08:30:00", " ", room = null)),
                0
            )
        )
        val content = WidgetTextMapper.schedule(snapshot, mondayMorning)
        assertEquals(listOf("07:00  – (XII MIPA 1)", "08:30  –"), content.lines.map(::render))
    }

    @Test
    fun scheduleOnADayWithoutLessons() {
        val snapshot = student.copy(schedule = ScheduleSnapshot(listOf(lesson("Senin", "07:00", "Matematika")), 0))
        val content = WidgetTextMapper.schedule(snapshot, sundayMorning)
        assertEquals("0 sesi", render(content.count))
        assertEquals(listOf("Tidak ada pelajaran hari ini"), content.lines.map(::render))
    }
}
