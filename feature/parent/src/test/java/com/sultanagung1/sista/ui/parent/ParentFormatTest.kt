package com.sultanagung1.sista.ui.parent

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.parent.R
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Dates, numbers and event colours on the parent's screens, in each of the app's languages. */
class ParentFormatTest {

    private val id = displayLocale(Locale.forLanguageTag("id"))
    private val en = displayLocale(Locale.ENGLISH)
    private val ar = displayLocale(Locale.forLanguageTag("ar"))

    /** Wednesday 30 Sep 2026, 16:20 WIB. */
    private val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply { clear(); set(2026, 8, 30, 16, 20) }.timeInMillis
    private fun daysAgo(days: Int, hour: Int = 0, minute: Int = 0): Long = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, -days)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.timeInMillis / 1000

    @Test
    fun theColourComesFromTheCodeNotTheTranslatedTitle() {
        assertEquals(StatusTone.Success, activityStyle("attendance", "present").second)
        assertEquals(StatusTone.Danger, activityStyle("attendance", "absent").second)
        assertEquals(StatusTone.Warning, activityStyle("attendance", "sick").second)
        assertEquals(StatusTone.Success, activityStyle("discipline", "reward").second)
        assertEquals(StatusTone.Danger, activityStyle("discipline", "violation").second)
        // Unknown or missing code: never shown as good news.
        assertEquals(StatusTone.Warning, activityStyle("attendance", null).second)
        assertEquals(StatusTone.Danger, activityStyle("discipline", null).second)
    }

    @Test
    fun recentDaysAreNamedAndOlderOnesWrittenInTheAppsLanguage() {
        assertEquals(UiText.Res(R.string.day_today), dayLabel(daysAgo(0), now, en))
        assertEquals(UiText.Res(R.string.day_yesterday), dayLabel(daysAgo(1), now, en))
        assertEquals(UiText.Raw("Senin, 28 Sep"), dayLabel(daysAgo(2), now, id))
        assertEquals(UiText.Raw("Monday, 28 Sep"), dayLabel(daysAgo(2), now, en))
        // Arabic month and day names, Latin digits like the web.
        val arabic = (dayLabel(daysAgo(2), now, ar) as UiText.Raw).value
        assertEquals(true, arabic.contains("28") && arabic.contains("الاثنين،"))
    }

    @Test
    fun eventTimeIsAClockTodayAndADateBefore() {
        assertEquals(UiText.Raw("10:15"), eventTimeLabel(daysAgo(0, 10, 15), now, ar))
        // Recorded per day (midnight): no made-up time.
        assertEquals(UiText.Res(R.string.day_today), eventTimeLabel(daysAgo(0), now, id))
        assertEquals(UiText.Raw("28 Sep"), eventTimeLabel(daysAgo(2, 9), now, en))
    }

    @Test
    fun numbersFollowTheLanguageAndMissingIsADash() {
        assertEquals("96,5", decimal(96.5, id))
        assertEquals("96.5", decimal(96.5, en))
        assertEquals("96.5", decimal(96.5, ar))
        assertEquals("90", decimal(90.0, id))
        assertEquals("–", decimal(null, id))
    }

    @Test
    fun serverDatesAreReadableOrLeftOut() {
        assertEquals("Selasa, 29 Sep 2026", dateLabel("2026-09-29", id))
        assertEquals("Tuesday, 29 Sep 2026", dateLabel("2026-09-29T00:00:00.000000Z", en))
        assertEquals(null, dateLabel("kemarin", id))
        assertEquals(null, dateLabel(null, id))
    }
}
