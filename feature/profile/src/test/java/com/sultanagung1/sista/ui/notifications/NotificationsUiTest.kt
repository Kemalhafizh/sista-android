package com.sultanagung1.sista.ui.notifications

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.data.model.NotificationChannelType
import com.sultanagung1.sista.feature.profile.R
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class NotificationsUiTest {

    private val today = LocalDate.of(2026, 9, 30)

    private val id = Locale.forLanguageTag("id-ID")
    private val en = Locale.forLanguageTag("en-US")
    private val ar = Locale.forLanguageTag("ar-u-nu-latn")

    @Test
    fun `days read as people say them, in the app's language`() {
        assertEquals(UiText.Res(R.string.notif_today), dayHeader(today, today, id))
        assertEquals(UiText.Res(R.string.notif_yesterday), dayHeader(today.minusDays(1), today, id))
        assertEquals(UiText.Raw("Senin, 28 Sep"), dayHeader(LocalDate.of(2026, 9, 28), today, id))
        assertEquals(UiText.Raw("Rabu, 31 Des 2025"), dayHeader(LocalDate.of(2025, 12, 31), today, id))
        assertEquals(UiText.Raw("Monday, 28 Sep"), dayHeader(LocalDate.of(2026, 9, 28), today, en))
        // Arabic day and month names, Latin digits like the web.
        val arabic = (dayHeader(LocalDate.of(2026, 9, 28), today, ar) as UiText.Raw).value
        assert(arabic.contains("28") && arabic.any { it in '\u0600'..'\u06FF' }) { arabic }
    }

    @Test
    fun `times are in the school's zone`() {
        // 09:20 UTC is 16:20 in Jakarta.
        val time = parseInstant("2026-09-30T09:20:00Z")!!
        assertEquals("16.20", clock(time, id))
        assertEquals("16:20", clock(time, en))
        assertEquals("16:20", clock(time, ar))
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
        assertEquals(
            listOf(UiText.Res(R.string.notif_today), UiText.Res(R.string.notif_yesterday), UiText.Res(R.string.notif_other)),
            groups.map { it.first },
        )
        assertEquals(listOf("d", "b"), groups[0].second.map { it.first })
    }

    @Test
    fun `an unknown channel is general information`() {
        assertEquals(NotificationChannelType.FINANCE, channelOf("financial_reminders"))
        assertEquals(NotificationChannelType.GENERAL, channelOf("tidak_dikenal"))
    }
}
