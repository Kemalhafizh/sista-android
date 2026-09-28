package com.sultanagung1.sista.ui.analytics

import com.sultanagung1.sista.data.model.AttendanceDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class AnalyticsFormatTest {

    @Test
    fun `overall rate weighs days by how many records they have`() {
        val days = listOf(AttendanceDay("2026-09-28", recorded = 100, present = 90, rate = 90.0), AttendanceDay("2026-09-29", recorded = 2, present = 0, rate = 0.0))
        // A plain mean of the day rates would say 45%.
        assertEquals(88.235, overallRate(days)!!, 0.001)
    }

    @Test
    fun `no records means no rate, not zero`() {
        assertNull(overallRate(emptyList()))
        assertEquals("–", decimal(null))
    }

    @Test
    fun `numbers read the Indonesian way`() {
        assertEquals("96,5", decimal(96.54))
        assertEquals("90", decimal(90.0))
        assertEquals("97", decimal(96.54, digits = 0))
        assertEquals("+12,5", signed(12.5))
        assertEquals("−3", signed(-3.0))
        assertEquals("Rp 950.000", rupiahShort(950_000.0))
        assertEquals("Rp 12,5 jt", rupiahShort(12_500_000.0))
        assertEquals("12.480", thousands(12480))
    }

    @Test
    fun `day labels come from the date`() {
        assertEquals("Sen" to "28", dayParts("2026-09-28"))
        assertEquals("Rab 30 Sep", shortDay("2026-09-30"))
        assertEquals("" to "bukan tanggal", dayParts("bukan tanggal"))
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
