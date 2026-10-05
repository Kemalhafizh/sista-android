package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.teacher.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuestionBankFormatTest {

    @Test
    fun difficultyIsWordedByCode() {
        assertEquals(UiText.Res(R.string.qb_easy), QuestionBankFormat.difficulty("mudah"))
        assertEquals(UiText.Res(R.string.qb_hard), QuestionBankFormat.difficulty("sulit"))
        // An item without a level is not passed off as "sedang".
        assertEquals(UiText.Res(R.string.qb_no_difficulty), QuestionBankFormat.difficulty(null))
        assertEquals(UiText.Raw("ekstrem"), QuestionBankFormat.difficulty("ekstrem"))
        assertEquals(StatusTone.Neutral, QuestionBankFormat.difficultyTone(null))
    }

    @Test
    fun everyMixAddsUpToAHundredPercent() {
        QuestionBankFormat.MIXES.forEach { mix ->
            assertEquals(QuestionBankFormat.DIFFICULTIES.toSet(), mix.keys)
            assertEquals(100, mix.values.sum())
        }
        assertEquals("30 / 50 / 20", QuestionBankFormat.mixLabel(QuestionBankFormat.MIXES[0]))
    }

    @Test
    fun theCountMustBeOneToAHundred() {
        assertEquals(10, QuestionPickForm(count = "10").countValue)
        assertNull(QuestionPickForm(count = "0").countValue)
        assertNull(QuestionPickForm(count = "101").countValue)
        assertNull(QuestionPickForm(count = "").countValue)
    }
}
