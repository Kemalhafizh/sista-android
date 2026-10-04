package com.sultanagung1.sista.widget

import com.sultanagung1.sista.core.widget.AttendanceSnapshot
import com.sultanagung1.sista.core.widget.BillingSnapshot
import com.sultanagung1.sista.core.widget.WidgetSnapshot
import com.sultanagung1.sista.core.widget.WidgetSnapshots
import com.sultanagung1.sista.core.widget.withAttendance
import com.sultanagung1.sista.core.widget.withBilling
import com.sultanagung1.sista.data.model.AttendanceHistoryItem
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.model.ChildAttendanceLog
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.ChildSummaryStatistics
import com.sultanagung1.sista.data.model.ChildSummaryStudent
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

    @Test
    fun studentAttendancePicksTheNewestDateNotTheFirstRow() {
        val rows = listOf(
            AttendanceHistoryItem(1, "2026-09-24", null, null, "H", null),
            AttendanceHistoryItem(2, "2026-09-26T00:00:00.000000Z", "06:41:07", null, "S", null),
            AttendanceHistoryItem(3, "2026-09-25", null, null, "A", null)
        )
        val latest = WidgetSnapshots.studentAttendance(rows, fetchedAt = 9)!!
        assertEquals("2026-09-26", latest.date)
        assertEquals("S", latest.statusCode)
        assertEquals("06:41", latest.checkInTime)
    }

    @Test
    fun emptyHistoryIsRecordedAsNoAttendanceButUnreadableDatesAreNot() {
        assertNull(WidgetSnapshots.studentAttendance(emptyList(), fetchedAt = 1)!!.date)
        assertNull(WidgetSnapshots.studentAttendance(listOf(AttendanceHistoryItem(1, "kemarin", null, null, "H", null)), 1))
        assertNull(WidgetSnapshots.childAttendance("c-1", listOf(ChildAttendanceLog(1, "", "H", "Hadir")), 1))
    }

    @Test
    fun childAttendanceKeepsTheServerLabel() {
        val latest = WidgetSnapshots.childAttendance(
            "c-1",
            listOf(ChildAttendanceLog(1, "2026-09-25", "H", "Hadir"), ChildAttendanceLog(2, "2026-09-26", "I", "Izin")),
            fetchedAt = 1
        )!!
        assertEquals("c-1", latest.childUuid)
        assertEquals("2026-09-26", latest.date)
        assertEquals("Izin", latest.statusLabel)
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

    @Test
    fun parentWidgetsFollowOneChild() {
        val budiBilling = BillingSnapshot(childUuid = "budi", childName = "Budi", unpaidCount = 1, unpaidTotal = null, invoiceCount = null, fetchedAt = 1)
        val budiAttendance = AttendanceSnapshot(childUuid = "budi", date = "2026-09-27", statusCode = "H", statusLabel = "Hadir", fetchedAt = 2)
        val sitiAttendance = AttendanceSnapshot(childUuid = "siti", date = "2026-09-27", statusCode = "S", statusLabel = "Sakit", fetchedAt = 3)

        val budi = WidgetSnapshot(role = "parent").withBilling(budiBilling).withAttendance(budiAttendance)
        assertEquals("Budi", budi.attendance!!.childName)
        assertEquals("budi", budi.billing!!.childUuid)

        // Switching to Siti must not leave Budi's bills next to Siti's attendance.
        val siti = budi.withAttendance(sitiAttendance)
        assertEquals("siti", siti.attendance!!.childUuid)
        assertNull(siti.billing)
        assertNull(siti.attendance!!.childName)

        val sitiNamed = siti.withBilling(budiBilling.copy(childUuid = "siti", childName = "Siti"))
        assertEquals("Siti", sitiNamed.attendance!!.childName)
    }

    @Test
    fun studentSectionsCoexist() {
        val snapshot = WidgetSnapshot(role = "student")
            .withBilling(BillingSnapshot(unpaidCount = 1, unpaidTotal = 5.0, invoiceCount = 2, fetchedAt = 1))
            .withAttendance(AttendanceSnapshot(date = "2026-09-27", statusCode = "H", statusLabel = null, fetchedAt = 2))
        assertEquals(1, snapshot.billing!!.unpaidCount)
        assertEquals("H", snapshot.attendance!!.statusCode)
    }
}
