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
