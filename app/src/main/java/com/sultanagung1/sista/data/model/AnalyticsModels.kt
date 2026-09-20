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
    val studentName: String = "Ahmad Kemal Hafizh",
    val className: String = "XII MIPA 1",
    val overallAverage: Float = 91.2f,
    val attendanceRate: Float = 98.5f,
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
    val className: String = "XII MIPA 1",
    val subjectName: String = "Fisika Modern",
    val teacherName: String = "Dr. Hj. Siti Nurjanah, M.Si",
    val classAverage: Float = 86.4f,
    val highestScore: Int = 98,
    val lowestScore: Int = 68,
    val passRatePercentage: Float = 94.4f,
    val distributionBuckets: List<ScoreDistributionBucket> = emptyList(),
    val atRiskStudents: List<AtRiskStudentItem> = emptyList()
)

data class DailyAttendanceHeatmapItem(
    val dayNumber: Int,
    val dateFormatted: String,
    val status: String // "HADIR", "IZIN", "SAKIT", "TERLAMBAT", "LIBUR"
)

data class ParentProgressData(
    val childName: String = "Ahmad Kemal Hafizh",
    val childClass: String = "XII MIPA 1",
    val academicScore: Float = 91.2f,
    val classAverageScore: Float = 84.5f,
    val tahfidzCurrentJuz: Int = 30,
    val tahfidzTargetJuz: Int = 30,
    val totalSurahCompleted: Int = 37,
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
    val totalActiveStudents: Int = 1080,
    val attendanceTodayPercentage: Float = 97.8f,
    val totalSppCollected: String = "Rp 918.000.000",
    val collectionRatePercentage: Float = 92.5f
)
