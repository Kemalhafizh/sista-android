package com.sultanagung1.sista.data.model

import java.util.Locale

/**
 * Pure rules behind the student's grade list (GradesScreen): grouped per
 * subject from `student/grades`, newest first. Averages are plain means of
 * what teachers entered; no pass mark is assumed because the minimum
 * (KKTP) differs per subject and the API does not send it.
 */
object GradeRules {

    data class SubjectGrades(val subject: String, val entries: List<GradeEntry>) {
        val average: Double get() = entries.map { it.score }.average()
    }

    /** The assessment types present, in the order they first appear ("UH", "UTS", …). */
    fun typesOf(grades: List<GradeEntry>): List<String> =
        grades.map { it.type.trim().uppercase(Locale.ROOT) }.filter { it.isNotEmpty() }.distinct()

    /** [grades] of one type, or all of them when [type] is null. */
    fun filter(grades: List<GradeEntry>, type: String?): List<GradeEntry> =
        if (type == null) grades else grades.filter { it.type.trim().equals(type, ignoreCase = true) }

    /** Per subject (A–Z), each subject's grades newest first. */
    fun bySubject(grades: List<GradeEntry>): List<SubjectGrades> =
        grades.groupBy { it.subject.trim() }
            .map { (subject, entries) -> SubjectGrades(subject, entries.sortedByDescending { it.date }) }
            .sortedBy { it.subject.lowercase(Locale.ROOT) }

    /** Mean of every grade shown, or null when there is none. */
    fun overallAverage(grades: List<GradeEntry>): Double? =
        grades.takeIf { it.isNotEmpty() }?.map { it.score }?.average()

    /** 86.666 → "86,7"; 90.0 → "90". Indonesian decimal comma. */
    fun formatScore(score: Double): String {
        val rounded = Math.round(score * 10) / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString()
        else String.format(Locale.forLanguageTag("id-ID"), "%.1f", rounded)
    }

    private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

    /** "2026-09-14" → "14 Sep 2026"; anything else is shown as sent. */
    fun formatDate(date: String): String {
        val parts = date.take(10).split("-")
        val month = parts.getOrNull(1)?.toIntOrNull()
        val day = parts.getOrNull(2)?.toIntOrNull()
        return if (parts.size == 3 && month in 1..12 && day != null) "$day ${MONTHS[month!! - 1]} ${parts[0]}" else date
    }
}
