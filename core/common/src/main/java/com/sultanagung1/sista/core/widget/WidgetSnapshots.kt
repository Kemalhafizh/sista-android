package com.sultanagung1.sista.core.widget

import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.LessonAttendanceDay
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.ScheduleRules
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                subject = ScheduleRules.known(it.subjectName),
                room = ScheduleRules.known(it.room)
            )
        },
        fetchedAt = fetchedAt
    )

    private val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    /**
     * GET student/class-session/attendance (student, [childUuid] null) or
     * GET parent/child/{uuid}/class-session-attendance. Lessons keep the
     * server's order (by start time); nothing is counted here.
     */
    fun lessonAttendance(childUuid: String?, day: LessonAttendanceDay, fetchedAt: Long) = LessonAttendanceSnapshot(
        childUuid = childUuid,
        date = isoDate(day.date),
        lessons = day.sessions.orEmpty().map { lesson ->
            WidgetLessonAttendance(
                subject = ScheduleRules.known(lesson.subjectName),
                start = clock(lesson.scheduledStart),
                statusCode = lesson.statusCode?.trim()?.takeIf { it.isNotEmpty() },
                statusLabel = lesson.status?.takeIf { it.isNotBlank() },
                awaitingScan = lesson.awaitingScan,
                checkInTime = schoolClock(lesson.checkedInAt)
            )
        },
        fetchedAt = fetchedAt
    )

    /** "2026-10-05T00:05:00+00:00" → "07:05": an ISO-8601 instant as a school-time clock. */
    fun schoolClock(iso: String?): String? = iso?.let {
        runCatching { OffsetDateTime.parse(it.trim()).atZoneSameInstant(SCHOOL_ZONE).format(HH_MM) }.getOrNull()
    }

    private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
 * Sets [lessonAttendance]. A parent's widgets follow one child: a record for
 * another child drops the billing section of the previous one, so the two
 * widgets never mix two children. The child's name is carried over when
 * already known.
 */
fun WidgetSnapshot.withLessonAttendance(attendance: LessonAttendanceSnapshot): WidgetSnapshot {
    val sameChildBilling = billing?.takeIf { it.childUuid == attendance.childUuid }
    val name = attendance.childName
        ?: sameChildBilling?.childName
        ?: lessonAttendance?.takeIf { it.childUuid == attendance.childUuid }?.childName
    return copy(lessonAttendance = attendance.copy(childName = name), billing = sameChildBilling)
}

/** Counterpart of [withLessonAttendance]. */
fun WidgetSnapshot.withBilling(billing: BillingSnapshot): WidgetSnapshot {
    val sameChildAttendance = lessonAttendance?.takeIf { it.childUuid == billing.childUuid }
    val name = billing.childName
        ?: sameChildAttendance?.childName
        ?: this.billing?.takeIf { it.childUuid == billing.childUuid }?.childName
    return copy(
        billing = billing.copy(childName = name),
        lessonAttendance = sameChildAttendance?.let { if (it.childName == null) it.copy(childName = name) else it }
    )
}
