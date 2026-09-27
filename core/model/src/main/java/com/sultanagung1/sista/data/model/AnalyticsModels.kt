package com.sultanagung1.sista.data.model

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

data class ExecutiveKpiItem(
    val title: String,
    val value: String,
    val changeText: String,
    val isPositiveTrend: Boolean,
    val progress: Float,
    val description: String
)

data class ExecutiveAnalyticsData(
    val kpiList: List<ExecutiveKpiItem> = emptyList(),
    val totalActiveStudents: Int = 0,
    val attendanceTodayPercentage: Float = 0f,
    val totalSppCollected: String = "",
    val collectionRatePercentage: Float = 0f
)
