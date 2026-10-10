package com.sultanagung1.sista.ui.profile

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.feature.profile.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class StudentProfileFormatTest {

    private val id = Locale.forLanguageTag("id-ID")
    private val en = Locale.forLanguageTag("en-US")
    private val ar = displayLocale(Locale.forLanguageTag("ar"))

    @Test
    fun numbersFollowTheAppLanguageAndDashWhenMissing() {
        assertEquals("80,5", idNumber(80.5, id))
        assertEquals("80", idNumber(80.0, id))
        assertEquals("1.250", idNumber(1250.0, id))
        assertEquals("80.5", idNumber(80.5, en))
        assertEquals("1,250", idNumber(1250.0, en))
        assertEquals(MISSING, idNumber(null, id))
    }

    @Test
    fun arabicKeepsLatinDigitsLikeTheWeb() {
        assertEquals("ar", ar.language)
        assertTrue(idNumber(1250.0, ar).filter { it.isDigit() }.all { it in '0'..'9' })
    }

    @Test
    fun rank() {
        assertEquals(UiText.Res(R.string.p360_rank_full, 2, 30), rankLabel(2, 30))
        assertEquals(UiText.Raw("2"), rankLabel(2, null))
        assertEquals(UiText.Raw(MISSING), rankLabel(null, 30))
    }

    @Test
    fun noTahfidzTargetIsNotZeroOfZero() {
        assertNull(juzLabel(null, null))
        assertNull(juzLabel(3, null))
        assertEquals(UiText.Res(R.string.p360_juz, 1, 4), juzLabel(1, 4))
    }

    @Test
    fun streakIsInDaysNotWeeks() {
        assertEquals(UiText.Res(R.string.p360_days, 3), streakLabel(3))
        assertEquals(UiText.Raw(MISSING), streakLabel(null))
    }

    @Test
    fun percentBodyPointsAndDate() {
        assertEquals("66,7%", percentLabel(66.7, id))
        assertEquals(MISSING, percentLabel(null, id))
        assertEquals(listOf(UiText.Res(R.string.unit_cm, "165"), UiText.Res(R.string.unit_kg, "52,5")), bodyLabel(165.0, 52.5, id))
        assertEquals(listOf(UiText.Res(R.string.unit_cm, "165")), bodyLabel(165.0, null, id))
        assertEquals(listOf(UiText.Raw(MISSING)), bodyLabel(null, null, id))
        assertEquals("+10", signedPoints(10, "+"))
        assertEquals("0", signedPoints(0, "+"))
        assertEquals(MISSING, signedPoints(null, "+"))
        assertEquals("12 September 2026", dateLabel("2026-09-12", id))
        assertEquals("September 12, 2026", dateLabel("2026-09-12", en))
        assertEquals(MISSING, dateLabel("bukan tanggal", id))
        assertEquals(MISSING, dateLabel(null, id))
    }

    @Test
    fun disciplineColourComesFromTheCodeNotTheTranslatedLabel() {
        assertEquals(com.sultanagung1.sista.core.ui.theme.StatusTone.Success, disciplineTone("aman"))
        assertEquals(com.sultanagung1.sista.core.ui.theme.StatusTone.Warning, disciplineTone("sp1"))
        assertEquals(com.sultanagung1.sista.core.ui.theme.StatusTone.Danger, disciplineTone("sp3"))
        assertEquals(com.sultanagung1.sista.core.ui.theme.StatusTone.Neutral, disciplineTone(null))
    }
}
