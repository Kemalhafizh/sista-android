package com.sultanagung1.sista.ui.calendar

import com.sultanagung1.sista.data.model.AcademicCalendarEventItem
import com.sultanagung1.sista.data.model.CalendarEvent
import com.sultanagung1.sista.data.model.EventType

/**
 * GET academic-calendar → what the calendar shows. The server sends the web's
 * AcademicEvent: `category` is its event_type code
 * (exam, holiday, meeting, ceremony, tryout, osis, other; see
 * AcademicEventController). A missing description or location stays null.
 */
object CalendarEventMapper {

    fun typeOf(category: String?, isHoliday: Boolean = false): EventType {
        if (isHoliday) return EventType.LIBUR
        return when (category?.trim()?.lowercase()) {
            "exam", "tryout" -> EventType.UJIAN
            "holiday" -> EventType.LIBUR
            "meeting" -> EventType.RAPAT
            "osis" -> EventType.EKSKUL
            else -> EventType.KEGIATAN // ceremony, other, or a code this app doesn't know yet
        }
    }

    fun toEvent(item: AcademicCalendarEventItem): CalendarEvent = CalendarEvent(
        id = item.id.toString(),
        title = item.title,
        date = item.startTime.substringBefore("T").substringBefore(" "),
        type = typeOf(item.category, item.isHoliday),
        description = item.description?.takeIf { it.isNotBlank() },
        location = item.location?.takeIf { it.isNotBlank() },
        startTime = item.startTime,
        endTime = item.endTime,
    )
}
