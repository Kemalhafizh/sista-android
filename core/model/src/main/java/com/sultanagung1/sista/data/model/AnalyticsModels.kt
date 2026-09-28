package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class RadarAxisPoint(
    val label: String,
    val value: Float, // 0.0 to 100.0
    val targetKktp: Float = 75.0f
)

data class SemesterTrendPoint(
    val semesterName: String,
    val gpaScore: Float, // e.g. 88.5
    val rankInClass: Int,
    val totalStudents: Int = 36
)

data class StudentAnalyticsData(
    val studentName: String = "",
    val className: String = "",
    val overallAverage: Float = 0f,
    val attendanceRate: Float = 0f,
    val semesterTrends: List<SemesterTrendPoint> = emptyList(),
    val competencyRadar: List<RadarAxisPoint> = emptyList(),
    val topSubjects: List<Pair<String, Float>> = emptyList()
)

data class ScoreDistributionBucket(
    val rangeLabel: String,
    val count: Int,
    val percentage: Float
)

data class AtRiskStudentItem(
    val studentId: String,
    val studentName: String,
    val lowestSubject: String,
    val currentScore: Int,
    val kktpThreshold: Int = 75,
    val recommendation: String
)

data class ClassAnalyticsData(
    val className: String = "",
    val subjectName: String = "",
    val teacherName: String = "",
    val classAverage: Float = 0f,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val passRatePercentage: Float = 0f,
    val distributionBuckets: List<ScoreDistributionBucket> = emptyList(),
    val atRiskStudents: List<AtRiskStudentItem> = emptyList()
)

data class DailyAttendanceHeatmapItem(
    val dayNumber: Int,
    val dateFormatted: String,
    val status: String // "HADIR", "IZIN", "SAKIT", "TERLAMBAT", "LIBUR"
)

data class ParentProgressData(
    val childName: String = "",
    val childClass: String = "",
    val academicScore: Float = 0f,
    val classAverageScore: Float = 0f,
    val tahfidzCurrentJuz: Int = 0,
    val tahfidzTargetJuz: Int = 30,
    val totalSurahCompleted: Int = 0,
    val attendanceHeatmap: List<DailyAttendanceHeatmapItem> = emptyList()
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

// A teacher's real (class, subject) teaching assignment, as returned by
// analytics/teacher/my-classes — used to replace a hardcoded class/subject
// pair with the teacher's actual options.
data class TeacherClassOption(
    val className: String = "",
    val subjectName: String = ""
)
