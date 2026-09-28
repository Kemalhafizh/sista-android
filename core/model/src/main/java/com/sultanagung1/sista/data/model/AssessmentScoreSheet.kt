package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName
import kotlin.math.abs

/** `{id, name}` of a related record; a classroom also carries `students_count`. */
data class NamedRef(
    val id: Long? = null,
    val name: String? = null,
    @SerializedName("students_count") val studentsCount: Int? = null,
)

/** `GET assessments/teacher` → `remedial_dashboard`. */
data class RemedialDashboard(
    @SerializedName("total_remedial_cases") val total: Int = 0,
    @SerializedName("pending_cases") val pending: Int = 0,
    @SerializedName("completed_cases") val completed: Int = 0,
)

/** `GET assessments/{id}/scores`: the assessed class with each stored score. */
data class AssessmentScoreSheet(
    val assessment: AssessmentSheetHeader,
    val students: List<AssessmentScoreRow> = emptyList(),
)

data class AssessmentSheetHeader(
    val id: Long,
    val title: String,
    @SerializedName("assessment_date") val assessmentDate: String? = null,
    val kkm: Double,
    @SerializedName("max_score") val maxScore: Double = ScoreSheetRules.SERVER_MAX,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("classroom_name") val classroomName: String? = null,
)

/** One student; [score] is null until a score has been entered. */
data class AssessmentScoreRow(
    @SerializedName("student_id") val studentId: Long,
    val name: String,
    val nis: String? = null,
    val nisn: String? = null,
    val score: Double? = null,
    val notes: String? = null,
)

/**
 * Rules for the score sheet. Edits are the text typed per student, keyed by
 * student id; a student without an edit keeps the stored score. Only changed,
 * valid scores are sent — a blank is never sent as 0.
 */
object ScoreSheetRules {
    /** `batch-scores` accepts 0..100 whatever the assessment's max score. */
    const val SERVER_MAX = 100.0

    data class Summary(val filled: Int, val total: Int, val belowKkm: Int, val changed: Int, val invalid: Int)

    fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

    fun ceiling(maxScore: Double): Double = if (maxScore > 0) minOf(maxScore, SERVER_MAX) else SERVER_MAX

    /** 80.0 → "80", 82.5 → "82,5". */
    fun format(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else value.toString().replace('.', ',')

    /** What the field shows before it is edited. */
    fun textOf(score: Double?): String = score?.let(::format) ?: ""

    /** Why [text] cannot be saved, or null. The server has no way to clear a stored score. */
    fun errorOf(text: String, maxScore: Double, stored: Double?): String? {
        if (text.isBlank()) return if (stored != null) "Tidak bisa dikosongkan" else null
        val value = parse(text) ?: return "Bukan angka"
        val max = ceiling(maxScore)
        return if (value < 0 || value > max) "0–${format(max)}" else null
    }

    /** The score that would stand after saving: the valid edit, else the stored one. */
    fun effective(row: AssessmentScoreRow, edits: Map<Long, String>, maxScore: Double): Double? {
        val text = edits[row.studentId] ?: return row.score
        return if (errorOf(text, maxScore, row.score) == null && text.isNotBlank()) parse(text) else row.score
    }

    /** Scores to send: edited, valid and different from what is stored. Notes are kept. */
    fun changes(rows: List<AssessmentScoreRow>, edits: Map<Long, String>, maxScore: Double): List<StudentScoreInput> =
        rows.mapNotNull { row ->
            val text = edits[row.studentId] ?: return@mapNotNull null
            if (text.isBlank() || errorOf(text, maxScore, row.score) != null) return@mapNotNull null
            val value = parse(text) ?: return@mapNotNull null
            val stored = row.score
            if (stored != null && abs(value - stored) < 1e-9) null else StudentScoreInput(row.studentId, value, row.notes)
        }

    fun summary(rows: List<AssessmentScoreRow>, edits: Map<Long, String>, kkm: Double, maxScore: Double): Summary {
        val values = rows.map { effective(it, edits, maxScore) }
        return Summary(
            filled = values.count { it != null },
            total = rows.size,
            belowKkm = values.count { it != null && it < kkm },
            changed = changes(rows, edits, maxScore).size,
            invalid = rows.count { row -> edits[row.studentId]?.let { errorOf(it, maxScore, row.score) } != null },
        )
    }
}
