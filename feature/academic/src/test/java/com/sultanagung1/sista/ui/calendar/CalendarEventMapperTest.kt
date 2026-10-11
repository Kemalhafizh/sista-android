package com.sultanagung1.sista.ui.calendar

import com.sultanagung1.sista.data.model.AcademicCalendarEventItem
import com.sultanagung1.sista.data.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarEventMapperTest {

    private fun item(
        category: String = "other",
        description: String? = null,
        location: String? = null,
        isHoliday: Boolean = false,
    ) = AcademicCalendarEventItem(
        id = 7,
        title = "Upacara Hari Pahlawan",
        description = description,
        category = category,
        startTime = "2026-11-10 07:00:00",
        endTime = "2026-11-10 09:00:00",
        location = location,
        isHoliday = isHoliday,
    )

    @Test
    fun aMissingDescriptionOrLocationStaysEmpty() {
        val event = CalendarEventMapper.toEvent(item(location = null, description = null))
        assertNull(event.location)
        assertNull(event.description)
        // Blank text from the server is no information either.
        assertNull(CalendarEventMapper.toEvent(item(location = "  ", description = "")).location)
    }

    @Test
    fun whatTheServerSendsIsShownAsIs() {
        val event = CalendarEventMapper.toEvent(item(description = "Seragam putih lengkap.", location = "Lapangan utama"))
        assertEquals("Seragam putih lengkap.", event.description)
        assertEquals("Lapangan utama", event.location)
        assertEquals("7", event.id)
        assertEquals("2026-11-10", event.date)
        assertEquals("2026-11-10 07:00:00", event.startTime)
    }

    @Test
    fun theWebsEventTypeCodesAreRecognised() {
        // AcademicEventController: in:exam,holiday,meeting,ceremony,tryout,osis,other
        assertEquals(EventType.UJIAN, CalendarEventMapper.typeOf("exam"))
        assertEquals(EventType.UJIAN, CalendarEventMapper.typeOf("tryout"))
        assertEquals(EventType.LIBUR, CalendarEventMapper.typeOf("holiday"))
        assertEquals(EventType.RAPAT, CalendarEventMapper.typeOf("meeting"))
        assertEquals(EventType.EKSKUL, CalendarEventMapper.typeOf("osis"))
        assertEquals(EventType.KEGIATAN, CalendarEventMapper.typeOf("ceremony"))
        assertEquals(EventType.KEGIATAN, CalendarEventMapper.typeOf("other"))
    }

    @Test
    fun aHolidayFlagWinsAndAnUnknownCodeIsAnEvent() {
        assertEquals(EventType.LIBUR, CalendarEventMapper.toEvent(item(category = "other", isHoliday = true)).type)
        assertEquals(EventType.KEGIATAN, CalendarEventMapper.typeOf("study_tour"))
        assertEquals(EventType.KEGIATAN, CalendarEventMapper.typeOf(null))
        assertEquals(EventType.UJIAN, CalendarEventMapper.typeOf(" EXAM "))
    }
}
