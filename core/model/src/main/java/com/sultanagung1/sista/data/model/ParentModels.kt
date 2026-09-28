package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Parent / Wali Murid role — matches ApiParentController
 * (routes/api.php prefix('parent'), role:parent) exactly. No field here has
 * a hardcoded default: every value not provided by the backend is nullable
 * and the UI shows an honest "belum tersedia" instead of a fabricated number.
 */

/** GET parent/children — real StudentResource row. */
data class ParentChildItem(
    val uuid: String,
    val name: String,
    val nis: String? = null,
    val nisn: String? = null,
    val classroom: String? = null,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    val major: String? = null,
    val gender: String? = null,
    val status: String? = null,
    @SerializedName("total_points") val totalPoints: Int = 0
)

/** GET parent/child/{uuid}/summary — nested `student` object. */
data class ChildSummaryStudent(
    val uuid: String,
    val name: String,
    val classroom: String? = null,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    @SerializedName("homeroom_teacher") val homeroomTeacher: String? = null
)

/**
 * GET parent/child/{uuid}/summary — nested `statistics`, this academic year.
 * Average and attendance are null when nothing has been recorded yet.
 */
data class ChildSummaryStatistics(
    @SerializedName("average_grade") val averageGrade: Double? = null,
    @SerializedName("attendance_rate") val attendanceRate: Double? = null,
    @SerializedName("unpaid_billings_count") val unpaidBillingsCount: Int,
    @SerializedName("total_bk_points") val totalBkPoints: Int
)

data class ChildSummaryResponse(
    val student: ChildSummaryStudent,
    val statistics: ChildSummaryStatistics
)

/** GET parent/child/{uuid}/attendance — real AttendanceResource row (latest 30). */
data class ChildAttendanceLog(
    val id: Long,
    val date: String,
    val status: String, // H, S, I, A
    @SerializedName("status_label") val statusLabel: String,
    val notes: String? = null,
    @SerializedName("recorded_by") val recordedBy: String? = null
)

/** GET parent/child/{uuid}/grades — real GradeResource row. */
data class ChildGradeItem(
    val id: Long,
    val subject: String,
    val type: String,
    val score: Double,
    val description: String? = null,
    val date: String
)
