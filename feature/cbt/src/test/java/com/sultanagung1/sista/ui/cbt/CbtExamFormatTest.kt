package com.sultanagung1.sista.ui.cbt

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.CbtExamItem
import com.sultanagung1.sista.feature.cbt.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CbtExamFormatTest {

    private fun exam(code: String? = "ongoing", attempt: String? = null, ongoing: Boolean = code == "ongoing") =
        CbtExamItem(id = 1, title = "Ulangan Fisika", availabilityCode = code, attemptStatus = attempt, isOngoing = ongoing)

    @Test
    fun availabilityFollowsTheServerCodes() {
        assertEquals(ExamAvailability.ONGOING, CbtExamFormat.availabilityOf(exam("ongoing")))
        assertEquals(ExamAvailability.IN_PROGRESS, CbtExamFormat.availabilityOf(exam("ongoing", "in_progress")))
        assertEquals(ExamAvailability.UPCOMING, CbtExamFormat.availabilityOf(exam("upcoming")))
        assertEquals(ExamAvailability.ENDED, CbtExamFormat.availabilityOf(exam("ended")))
    }

    @Test
    fun aFinishedAttemptWinsOverAnOpenExam() {
        listOf("submitted", "graded", "timeout").forEach {
            assertEquals(it, ExamAvailability.DONE, CbtExamFormat.availabilityOf(exam("ongoing", it)))
        }
        assertEquals(ExamAvailability.FORCE_CLOSED, CbtExamFormat.availabilityOf(exam("ongoing", "force_closed")))
        assertEquals(ExamAvailability.BLOCKED, CbtExamFormat.availabilityOf(exam("ongoing", "blocked_violation")))
    }

    @Test
    fun onlyAnOpenExamCanBeEntered() {
        ExamAvailability.entries.forEach {
            assertEquals(it.name, it == ExamAvailability.ONGOING || it == ExamAvailability.IN_PROGRESS, it.canEnter)
        }
    }

    @Test
    fun anOlderServerWithoutCodesIsNotGuessedAsUpcoming() {
        assertEquals(ExamAvailability.ONGOING, CbtExamFormat.availabilityOf(exam(code = null, ongoing = true)))
        assertEquals(ExamAvailability.CLOSED, CbtExamFormat.availabilityOf(exam(code = null, ongoing = false)))
    }

    @Test
    fun examTypeIsTranslatedAndUnknownTypesShownAsSent() {
        assertEquals(UiText.Res(R.string.cbt_type_uts), CbtExamFormat.examType("UTS"))
        assertEquals(UiText.Res(R.string.cbt_type_daily), CbtExamFormat.examType("ULANGAN_HARIAN"))
        assertEquals(UiText.Raw("PAS"), CbtExamFormat.examType("PAS"))
        assertNull(CbtExamFormat.examType(null))
    }

    @Test
    fun clockUsesLatinDigitsAndADashWhenUnknown() {
        assertEquals("01:05:09", CbtExamFormat.clock(3909))
        assertEquals("00:00:00", CbtExamFormat.clock(-4))
        assertEquals("–", CbtExamFormat.clock(null))
    }

    @Test
    fun clockToneWarnsNearTheEnd() {
        assertEquals(StatusTone.Neutral, CbtExamFormat.clockTone(null))
        assertEquals(StatusTone.Brand, CbtExamFormat.clockTone(301))
        assertEquals(StatusTone.Warning, CbtExamFormat.clockTone(300))
        assertEquals(StatusTone.Danger, CbtExamFormat.clockTone(60))
    }

    @Test
    fun scheduleIsInTheSchoolTimeZone() {
        val id = Locale.forLanguageTag("id")
        // 00:30 UTC is 07:30 in Jakarta.
        assertEquals("Sen, 5 Okt · 07:30–09:00", CbtExamFormat.schedule("2026-10-05T00:30:00Z", "2026-10-05T09:00:00+07:00", id))
        assertEquals("Mon, 5 Oct · 07:30", CbtExamFormat.schedule("2026-10-05T07:30:00+07:00", null, Locale.ENGLISH))
        assertTrue(CbtExamFormat.schedule("2026-10-05T07:30:00+07:00", "2026-10-06T07:30:00+07:00", Locale.ENGLISH)!!.contains(" – "))
        assertNull(CbtExamFormat.schedule(null, "2026-10-05T09:00:00+07:00", id))
        assertNull(CbtExamFormat.schedule("bukan tanggal", null, id))
    }

    @Test
    fun answeredCountsOnlyQuestionsOnScreen() {
        val answers = mapOf("1" to "A", "2" to "", "9" to "C")
        assertEquals(1, CbtExamFormat.answeredCount(listOf(1L, 2L, 3L), answers))
    }

    @Test
    fun tokenIsUpperCasedAndCapped() {
        assertEquals("K7X4M2", CbtExamFormat.cleanToken("k7x-4m 2zz"))
        assertFalse(CbtExamFormat.cleanToken("ab").length == CbtExamFormat.TOKEN_LENGTH)
    }

    @Test
    fun timeSpentIsNullRatherThanGuessed() {
        assertEquals(600L, CbtViewModel.timeSpent(90, 90 * 60L - 600))
        assertEquals(0L, CbtViewModel.timeSpent(60, 4000))
        assertNull(CbtViewModel.timeSpent(null, 120))
        assertNull(CbtViewModel.timeSpent(90, null))
    }
}
