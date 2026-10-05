package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.teacher.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ProctorFormatTest {

    @Test
    fun examTypesAreWordedByCodeAndUnknownTypesShownAsSent() {
        assertEquals(UiText.Res(R.string.pe_type_uts), ProctorFormat.examType("uts"))
        assertEquals(UiText.Res(R.string.pe_type_daily), ProctorFormat.examType("ulangan_harian"))
        assertEquals(UiText.Raw("kuis"), ProctorFormat.examType("kuis"))
    }

    @Test
    fun examTimeIsInTheSchoolTimeZoneAndTheAppLanguage() {
        // 00:30 UTC is 07:30 in Asia/Jakarta.
        assertEquals("Sen, 5 Okt 2026 · 07.30".replace(".", ":"), ProctorFormat.examTime("2026-10-05T00:30:00Z", Locale.forLanguageTag("id")))
        assertEquals("Mon, 5 Oct 2026 · 07:30", ProctorFormat.examTime("2026-10-05T07:30:00+07:00", Locale.ENGLISH))
        val arabic = ProctorFormat.examTime("2026-10-05T07:30:00+07:00", Locale.forLanguageTag("ar-u-nu-latn"))!!
        assertTrue(arabic, arabic.contains("07:30") && arabic.none { it in '٠'..'٩' })
    }

    @Test
    fun anUnreadableOrMissingTimeIsNull() {
        assertNull(ProctorFormat.examTime(null))
        assertNull(ProctorFormat.examTime("besok pagi"))
    }

    @Test
    fun countdownUsesLatinDigitsAndNeverGoesNegative() {
        assertEquals("2:05", ProctorFormat.countdown(125))
        assertEquals("0:00", ProctorFormat.countdown(-3))
    }

    @Test
    fun tokenToneFollowsTimeLeft() {
        assertEquals(StatusTone.Danger, ProctorFormat.tokenTone(expired = true, remainingSeconds = 200))
        assertEquals(StatusTone.Danger, ProctorFormat.tokenTone(expired = false, remainingSeconds = 30))
        assertEquals(StatusTone.Warning, ProctorFormat.tokenTone(expired = false, remainingSeconds = 90))
        assertEquals(StatusTone.Success, ProctorFormat.tokenTone(expired = false, remainingSeconds = 240))
        // No countdown from the server: not claimed as fresh or about to expire.
        assertEquals(StatusTone.Brand, ProctorFormat.tokenTone(expired = false, remainingSeconds = null))
    }
}
