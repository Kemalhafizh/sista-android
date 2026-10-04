package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.teacher.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class JournalFormatTest {

    @Test
    fun journalDatesFollowTheAppLanguageWithLatinDigits() {
        assertEquals("25 Sep", journalDate("2026-09-25", Locale.forLanguageTag("id")))
        assertEquals("25 Sep", journalDate("2026-09-25", Locale.ENGLISH))
        val arabic = journalDate("2026-09-25T00:00:00", Locale.forLanguageTag("ar-u-nu-latn"))
        assertTrue(arabic, arabic.startsWith("25 ") && arabic.none { it in '٠'..'٩' })
    }

    @Test
    fun aDateTheAppCannotReadIsShownAsSent() {
        assertEquals("kemarin", journalDate("kemarin", Locale.ENGLISH))
    }

    @Test
    fun reviewStatusComesFromTheStatusCode() {
        assertEquals(R.string.tj_draft to StatusTone.Warning, journalReviewStatus("draft"))
        assertEquals(R.string.tj_submitted to StatusTone.Info, journalReviewStatus("SUBMITTED"))
        assertEquals(R.string.tj_reviewed to StatusTone.Success, journalReviewStatus("reviewed"))
        assertEquals(R.string.tj_filed to StatusTone.Neutral, journalReviewStatus(null))
    }
}
