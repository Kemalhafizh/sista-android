package com.sultanagung1.sista.academic

import com.sultanagung1.sista.data.model.GradeEntry
import com.sultanagung1.sista.data.model.GradeRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeRulesTest {

    private fun grade(id: Long, subject: String, type: String, score: Double, date: String) =
        GradeEntry(id, subject, type, score, null, date)

    private val grades = listOf(
        grade(1, "Matematika", "UH", 80.0, "2026-08-20"),
        grade(2, "Fisika", "UTS", 90.0, "2026-09-10"),
        grade(3, "Matematika", "UTS", 95.0, "2026-09-12"),
        grade(4, "biologi", "uh", 70.0, "2026-09-01"),
    )

    @Test
    fun `grades are grouped per subject A to Z, newest first`() {
        val subjects = GradeRules.bySubject(grades)
        assertEquals(listOf("biologi", "Fisika", "Matematika"), subjects.map { it.subject })
        assertEquals(listOf(3L, 1L), subjects.last().entries.map { it.id })
        assertEquals(87.5, subjects.last().average, 0.001)
    }

    @Test
    fun `types come from the data and filter case-insensitively`() {
        assertEquals(listOf("UH", "UTS"), GradeRules.typesOf(grades))
        assertEquals(listOf(1L, 4L), GradeRules.filter(grades, "UH").map { it.id })
        assertEquals(grades, GradeRules.filter(grades, null))
    }

    @Test
    fun `averages and numbers are shown the Indonesian way`() {
        assertEquals(83.75, GradeRules.overallAverage(grades)!!, 0.001)
        assertNull(GradeRules.overallAverage(emptyList()))
        assertEquals("86,7", GradeRules.formatScore(86.666))
        assertEquals("90", GradeRules.formatScore(90.0))
        assertEquals("14 Sep 2026", GradeRules.formatDate("2026-09-14"))
        assertEquals("kemarin", GradeRules.formatDate("kemarin"))
    }
}
