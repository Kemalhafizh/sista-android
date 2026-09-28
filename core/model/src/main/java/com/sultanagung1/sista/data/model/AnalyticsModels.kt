package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

// analytics/student/summary, analytics/teacher/*, analytics/parent/child-progress.
// A figure is null when nothing was recorded, never 0.

data class RadarAxisPoint(
    val label: String = "",
    val value: Float = 0f,
    val targetKktp: Float = 75f,
)

data class SemesterTrendPoint(
    val semesterName: String = "",
    val gpaScore: Float = 0f,
    val rankInClass: Int? = null,
    val totalStudents: Int? = null,
)

/** One subject graded this year: the student's average against its KKM. */
data class SubjectAverage(
    val name: String = "",
    val average: Double = 0.0,
    val kkm: Double = 75.0,
    val gradesCount: Int = 0,
)

data class StudentAnalyticsData(
    val studentName: String = "",
    val className: String = "",
    val overallAverage: Double? = null,
    val attendanceRate: Double? = null,
    val attendanceRecorded: Int? = null,
    val semesterTrends: List<SemesterTrendPoint> = emptyList(),
    /** At most six; [subjects] has them all on a current server. */
    val competencyRadar: List<RadarAxisPoint> = emptyList(),
    val subjects: List<SubjectAverage>? = null,
)

data class ScoreDistributionBucket(
    val rangeLabel: String = "",
    val count: Int = 0,
    val percentage: Float = 0f,
)

data class AtRiskStudentItem(
    val studentId: String = "",
    val studentName: String = "",
    val lowestSubject: String = "",
    val currentScore: Int = 0,
    val kktpThreshold: Int = 75,
)

data class ClassAnalyticsData(
    val classroomId: Long? = null,
    val subjectId: Long? = null,
    val className: String = "",
    val subjectName: String = "",
    val teacherName: String = "",
    val classAverage: Double? = null,
    val highestScore: Int? = null,
    val lowestScore: Int? = null,
    val passRatePercentage: Double? = null,
    val kkm: Double? = null,
    val scoresCount: Int? = null,
    val studentsGraded: Int? = null,
    val studentsCount: Int? = null,
    val distributionBuckets: List<ScoreDistributionBucket> = emptyList(),
    /** Lowest score first. */
    val atRiskStudents: List<AtRiskStudentItem> = emptyList(),
)

data class DailyAttendanceHeatmapItem(
    val dayNumber: Int = 0,
    val dateFormatted: String = "",
    /** HADIR, IZIN, SAKIT, ALPA; KOSONG (or LIBUR from an older server) when nothing was recorded. */
    val status: String = "",
)

data class ParentProgressData(
    val childName: String = "",
    val childClass: String = "",
    val academicScore: Double? = null,
    val classAverageScore: Double? = null,
    /** Both null when the child has no hafalan target this year. */
    val tahfidzCurrentJuz: Int? = null,
    val tahfidzTargetJuz: Int? = null,
    val totalSurahCompleted: Int = 0,
    val attendanceHeatmap: List<DailyAttendanceHeatmapItem> = emptyList(),
)

/**
 * analytics/executive/kpi. Raw figures that the screen formats itself; a
 * rate is null when there was nothing to divide by, never 0.
 */
data class ExecutiveAnalyticsData(
    val totalActiveStudents: Int = 0,
    val attendance: ExecutiveAttendance? = null,
    val spp: ExecutiveSpp? = null,
)

data class ExecutiveAttendance(
    val today: AttendanceBreakdown? = null,
    /** Days with records in the last 14, oldest first; days without records are absent, not 0%. */
    val days: List<AttendanceDay> = emptyList(),
)

/** Attendance records by status: H, S, I, A. */
data class AttendanceBreakdown(
    val recorded: Int = 0,
    val present: Int = 0,
    val sick: Int = 0,
    val permit: Int = 0,
    val absent: Int = 0,
    val rate: Double? = null,
)

data class AttendanceDay(
    val date: String = "",
    val recorded: Int = 0,
    val present: Int = 0,
    val rate: Double = 0.0,
)

/** This month's SPP: how much of the billed amount is settled, and what came in. */
data class ExecutiveSpp(
    val month: String = "",
    val billed: Double = 0.0,
    val settled: Double = 0.0,
    @SerializedName("settled_rate") val settledRate: Double? = null,
    val bills: Int = 0,
    @SerializedName("bills_paid") val billsPaid: Int = 0,
    @SerializedName("received_this_month") val receivedThisMonth: Double = 0.0,
    @SerializedName("received_last_month") val receivedLastMonth: Double = 0.0,
    @SerializedName("received_change_pct") val receivedChangePct: Double? = null,
)

/**
 * A (class, subject) the teacher teaches this year, from
 * analytics/teacher/my-classes. The ids are null on an older server, which
 * sent names only.
 */
data class TeacherClassOption(
    val classroomId: Long? = null,
    val subjectId: Long? = null,
    val className: String = "",
    val subjectName: String = "",
    val academicYear: String? = null,
)
