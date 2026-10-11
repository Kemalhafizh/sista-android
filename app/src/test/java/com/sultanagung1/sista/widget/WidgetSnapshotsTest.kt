package com.sultanagung1.sista.widget

import com.sultanagung1.sista.core.widget.BillingSnapshot
import com.sultanagung1.sista.core.widget.LessonAttendanceSnapshot
import com.sultanagung1.sista.core.widget.WidgetLessonAttendance
import com.sultanagung1.sista.core.widget.WidgetSnapshot
import com.sultanagung1.sista.core.widget.WidgetSnapshots
import com.sultanagung1.sista.core.widget.withBilling
import com.sultanagung1.sista.core.widget.withLessonAttendance
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.ChildSummaryStatistics
import com.sultanagung1.sista.data.model.ChildSummaryStudent
import com.sultanagung1.sista.data.model.LessonAttendanceDay
import com.sultanagung1.sista.data.model.LessonAttendanceItem
import com.sultanagung1.sista.data.model.ScheduleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** API response → snapshot section, and how parent sections follow one child. */
class WidgetSnapshotsTest {

    private fun invoice(status: String, amount: Double, remaining: Double) = BillingInvoice(
        id = 1, category = "SPP", month = "September 2026", amount = amount,
        amountFormatted = "", dueDate = "2026-09-10", status = status, remainingBalance = remaining
    )

    @Test
    fun studentBillingCountsEverythingNotPaidAsOpen() {
        val billing = WidgetSnapshots.studentBilling(
            listOf(
                invoice("paid", 850_000.0, 0.0),
                invoice("PAID", 850_000.0, 0.0),
                invoice("unpaid", 850_000.0, 850_000.0),
                invoice("partial", 850_000.0, 350_000.0)
            ),
            fetchedAt = 5
        )
        assertEquals(2, billing.unpaidCount)
        assertEquals(1_200_000.0, billing.unpaidTotal!!, 0.0)
        assertEquals(4, billing.invoiceCount)
        assertEquals(5L, billing.fetchedAt)
    }

    @Test
    fun childBillingLeavesAmountUnknown() {
        val summary = ChildSummaryResponse(
            student = ChildSummaryStudent(uuid = "c-1", name = "Budi"),
            statistics = ChildSummaryStatistics(averageGrade = 80.0, attendanceRate = 95.0, unpaidBillingsCount = 2, totalBkPoints = 0)
        )
        val billing = WidgetSnapshots.childBilling("c-1", summary, fetchedAt = 1)
        assertEquals(2, billing.unpaidCount)
        assertNull(billing.unpaidTotal)
        assertNull(billing.invoiceCount)
        assertEquals("Budi", billing.childName)
    }

    private fun lesson(
        id: Long,
        subject: String?,
        start: String,
        code: String?,
        label: String? = null,
        checkedInAt: String? = null,
        awaiting: Boolean = false,
    ) = LessonAttendanceItem(
        sessionId = id, subjectName = subject, scheduledStart = start, scheduledEnd = null,
        statusCode = code, status = label, checkedInAt = checkedInAt, awaitingScan = awaiting,
    )

    @Test
    fun lessonAttendanceKeepsWhatTheServerSentPerLesson() {
        val day = LessonAttendanceDay(
            date = "2026-10-05",
            sessions = listOf(
                lesson(1, "Matematika", "07:00", "hadir", "Hadir", checkedInAt = "2026-10-05T07:05:00+07:00"),
                lesson(2, "Fisika", "08:30", "alpha", "Alpha", awaiting = true),
                lesson(3, "N/A", "10:00", null),
            ),
        )
        val snapshot = WidgetSnapshots.lessonAttendance(null, day, fetchedAt = 9)
        assertEquals("2026-10-05", snapshot.date)
        assertNull(snapshot.childUuid)
        assertEquals(9L, snapshot.fetchedAt)
        val lessons = snapshot.lessons!!
        assertEquals(listOf("Matematika", "Fisika", null), lessons.map { it.subject })
        assertEquals(listOf("hadir", "alpha", null), lessons.map { it.statusCode })
        assertEquals(listOf(false, true, false), lessons.map { it.awaitingScan })
        assertEquals("07:05", lessons[0].checkInTime)
        assertNull(lessons[1].checkInTime)
    }

    @Test
    fun scanTimeIsShownInSchoolTimeWhateverOffsetTheServerUses() {
        assertEquals("07:05", WidgetSnapshots.schoolClock("2026-10-05T00:05:00+00:00"))
        assertEquals("07:05", WidgetSnapshots.schoolClock("2026-10-05T07:05:00+07:00"))
        assertNull(WidgetSnapshots.schoolClock("07:05"))
        assertNull(WidgetSnapshots.schoolClock(null))
    }

