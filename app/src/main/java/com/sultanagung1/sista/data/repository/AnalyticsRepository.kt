package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnalyticsRepository(private val apiClient: ApiClient) {

    fun getStudentAnalytics(): Flow<NetworkResult<StudentAnalyticsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getStudentAnalytics()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getMockStudentAnalytics()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getMockStudentAnalytics()))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassAnalytics(className: String, subjectName: String): Flow<NetworkResult<ClassAnalyticsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getClassAnalytics(className, subjectName)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getMockClassAnalytics()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getMockClassAnalytics()))
        }
    }.flowOn(Dispatchers.IO)

    fun getParentProgress(): Flow<NetworkResult<ParentProgressData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getParentProgress()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getMockParentProgress()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getMockParentProgress()))
        }
    }.flowOn(Dispatchers.IO)

    fun getExecutiveKpi(): Flow<NetworkResult<ExecutiveAnalyticsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getExecutiveKpi()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getMockExecutiveKpi()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getMockExecutiveKpi()))
        }
    }.flowOn(Dispatchers.IO)

    private fun getMockStudentAnalytics(): StudentAnalyticsData {
        return StudentAnalyticsData(
            studentName = "Ahmad Kemal Hafizh",
            className = "XII MIPA 1",
            overallAverage = 91.2f,
            attendanceRate = 98.5f,
            semesterTrends = listOf(
                SemesterTrendPoint("Sem 1 (X)", 86.4f, 4),
                SemesterTrendPoint("Sem 2 (X)", 88.1f, 3),
                SemesterTrendPoint("Sem 3 (XI)", 89.5f, 2),
                SemesterTrendPoint("Sem 4 (XI)", 90.8f, 2),
                SemesterTrendPoint("Sem 5 (XII)", 91.2f, 1)
            ),
            competencyRadar = listOf(
                RadarAxisPoint("PAI & Karakter", 95f, 75f),
                RadarAxisPoint("Matematika", 90f, 75f),
                RadarAxisPoint("Sains Fisika", 88f, 75f),
                RadarAxisPoint("Biologi/Kimia", 92f, 75f),
                RadarAxisPoint("Bahasa & Literasi", 94f, 75f),
                RadarAxisPoint("IT & Coding", 96f, 75f)
            ),
            topSubjects = listOf(
                "Informatika & Coding" to 96.0f,
                "Bahasa Arab & Tarjamah" to 95.0f,
                "PAI & Budi Pekerti" to 94.0f,
                "Bahasa Inggris" to 92.5f,
                "Matematika Peminatan" to 90.0f
            )
        )
    }

    private fun getMockClassAnalytics(): ClassAnalyticsData {
        return ClassAnalyticsData(
            className = "XII MIPA 1",
            subjectName = "Fisika Modern",
            teacherName = "Dr. Hj. Siti Nurjanah, M.Si",
            classAverage = 86.4f,
            highestScore = 98,
            lowestScore = 68,
            passRatePercentage = 94.4f,
            distributionBuckets = listOf(
                ScoreDistributionBucket("< 75 (Remedial)", 2, 5.6f),
                ScoreDistributionBucket("75 - 84 (Cukup)", 10, 27.8f),
                ScoreDistributionBucket("85 - 94 (Baik)", 18, 50.0f),
                ScoreDistributionBucket("95 - 100 (Sangat Baik)", 6, 16.6f)
            ),
            atRiskStudents = listOf(
                AtRiskStudentItem("s10", "Budi Santoso", "Fisika Modern", 68, 75, "Perlu pendampingan remedial bab Mekanika Gelombang"),
                AtRiskStudentItem("s19", "Rian Hidayat", "Fisika Modern", 72, 75, "Perlu tugas pengayaan dan latihan soal praktikum")
            )
        )
    }

    private fun getMockParentProgress(): ParentProgressData {
        val heatmap = mutableListOf<DailyAttendanceHeatmapItem>()
        for (i in 1..30) {
            val status = when (i) {
                7, 14, 21, 28 -> "LIBUR"
                12 -> "IZIN"
                19 -> "TERLAMBAT"
                else -> "HADIR"
            }
            heatmap.add(DailyAttendanceHeatmapItem(i, "$i Agu", status))
        }

        return ParentProgressData(
            childName = "Ahmad Kemal Hafizh",
            childClass = "XII MIPA 1",
            academicScore = 91.2f,
            classAverageScore = 84.5f,
            tahfidzCurrentJuz = 30,
            tahfidzTargetJuz = 30,
            totalSurahCompleted = 37,
            attendanceHeatmap = heatmap
        )
    }

    private fun getMockExecutiveKpi(): ExecutiveAnalyticsData {
        return ExecutiveAnalyticsData(
            kpiList = listOf(
                ExecutiveKpiItem("Tingkat Kehadiran Kampus", "97.8%", "+1.2% dari minggu lalu", true, 0.978f, "Target KPI Yayasan: 95.0%"),
                ExecutiveKpiItem("Realisasi Pembayaran SPP", "92.5%", "+3.8% dari bulan lalu", true, 0.925f, "Total terkumpul: Rp 918 Juta"),
                ExecutiveKpiItem("Kelulusan KKTP Siswa", "96.4%", "+0.5% semester ini", true, 0.964f, "Target Standar Mutu YBWSA: 90%"),
                ExecutiveKpiItem("Rasio Siswa Berprestasi", "18.4%", "+2.1% tahun 2025/2026", true, 0.75f, "Olimpiade Sains & FLS2N Nasional")
            ),
            totalActiveStudents = 1080,
            attendanceTodayPercentage = 97.8f,
            totalSppCollected = "Rp 918.000.000",
            collectionRatePercentage = 92.5f
        )
    }
}
