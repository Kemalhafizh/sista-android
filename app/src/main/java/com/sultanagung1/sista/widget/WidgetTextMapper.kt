package com.sultanagung1.sista.widget

import com.sultanagung1.sista.R
import com.sultanagung1.sista.core.widget.WidgetLessonAttendance
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

    /**
     * Today's per-lesson QR attendance (FASE 117). Counts only what the server
     * has recorded; a running class the student has not scanned yet reads
     * "belum absen", not "alpa".
     */
    fun attendance(snapshot: WidgetSnapshot?, now: ZonedDateTime): AttendanceWidgetContent {
        if (snapshot != null && UserRoles.groupOf(snapshot.role) in STAFF) {
            return AttendanceWidgetContent(res(R.string.widget_no_data), res(R.string.widget_only_student_parent), null)
        }
        val record = snapshot?.lessonAttendance
            ?: return AttendanceWidgetContent(res(R.string.widget_no_data), res(R.string.widget_open_app), null)

        val child = record.childName?.takeIf { it.isNotBlank() }
        val updated = updatedAt(record.fetchedAt, now)
        val date = record.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (date != now.toLocalDate()) {
            // Loaded on another day: nothing is known about today yet.
            val detail = child?.let(WidgetText::Raw) ?: res(R.string.widget_open_app)
            return AttendanceWidgetContent(res(R.string.widget_attendance_not_yet_today), detail, updated)
        }

        val lessons = record.lessons.orEmpty()
        if (lessons.isEmpty()) {
            return AttendanceWidgetContent(res(R.string.widget_lessons_none_today), child?.let(WidgetText::Raw), updated)
        }

        val waiting = lessons.firstOrNull { it.awaitingScan }
        val recorded = lessons.filter { it.statusCode != null && !it.awaitingScan }
        val status = when {
            waiting != null -> res(R.string.widget_lessons_awaiting_scan, subjectOf(waiting))
            recorded.isEmpty() -> res(R.string.widget_attendance_none)
            else -> res(
                R.string.widget_lessons_present,
                recorded.count { it.statusCode?.lowercase() in PRESENT },
                recorded.size
            )
        }
        val last = recorded.lastOrNull()?.let(::lessonLine)
        val detail = when {
            child != null && last != null -> res(R.string.widget_child_and, child, last)
            child != null -> WidgetText.Raw(child)
            else -> last
        }
        return AttendanceWidgetContent(status, detail, updated)
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
            val subject = lesson.subject?.takeIf { it.isNotBlank() } ?: res(R.string.widget_unknown_value)
            if (room != null) {
                res(R.string.widget_schedule_lesson, start, subject, room)
            } else {
                res(R.string.widget_schedule_lesson_no_room, start, subject)
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

    private val STAFF = setOf(RoleGroup.TEACHER, RoleGroup.ADMIN, RoleGroup.ACADEMIC_STAFF)

    /** hadir and telat both mean the student was in class, as on the server (PRESENT_STATUSES). */
    private val PRESENT = setOf("hadir", "telat")

    private fun subjectOf(lesson: WidgetLessonAttendance): WidgetText =
        lesson.subject?.takeIf { it.isNotBlank() }?.let(WidgetText::Raw) ?: res(R.string.widget_unknown_value)

    /** "Matematika: Hadir (07:05)" */
    private fun lessonLine(lesson: WidgetLessonAttendance): WidgetText {
        val time = lesson.checkInTime
        return if (time != null) {
            res(R.string.widget_lesson_status_at, subjectOf(lesson), statusLabel(lesson), time)
        } else {
            res(R.string.widget_lesson_status, subjectOf(lesson), statusLabel(lesson))
        }
    }

    /** The server's status codes (SessionAttendance::STATUSES); its own label for anything else. */
    private fun statusLabel(lesson: WidgetLessonAttendance): WidgetText = when (lesson.statusCode?.trim()?.lowercase()) {
        "hadir" -> res(R.string.widget_attendance_status_h)
        "sakit" -> res(R.string.widget_attendance_status_s)
        "izin" -> res(R.string.widget_attendance_status_i)
        "alpha" -> res(R.string.widget_attendance_status_a)
        "telat" -> res(R.string.widget_attendance_status_t)
        else -> lesson.statusLabel?.takeIf { it.isNotBlank() }?.let(WidgetText::Raw)
            ?: lesson.statusCode?.takeIf { it.isNotBlank() }?.let(WidgetText::Raw)
            ?: res(R.string.widget_unknown_value)
    }

    private fun updatedAt(fetchedAt: Long, now: ZonedDateTime): WidgetText {
        val at = Instant.ofEpochMilli(fetchedAt).atZone(now.zone)
        val label = if (at.toLocalDate() == now.toLocalDate()) at.format(TIME) else at.format(DAY_TIME)
        return res(R.string.widget_updated_at, label)
    }

    private fun res(id: Int, vararg args: Any): WidgetText = WidgetText.Res(id, args.toList())
}
