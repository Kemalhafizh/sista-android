package com.sultanagung1.sista.ui.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class AnalyticsFormatTest {

    @Test
    fun `no figure is shown as a dash, not zero`() {
        assertEquals("–", decimal(null))
    }

    @Test
    fun `numbers read the Indonesian way`() {
        assertEquals("96,5", decimal(96.54))
        assertEquals("90", decimal(90.0))
        assertEquals("97", decimal(96.54, digits = 0))
        assertEquals("+12,5", signed(12.5))
        assertEquals("−3", signed(-3.0))
        assertEquals("12.480", thousands(12480))
    }

    @Test
    fun `a day without records is never a holiday`() {
        assertEquals(DayMark.Unrecorded, dayMarkOf("LIBUR"))
        assertEquals(DayMark.Unrecorded, dayMarkOf("KOSONG"))
        assertEquals(DayMark.Present, dayMarkOf("HADIR"))
        assertEquals(DayMark.Absent, dayMarkOf("ALPA"))
    }

    @Test
    fun `the month starts on its weekday and stops marking after today`() {
        // 1 Sep 2026 is a Tuesday.
        val weeks = monthGrid(LocalDate.of(2026, 9, 10), mapOf(1 to DayMark.Present, 10 to DayMark.Sick, 11 to DayMark.Absent))
        assertEquals(5, weeks.size)
        assertNull(weeks[0][0])
        assertEquals(MonthCell(1, DayMark.Present), weeks[0][1])
        assertEquals(MonthCell(10, DayMark.Sick), weeks[1][3])
        assertEquals(MonthCell(11, null), weeks[1][4])
        assertEquals(MonthCell(30, null), weeks[4][2])
        assertNull(weeks[4][6])
        assertEquals("September 2026", monthLabel(LocalDate.of(2026, 9, 10)))
    }
}
