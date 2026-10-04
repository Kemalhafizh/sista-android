package com.sultanagung1.sista.academic

import com.sultanagung1.sista.data.model.AssessmentScoreRow
import com.sultanagung1.sista.data.model.ScoreSheetRules
import com.sultanagung1.sista.data.model.StudentScoreInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreSheetRulesTest {

    private val ani = AssessmentScoreRow(1, "Ani", score = 82.5, notes = "Rapi")
    private val budi = AssessmentScoreRow(2, "Budi", score = null)
    private val citra = AssessmentScoreRow(3, "Citra", score = 60.0)
    private val rows = listOf(ani, budi, citra)

    @Test
    fun blankStudentsAreNeverSentAsZero() {
        // The old sheet sent every blank field as 0.0, overwriting real scores.
        assertTrue(ScoreSheetRules.changes(rows, emptyMap(), 100.0).isEmpty())
        assertTrue(ScoreSheetRules.changes(rows, mapOf(2L to ""), 100.0).isEmpty())
    }

    @Test
    fun onlyChangedValidScoresAreSentWithTheirNotesKept() {
        val edits = mapOf(1L to "90", 2L to "77,5", 3L to "60")
        assertEquals(
            listOf(StudentScoreInput(1, 90.0, "Rapi"), StudentScoreInput(2, 77.5, null)),
            ScoreSheetRules.changes(rows, edits, 100.0),
        )
    }

    @Test
    fun outOfRangeAndNonNumbersAreRejected() {
        assertEquals(ScoreSheetRules.ScoreError.OUT_OF_RANGE, ScoreSheetRules.errorOf("101", 100.0, null))
        assertEquals(ScoreSheetRules.ScoreError.OUT_OF_RANGE, ScoreSheetRules.errorOf("51", 50.0, null))
        assertEquals(ScoreSheetRules.ScoreError.NOT_A_NUMBER, ScoreSheetRules.errorOf("8,5,1", 100.0, null))
        assertNull(ScoreSheetRules.errorOf("50", 50.0, null))
        assertTrue(ScoreSheetRules.changes(rows, mapOf(2L to "150"), 100.0).isEmpty())
    }

    @Test
    fun theServerCeilingOf100WinsOverALargerMaxScore() {
        assertEquals(100.0, ScoreSheetRules.ceiling(150.0), 0.0)
        assertEquals(ScoreSheetRules.ScoreError.OUT_OF_RANGE, ScoreSheetRules.errorOf("120", 150.0, null))
    }

    @Test
    fun aStoredScoreCannotBeClearedBecauseTheServerCannotDeleteIt() {
        assertEquals(ScoreSheetRules.ScoreError.CANNOT_CLEAR, ScoreSheetRules.errorOf("", 100.0, 82.5))
        assertNull(ScoreSheetRules.errorOf("", 100.0, null))
    }

    @Test
    fun summaryCountsWhatWouldStandAfterSaving() {
        val summary = ScoreSheetRules.summary(rows, mapOf(2L to "70", 3L to "abc"), kkm = 75.0, maxScore = 100.0)
        assertEquals(3, summary.filled)
        assertEquals(3, summary.total)
        // Budi's 70 and Citra's stored 60 (her invalid edit does not count).
        assertEquals(2, summary.belowKkm)
        assertEquals(1, summary.changed)
        assertEquals(1, summary.invalid)
    }

    @Test
    fun scoresUseTheDecimalSeparatorOfTheAppLanguage() {
        assertEquals("80", ScoreSheetRules.format(80.0))
        assertEquals("82,5", ScoreSheetRules.format(82.5))
        assertEquals("82.5", ScoreSheetRules.format(82.5, '.'))
        assertEquals("82.5", ScoreSheetRules.textOf(82.5, '.'))
        assertEquals("", ScoreSheetRules.textOf(null))
        assertEquals(82.5, ScoreSheetRules.parse(" 82,5 ")!!, 0.0)
    }
}
