package com.sultanagung1.sista.data.model

import java.util.Calendar

/**
 * Pure rules behind the student's timetable (ScheduleScreen), so the screen
 * shows what `student/schedule` returned — per day, in time order, with a
 * status computed from the clock — instead of the sample lessons, the fixed
 * date and the always-"Berlangsung" first card it used to draw.
 */
object ScheduleRules {

    /** `schedules.day` on the server is an enum of these. */
    val SCHOOL_DAYS = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")

    private val CALENDAR_DAYS = mapOf(
        Calendar.MONDAY to "Senin",
        Calendar.TUESDAY to "Selasa",
        Calendar.WEDNESDAY to "Rabu",
        Calendar.THURSDAY to "Kamis",
        Calendar.FRIDAY to "Jumat",
        Calendar.SATURDAY to "Sabtu",
        Calendar.SUNDAY to "Minggu"
    )

    enum class LessonStatus { UPCOMING, ONGOING, DONE }

    /** Indonesian day name for a [Calendar.DAY_OF_WEEK]. */
    fun dayName(calendarDayOfWeek: Int): String = CALENDAR_DAYS[calendarDayOfWeek] ?: "Senin"

    /** Today when it is a school day, otherwise Monday (the next timetable a student looks at). */
    fun defaultDay(calendarDayOfWeek: Int): String =
        dayName(calendarDayOfWeek).takeIf { it in SCHOOL_DAYS } ?: SCHOOL_DAYS.first()

    /** Days to offer: the school week, plus any extra day the server actually schedules (e.g. Sabtu). */
    fun daysToShow(items: List<ScheduleItem>): List<String> {
        val extra = items.map { it.day.trim() }
            .filter { day -> SCHOOL_DAYS.none { it.equals(day, ignoreCase = true) } && day.isNotEmpty() }
            .distinct()
        return SCHOOL_DAYS + extra
    }

    /** One day's lessons, earliest first. */
    fun lessonsFor(items: List<ScheduleItem>, day: String): List<ScheduleItem> =
        items.filter { it.day.trim().equals(day, ignoreCase = true) }
            .sortedWith(compareBy({ minutesOf(it.startTime) ?: Int.MAX_VALUE }, { it.subjectName }))

    /** "07:00" or "07:00:00" → minutes after midnight; null when unparseable. */
    fun minutesOf(time: String?): Int? {
        val parts = time?.trim()?.split(":") ?: return null
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    /** "07:00:00" → "07:00" for display. */
    fun displayTime(time: String?): String {
        val minutes = minutesOf(time) ?: return time.orEmpty()
        return "%02d:%02d".format(minutes / 60, minutes % 60)
    }

    /**
     * Status of a lesson on the day being shown. Only today's lessons can be
     * ongoing or done; any other day is simply the timetable (UPCOMING).
     */
    fun statusOf(item: ScheduleItem, isToday: Boolean, nowMinutes: Int): LessonStatus {
        if (!isToday) return LessonStatus.UPCOMING
        val start = minutesOf(item.startTime) ?: return LessonStatus.UPCOMING
        val end = minutesOf(item.endTime) ?: start
        return when {
            nowMinutes < start -> LessonStatus.UPCOMING
            nowMinutes < end -> LessonStatus.ONGOING
            else -> LessonStatus.DONE
        }
    }

    /**
     * The lesson to put first on today's timetable: the one going on now,
     * otherwise the next one still to come. Null on other days, or when
     * today's lessons are over.
     */
    fun focusOf(lessons: List<ScheduleItem>, isToday: Boolean, nowMinutes: Int): Pair<ScheduleItem, LessonStatus>? {
        if (!isToday) return null
        lessons.firstOrNull { statusOf(it, true, nowMinutes) == LessonStatus.ONGOING }
            ?.let { return it to LessonStatus.ONGOING }
        return lessons.firstOrNull { statusOf(it, true, nowMinutes) == LessonStatus.UPCOMING }
            ?.let { it to LessonStatus.UPCOMING }
    }

    /** "Senin" → "Sen": short labels for the day chips. */
    fun shortDay(day: String): String = day.trim().take(3)

    /** Where the lesson happens: the room when known, else the class. */
    fun locationOf(item: ScheduleItem): String =
        item.roomName?.takeIf { it.isNotBlank() && it != "N/A" }
            ?: item.room.takeIf { it.isNotBlank() && it != "N/A" }
            ?: "—"

    /** The class name the timetable is for, when every lesson agrees on one. */
    fun classNameOf(items: List<ScheduleItem>): String? =
        items.map { it.room }.filter { it.isNotBlank() && it != "N/A" }.distinct().singleOrNull()
}
