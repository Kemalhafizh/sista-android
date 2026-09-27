package com.sultanagung1.sista.core.widget

import com.sultanagung1.sista.data.model.AttendanceHistoryItem
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.model.ChildAttendanceLog
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.ScheduleItem

/**
 * API response → [WidgetSnapshot] section. Pure functions, no Android types,
 * so the unit tests can pin exactly what reaches the widgets.
 */
object WidgetSnapshots {

    private val ISO_DATE = Regex("""^\d{4}-\d{2}-\d{2}""")
    private val CLOCK = Regex("""(\d{1,2}):(\d{2})""")

    fun schedule(items: List<ScheduleItem>, fetchedAt: Long) = ScheduleSnapshot(
        lessons = items.map {
            WidgetLesson(
                day = it.day,
                start = it.startTime,
                end = it.endTime,
                subject = it.subjectName,
                room = it.room
            )
        },
        fetchedAt = fetchedAt
    )

    /**
     * Latest row of GET student/attendance. The backend orders by created_at,
     * not by date, so the newest date is picked here. Returns null (record
     * nothing) when rows exist but none has a readable date: "unknown" must not
     * turn into "no attendance recorded".
     */
    fun studentAttendance(items: List<AttendanceHistoryItem>, fetchedAt: Long): AttendanceSnapshot? {
        if (items.isEmpty()) {
            return AttendanceSnapshot(date = null, statusCode = null, statusLabel = null, fetchedAt = fetchedAt)
        }
        val latest = items
            .mapNotNull { item -> isoDate(item.date)?.let { it to item } }
            .maxByOrNull { it.first }
            ?: return null
        return AttendanceSnapshot(
            date = latest.first,
            statusCode = latest.second.status,
            statusLabel = null,
            checkInTime = latest.second.checkIn?.let(::clock),
            fetchedAt = fetchedAt
        )
    }

    /** Same as [studentAttendance] for GET parent/child/{uuid}/attendance. */
    fun childAttendance(childUuid: String, logs: List<ChildAttendanceLog>, fetchedAt: Long): AttendanceSnapshot? {
        if (logs.isEmpty()) {
            return AttendanceSnapshot(
                childUuid = childUuid,
                date = null,
                statusCode = null,
                statusLabel = null,
                fetchedAt = fetchedAt
            )
        }
        val latest = logs
            .mapNotNull { log -> isoDate(log.date)?.let { it to log } }
            .maxByOrNull { it.first }
            ?: return null
        return AttendanceSnapshot(
            childUuid = childUuid,
            date = latest.first,
            statusCode = latest.second.status,
            statusLabel = latest.second.statusLabel,
            fetchedAt = fetchedAt
        )
    }

    /** Anything not explicitly "paid" (unpaid, partial, pending…) is still open, as in BillingViewModel. */
    fun studentBilling(invoices: List<BillingInvoice>, fetchedAt: Long): BillingSnapshot {
        val open = invoices.filterNot { it.status.equals("paid", ignoreCase = true) }
        return BillingSnapshot(
            unpaidCount = open.size,
            unpaidTotal = open.sumOf { it.remainingBalance },
            invoiceCount = invoices.size,
            fetchedAt = fetchedAt
        )
    }

    /** The parent API only exposes the unpaid count, so amount and total count stay unknown (null). */
    fun childBilling(childUuid: String, summary: ChildSummaryResponse, fetchedAt: Long) = BillingSnapshot(
        childUuid = childUuid,
        childName = summary.student.name.takeIf { it.isNotBlank() },
        unpaidCount = summary.statistics.unpaidBillingsCount,
        unpaidTotal = null,
        invoiceCount = null,
        fetchedAt = fetchedAt
    )

    fun isoDate(raw: String?): String? = raw?.trim()?.let { ISO_DATE.find(it)?.value }

    fun clock(raw: String?): String? {
        val match = raw?.let { CLOCK.find(it) } ?: return null
        val (h, m) = match.destructured
        return h.padStart(2, '0') + ":" + m
    }
}

/**
 * Sets [attendance]. A parent's widgets follow one child: a record for another
 * child drops the billing section of the previous one, so the two widgets never
 * mix two children. The child's name is carried over when already known.
 */
fun WidgetSnapshot.withAttendance(attendance: AttendanceSnapshot): WidgetSnapshot {
    val sameChildBilling = billing?.takeIf { it.childUuid == attendance.childUuid }
    val name = attendance.childName
        ?: sameChildBilling?.childName
        ?: this.attendance?.takeIf { it.childUuid == attendance.childUuid }?.childName
    return copy(attendance = attendance.copy(childName = name), billing = sameChildBilling)
}

/** Counterpart of [withAttendance]. */
fun WidgetSnapshot.withBilling(billing: BillingSnapshot): WidgetSnapshot {
    val sameChildAttendance = attendance?.takeIf { it.childUuid == billing.childUuid }
    val name = billing.childName
        ?: sameChildAttendance?.childName
        ?: this.billing?.takeIf { it.childUuid == billing.childUuid }?.childName
    return copy(
        billing = billing.copy(childName = name),
        attendance = sameChildAttendance?.let { if (it.childName == null) it.copy(childName = name) else it }
    )
}
