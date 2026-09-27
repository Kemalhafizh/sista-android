package com.sultanagung1.sista.academic

import com.google.gson.Gson
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.ScheduleRules
import com.sultanagung1.sista.data.model.ScheduleRules.LessonStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.util.Calendar

/**
 * The timetable shows what `student/schedule` returns, per day and in time
 * order, with a status from the clock — no sample lessons, fixed date or
 * always-"Berlangsung" first card.
 */
class ScheduleRulesTest {

    private fun lesson(id: Long, day: String, start: String, end: String, subject: String = "Mapel $id", room: String? = null) =
        ScheduleItem(id, day, start, end, subject, "Guru $id", "XI MIPA 2", room)

    @Test
    fun `lessons are filtered by the selected day and sorted by start time`() {
        val all = listOf(
            lesson(9, "Senin", "10:15:00", "11:45:00"),
            lesson(3, "Selasa", "07:00:00", "08:30:00"),
            lesson(5, "Senin", "07:00:00", "08:30:00"),
            lesson(7, "senin", "08:30", "10:00")
        )

        assertEquals(listOf(5L, 7L, 9L), ScheduleRules.lessonsFor(all, "Senin").map { it.id })
        assertEquals(listOf(3L), ScheduleRules.lessonsFor(all, "Selasa").map { it.id })
        assertEquals(emptyList<ScheduleItem>(), ScheduleRules.lessonsFor(all, "Rabu"))
    }

    @Test
    fun `status comes from the clock and only applies to today`() {
        val item = lesson(1, "Senin", "07:00:00", "08:30:00")
        val at = { h: Int, m: Int -> h * 60 + m }

        assertEquals(LessonStatus.UPCOMING, ScheduleRules.statusOf(item, isToday = true, nowMinutes = at(6, 59)))
        assertEquals(LessonStatus.ONGOING, ScheduleRules.statusOf(item, isToday = true, nowMinutes = at(7, 0)))
        assertEquals(LessonStatus.ONGOING, ScheduleRules.statusOf(item, isToday = true, nowMinutes = at(8, 29)))
        assertEquals(LessonStatus.DONE, ScheduleRules.statusOf(item, isToday = true, nowMinutes = at(8, 30)))
        // Another day's timetable is never "Berlangsung".
        assertEquals(LessonStatus.UPCOMING, ScheduleRules.statusOf(item, isToday = false, nowMinutes = at(7, 30)))
    }

    @Test
    fun `default day is today on school days and Monday otherwise`() {
        assertEquals("Rabu", ScheduleRules.defaultDay(Calendar.WEDNESDAY))
        assertEquals("Jumat", ScheduleRules.defaultDay(Calendar.FRIDAY))
        assertEquals("Senin", ScheduleRules.defaultDay(Calendar.SATURDAY))
        assertEquals("Senin", ScheduleRules.defaultDay(Calendar.SUNDAY))
        assertEquals("Minggu", ScheduleRules.dayName(Calendar.SUNDAY))
    }

    @Test
    fun `days offered are the school week plus any extra day the server schedules`() {
        assertEquals(ScheduleRules.SCHOOL_DAYS, ScheduleRules.daysToShow(emptyList()))
        assertEquals(
            ScheduleRules.SCHOOL_DAYS + "Sabtu",
            ScheduleRules.daysToShow(listOf(lesson(1, "Sabtu", "07:00", "08:00"), lesson(2, "senin", "07:00", "08:00")))
        )
    }

    @Test
    fun `times are parsed and shown without seconds`() {
        assertEquals(7 * 60, ScheduleRules.minutesOf("07:00:00"))
        assertEquals(13 * 60 + 45, ScheduleRules.minutesOf("13:45"))
        assertNull(ScheduleRules.minutesOf("25:00"))
        assertNull(ScheduleRules.minutesOf("pagi"))
        assertEquals("07:05", ScheduleRules.displayTime("7:05:00"))
        assertEquals("pagi", ScheduleRules.displayTime("pagi"))
    }

    @Test
    fun `location prefers the room and the badge is the class all lessons share`() {
        assertEquals("Lab Fisika", ScheduleRules.locationOf(lesson(1, "Senin", "07:00", "08:00", room = "Lab Fisika")))
        assertEquals("XI MIPA 2", ScheduleRules.locationOf(lesson(1, "Senin", "07:00", "08:00", room = null)))
        assertEquals("XI MIPA 2", ScheduleRules.classNameOf(listOf(lesson(1, "Senin", "07:00", "08:00"), lesson(2, "Selasa", "07:00", "08:00"))))
        assertNull(ScheduleRules.classNameOf(emptyList()))
    }

    @Test
    fun `parses the backend ScheduleResource payload including room_name`() {
        val json = """
            {"id":12,"day":"Kamis","session_start":"07:00:00","session_end":"08:30:00","subject_id":3,
             "subject_name":"Fisika","subject_code":"FIS","teacher_name":"Bu Rina","classroom_id":4,
             "classroom_name":"XI MIPA 2","room_name":null}
        """.trimIndent()
        val item = Gson().fromJson(json, ScheduleItem::class.java)

        assertEquals("Kamis", item.day)
        assertEquals("XI MIPA 2", item.room)
        assertNull(item.roomName)
        assertEquals("XI MIPA 2", ScheduleRules.locationOf(item))
    }

    @Test
    fun `the schedule screen no longer ships sample lessons or fixed dates`() {
        val screen = File("../feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/ScheduleScreen.kt")
            .takeIf { it.exists() } ?: File("feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/ScheduleScreen.kt")
        val source = screen.readText()
        listOf("sampleSchedules", "Sarah Jenkins", "20 September 2026", "\"XII MIPA 1\"", "delay(600)").forEach {
            assertEquals("ScheduleScreen still contains $it", false, source.contains(it))
        }
    }
}
