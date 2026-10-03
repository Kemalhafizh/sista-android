package com.sultanagung1.sista.ui.notifications

import com.sultanagung1.sista.data.model.NotificationChannelType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NotificationsUiTest {

    private val today = LocalDate.of(2026, 9, 30)

    @Test
    fun `days read as people say them`() {
        assertEquals("Hari ini", dayHeader(today, today))
        assertEquals("Kemarin", dayHeader(today.minusDays(1), today))
        assertEquals("Senin, 28 Sep", dayHeader(LocalDate.of(2026, 9, 28), today))
        assertEquals("Rabu, 31 Des 2025", dayHeader(LocalDate.of(2025, 12, 31), today))
    }

    @Test
    fun `times are in the school's zone`() {
        // 09:20 UTC is 16:20 in Jakarta.
        assertEquals("16.20", clock(parseInstant("2026-09-30T09:20:00Z")!!))
        assertEquals(null, parseInstant("bukan waktu"))
    }

    @Test
    fun `items group by day, newest first, undated last`() {
        val items = listOf(
            "a" to "2026-09-29T01:00:00+07:00",
            "b" to "2026-09-30T08:00:00+07:00",
            "c" to null,
            "d" to "2026-09-30T15:00:00+07:00",
        )
        val groups = groupByDay(items, today) { parseInstant(it.second) }
        assertEquals(listOf("Hari ini", "Kemarin", "Lainnya"), groups.map { it.first })
        assertEquals(listOf("d", "b"), groups[0].second.map { it.first })
    }

    @Test
    fun `an unknown channel is general information`() {
        assertEquals(NotificationChannelType.FINANCE, channelOf("financial_reminders"))
        assertEquals(NotificationChannelType.GENERAL, channelOf("tidak_dikenal"))
    }
}