    @Test
    fun aDayWithoutLessonsIsAnEmptyListNotUnknown() {
        val snapshot = WidgetSnapshots.lessonAttendance("c-1", LessonAttendanceDay(date = "2026-10-05", sessions = emptyList()), 1)
        assertEquals("c-1", snapshot.childUuid)
        assertEquals(emptyList<Any>(), snapshot.lessons)
    }

    @Test
    fun clockReadsIsoTimestampsAndPlainTimes() {
        assertEquals("06:45", WidgetSnapshots.clock("2026-09-27T06:45:12+07:00"))
        assertEquals("07:05", WidgetSnapshots.clock("7:05"))
        assertNull(WidgetSnapshots.clock(""))
        assertNull(WidgetSnapshots.clock(null))
    }

    @Test
    fun scheduleKeepsTheWholeWeek() {
        val schedule = WidgetSnapshots.schedule(
            listOf(
                ScheduleItem(1, "Senin", "07:00:00", "08:30:00", "Matematika", "Bu Sari", "XII MIPA 1"),
                ScheduleItem(2, "Selasa", "07:00:00", "08:30:00", "Fisika", "Pak Arif", "XII MIPA 1")
            ),
            fetchedAt = 3
        )
        assertEquals(listOf("Senin", "Selasa"), schedule.lessons.map { it.day })
        assertEquals("Matematika", schedule.lessons.first().subject)
    }

    @Test
    fun scheduleKeepsAMissingSubjectAndClassUnknown() {
        val schedule = WidgetSnapshots.schedule(
            listOf(
                ScheduleItem(1, "Senin", "07:00:00", "08:30:00", null, null, null),
                // Cached from a server that still sent "N/A".
                ScheduleItem(2, "Senin", "08:30:00", "10:00:00", "N/A", "N/A", "N/A")
            ),
            fetchedAt = 3
        )
        assertEquals(listOf(null, null), schedule.lessons.map { it.subject })
        assertEquals(listOf(null, null), schedule.lessons.map { it.room })
    }

    private fun attendanceOf(child: String?, code: String, fetchedAt: Long) = LessonAttendanceSnapshot(
        childUuid = child,
        date = "2026-09-27",
        lessons = listOf(WidgetLessonAttendance("Matematika", "07:00", code, null, awaitingScan = false, checkInTime = null)),
        fetchedAt = fetchedAt,
    )

    @Test
    fun parentWidgetsFollowOneChild() {
        val budiBilling = BillingSnapshot(childUuid = "budi", childName = "Budi", unpaidCount = 1, unpaidTotal = null, invoiceCount = null, fetchedAt = 1)

        val budi = WidgetSnapshot(role = "parent").withBilling(budiBilling).withLessonAttendance(attendanceOf("budi", "hadir", 2))
        assertEquals("Budi", budi.lessonAttendance!!.childName)
        assertEquals("budi", budi.billing!!.childUuid)

        // Switching to Siti must not leave Budi's bills next to Siti's attendance.
        val siti = budi.withLessonAttendance(attendanceOf("siti", "sakit", 3))
        assertEquals("siti", siti.lessonAttendance!!.childUuid)
        assertNull(siti.billing)
        assertNull(siti.lessonAttendance!!.childName)

        val sitiNamed = siti.withBilling(budiBilling.copy(childUuid = "siti", childName = "Siti"))
        assertEquals("Siti", sitiNamed.lessonAttendance!!.childName)
    }

    @Test
    fun studentSectionsCoexist() {
        val snapshot = WidgetSnapshot(role = "student")
            .withBilling(BillingSnapshot(unpaidCount = 1, unpaidTotal = 5.0, invoiceCount = 2, fetchedAt = 1))
            .withLessonAttendance(attendanceOf(null, "hadir", 2))
        assertEquals(1, snapshot.billing!!.unpaidCount)
        assertEquals("hadir", snapshot.lessonAttendance!!.lessons!!.single().statusCode)
    }

    @Test
    fun aSnapshotSavedByAnOlderVersionDropsItsDailyAttendance() {
        // Older versions stored the daily record under "attendance"; it is not read back as lessons.
        val old = """{"ownerUserId":"12","role":"student","attendance":{"date":"2026-09-27","statusCode":"H","fetchedAt":1}}"""
        val snapshot = com.google.gson.Gson().fromJson(old, WidgetSnapshot::class.java)
        assertEquals("12", snapshot.ownerUserId)
        assertNull(snapshot.lessonAttendance)
    }
}
