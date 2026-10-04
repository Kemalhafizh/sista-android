package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Teacher / Guru role — matches ApiTeacherController (role:guru,bk)
 * exactly. "Today's schedule" and "recent journals" are NOT provided by these
 * models: they're composed from the already-real TeacherScheduleSlot /
 * TeachingJournalEntry (TeachingJournalMobileModels.kt) shared with the
 * KBM journal feature, instead of duplicating a second copy of the same data.
 */

/** GET teacher/classes — real classroom roster the teacher is assigned to. */
data class TeacherClassSummary(
    val id: Long,
    val name: String,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    val major: String? = null,
    @SerializedName("is_homeroom") val isHomeroom: Boolean = false,
    @SerializedName("students_count") val studentsCount: Int
)

/** GET teacher/classes/{id}/students — real roster row (Student::select id,name,nis,nisn,gender). */
data class TeacherClassStudent(
    val id: Long,
    val name: String,
    val nis: String? = null,
    val nisn: String? = null,
    val gender: String = "L"
)

/**
 * One student on the class attendance sheet with the teacher's selection,
 * kept as the code `teacher/attendance` stores (H, I, S, A). The screen words
 * the code in the app's language; it used to keep the Indonesian label and
 * translate it to a code only when sending.
 */
data class StudentAttendanceInputItem(
    val studentId: Long,
    val name: String,
    val nis: String? = null,
    val nisn: String? = null,
    val status: String = "H",
)

data class AttendanceStudentStatus(
    @SerializedName("student_id") val studentId: Long,
    val status: String, // H, S, I, A
    val notes: String? = null
)

/** POST teacher/attendance body. */
data class SubmitClassAttendanceRequest(
    @SerializedName("classroom_id") val classroomId: Long,
    val date: String,
    @SerializedName("schedule_id") val scheduleId: Long? = null,
    val students: List<AttendanceStudentStatus>
)

data class AttendanceRecordedResponse(
    @SerializedName("recorded_count") val recordedCount: Int
)
