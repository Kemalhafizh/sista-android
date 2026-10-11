package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.data.model.RemedialItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class StudentRemedialRulesTest {

    private val today = LocalDate.of(2026, 10, 11)
    private val id = Locale.forLanguageTag("id-ID")

    private fun remedial(id: Long, status: String?, deadline: String? = null) =
        RemedialItem(id = id, originalScore = 60.0, kkm = 75.0, status = status, deadline = deadline)

    @Test
    fun statusFollowsTheServersCodes() {
        assertEquals(StudentRemedialRules.Status.DONE, StudentRemedialRules.statusOf(remedial(1, "completed")))
        assertEquals(StudentRemedialRules.Status.PENDING, StudentRemedialRules.statusOf(remedial(1, " Pending ")))
        assertEquals(StudentRemedialRules.Status.OTHER, StudentRemedialRules.statusOf(remedial(1, "in_review")))
        assertEquals(StudentRemedialRules.Status.OTHER, StudentRemedialRules.statusOf(remedial(1, null)))
    }

    @Test
    fun aMissingScoreOrKkmStaysUnknown() {
        assertNull(StudentRemedialRules.score(null, id))
        assertEquals("60", StudentRemedialRules.score(60.0, id))
        assertEquals("62,5", StudentRemedialRules.score(62.5, id))
        assertEquals("62.5", StudentRemedialRules.score(62.5, Locale.ENGLISH))
    }

    @Test
    fun theDeadlineIsReadInSchoolTime() {
        // 18:00 UTC on the 19th is already the 20th in Jakarta.
        assertEquals(LocalDate.of(2026, 10, 20), StudentRemedialRules.deadlineDate("2026-10-19T18:00:00.000000Z"))
        assertEquals(LocalDate.of(2026, 10, 20), StudentRemedialRules.deadlineDate("2026-10-20"))
        assertNull(StudentRemedialRules.deadlineDate("minggu depan"))
        assertNull(StudentRemedialRules.deadlineDate(null))
        assertEquals("20 Okt 2026", StudentRemedialRules.deadlineText("2026-10-20", id))
    }

    @Test
    fun onlyUnfinishedWorkPastItsDeadlineIsOverdue() {
        assertTrue(StudentRemedialRules.isOverdue(remedial(1, "pending", "2026-10-10"), today))
        assertFalse(StudentRemedialRules.isOverdue(remedial(1, "pending", "2026-10-11"), today))
        assertFalse(StudentRemedialRules.isOverdue(remedial(1, "completed", "2026-10-01"), today))
        assertFalse(StudentRemedialRules.isOverdue(remedial(1, "pending"), today))
    }

    @Test
    fun workStillToDoComesFirstBySoonestDeadline() {
        val ordered = StudentRemedialRules.ordered(
            listOf(
                remedial(1, "completed", "2026-10-01"),
                remedial(2, "pending"),
                remedial(3, "pending", "2026-10-20"),
                remedial(4, "pending", "2026-10-14"),
            )
        )
        assertEquals(listOf(4L, 3L, 2L, 1L), ordered.map { it.id })
    }
}
