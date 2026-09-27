package com.sultanagung1.sista.widget

import com.sultanagung1.sista.R
import com.sultanagung1.sista.core.widget.AttendanceSnapshot
import com.sultanagung1.sista.core.widget.WidgetSnapshot
import com.sultanagung1.sista.core.widget.WidgetSnapshots
import com.sultanagung1.sista.ui.navigation.RoleGroup
import com.sultanagung1.sista.ui.navigation.UserRoles
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * [WidgetSnapshot] → widget text.
 *
 * Rule: a status (present, paid, a lesson) is only shown when it came from the
 * snapshot. Missing data renders as "Belum ada data" / "–", never as a
 * plausible-looking default.
 */
object WidgetTextMapper {

    /** Backend `day` values, as HomeViewModel matches them. */
    private val INDONESIAN_DAYS = mapOf(
        DayOfWeek.MONDAY to "Senin",
        DayOfWeek.TUESDAY to "Selasa",
        DayOfWeek.WEDNESDAY to "Rabu",
        DayOfWeek.THURSDAY to "Kamis",
        DayOfWeek.FRIDAY to "Jumat",
        DayOfWeek.SATURDAY to "Sabtu",
        DayOfWeek.SUNDAY to "Minggu"
    )

    private const val SCHEDULE_SLOTS = 3

    private val TIME = DateTimeFormatter.ofPattern("HH:mm")
    private val DAY_TIME = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    private val DAY_MONTH = DateTimeFormatter.ofPattern("dd/MM")

    fun attendance(snapshot: WidgetSnapshot?, now: ZonedDateTime): AttendanceWidgetContent {
        if (snapshot != null && UserRoles.groupOf(snapshot.role) in STAFF) {
            return AttendanceWidgetContent(res(R.string.widget_no_data), res(R.string.widget_only_student_parent), null)
        }
        val record = snapshot?.attendance
            ?: return AttendanceWidgetContent(res(R.string.widget_no_data), res(R.string.widget_open_app), null)

        val child = record.childName?.takeIf { it.isNotBlank() }
        val updated = updatedAt(record.fetchedAt, now)
        // Local copy: record comes from :core:common, so its properties don't smart-cast here.
        val rawDate = record.date
        val date = rawDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        return when {
            rawDate == null ->
                AttendanceWidgetContent(res(R.string.widget_attendance_none), child?.let(WidgetText::Raw), updated)
            date == now.toLocalDate() -> {
                val label = statusLabel(record)
                val status = record.checkInTime
                    ?.let { res(R.string.widget_attendance_today_at, label, it) }
                    ?: res(R.string.widget_attendance_today, label)
                AttendanceWidgetContent(status, child?.let(WidgetText::Raw), updated)
            }
            else -> {
                // The newest record is from another day. Nothing is known about
                // today beyond "not recorded when the app last loaded it".
                val day = date?.format(DAY_MONTH) ?: rawDate
                val last = if (child != null) {
                    res(R.string.widget_attendance_child_last, child, statusLabel(record), day)
                } else {
                    res(R.string.widget_attendance_last, statusLabel(record), day)
                }
                AttendanceWidgetContent(res(R.string.widget_attendance_not_yet_today), last, updated)
            }
        }
    }

    fun spp(snapshot: WidgetSnapshot?, now: ZonedDateTime): SppWidgetContent {
        if (snapshot != null && UserRoles.groupOf(snapshot.role) in STAFF) {
            return SppWidgetContent(res(R.string.widget_unknown_value), res(R.string.widget_only_student_parent), null)
        }
        val billing = snapshot?.billing
            ?: return SppWidgetContent(res(R.string.widget_unknown_value), res(R.string.widget_open_app), null)

        val child = billing.childName?.takeIf { it.isNotBlank() }?.let(WidgetText::Raw)
        val updated = updatedAt(billing.fetchedAt, now)
        return when {
            billing.unpaidCount > 0 -> SppWidgetContent(
                status = res(R.string.widget_spp_unpaid, billing.unpaidCount),
                detail = child ?: billing.unpaidTotal?.let { res(R.string.widget_spp_remaining, rupiah(it)) },
                updated = updated
            )
            billing.invoiceCount == 0 -> SppWidgetContent(res(R.string.widget_spp_none), child, updated)
            else -> SppWidgetContent(res(R.string.widget_spp_clear), child, updated)
        }
    }

    fun schedule(snapshot: WidgetSnapshot?, now: ZonedDateTime): ScheduleWidgetContent {
        if (snapshot != null && UserRoles.groupOf(snapshot.role) != RoleGroup.STUDENT) {
            return ScheduleWidgetContent(res(R.string.widget_unknown_value), listOf(res(R.string.widget_only_student)))
        }
        val schedule = snapshot?.schedule
            ?: return ScheduleWidgetContent(
                res(R.string.widget_unknown_value),
                listOf(res(R.string.widget_no_data), res(R.string.widget_open_app))
            )

        val todayName = INDONESIAN_DAYS.getValue(now.dayOfWeek)
        val today = schedule.lessons
            .filter { it.day.equals(todayName, ignoreCase = true) }
            .sortedBy { it.start }
        val count = res(R.string.widget_schedule_count, today.size)
        if (today.isEmpty()) {
            return ScheduleWidgetContent(count, listOf(res(R.string.widget_schedule_free_day)))
        }

        val lines = today.map { lesson ->
            val start = WidgetSnapshots.clock(lesson.start) ?: lesson.start
            val room = lesson.room?.takeIf { it.isNotBlank() }
            if (room != null) {
                res(R.string.widget_schedule_lesson, start, lesson.subject, room)
            } else {
                res(R.string.widget_schedule_lesson_no_room, start, lesson.subject)
            }
        }
        val shown = if (lines.size <= SCHEDULE_SLOTS) {
            lines
        } else {
            lines.take(SCHEDULE_SLOTS - 1) + res(R.string.widget_schedule_more, lines.size - (SCHEDULE_SLOTS - 1))
        }
        return ScheduleWidgetContent(count, shown)
    }

    /** "Rp 2.550.000", the format BillingScreen shows. */
    fun rupiah(amount: Double): String =
        "Rp " + String.format(Locale.US, "%,d", Math.round(amount)).replace(',', '.')

    private val STAFF = setOf(RoleGroup.TEACHER, RoleGroup.ADMIN)

    /** Both spellings the backend stores: codes from class attendance, words from GPS check-in. */
    private fun statusLabel(record: AttendanceSnapshot): WidgetText = when (record.statusCode?.trim()?.uppercase()) {
        "H", "HADIR" -> res(R.string.widget_attendance_status_h)
        "S", "SAKIT" -> res(R.string.widget_attendance_status_s)
        "I", "IZIN" -> res(R.string.widget_attendance_status_i)
        "A", "ALPA", "ALPHA", "ALFA" -> res(R.string.widget_attendance_status_a)
        "T", "TERLAMBAT" -> res(R.string.widget_attendance_status_t)
        else -> record.statusLabel?.takeIf { it.isNotBlank() }?.let(WidgetText::Raw)
            ?: record.statusCode?.takeIf { it.isNotBlank() }?.let(WidgetText::Raw)
            ?: res(R.string.widget_unknown_value)
    }

    private fun updatedAt(fetchedAt: Long, now: ZonedDateTime): WidgetText {
        val at = Instant.ofEpochMilli(fetchedAt).atZone(now.zone)
        val label = if (at.toLocalDate() == now.toLocalDate()) at.format(TIME) else at.format(DAY_TIME)
        return res(R.string.widget_updated_at, label)
    }

    private fun res(id: Int, vararg args: Any): WidgetText = WidgetText.Res(id, args.toList())
}
