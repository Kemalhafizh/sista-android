package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.ClassAnalyticsData
import com.sultanagung1.sista.data.model.ExecutiveAnalyticsData
import com.sultanagung1.sista.data.model.ParentProgressData
import com.sultanagung1.sista.data.model.StudentAnalyticsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * NOTE ON BACKEND STATE (read before "fixing" this further): sistem-terpadu's
 * analytics/... routes this service calls do not exist in routes/api.php at all
 * (404 on every call). The closest registered equivalents (VizController /
 * DataVisualizationService — viz/radar/..., viz/trend/..., viz/distribution/...,
 * viz/comparison/..., viz/counters/live) are themselves backend-side stubs:
 * getStudentRadarChart($studentId) and getGradeDistribution($examId) never
 * use the id they're given and return the exact same hardcoded numbers for
 * every student/exam. Pointing this repository at those endpoints instead
 * would trade one kind of fake data for another — a real HTTP 200 carrying
 * the same fabricated numbers for every user — which is not meaningfully
 * "synced with the backend." Real analytics for this screen needs backend
 * work (DataVisualizationService actually querying Grade/Attendance/
 * ReportCard) before this repository can show genuine data. Until then this
 * surfaces a real error instead of silently faking success.
 */
class AnalyticsRepository(private val apiClient: ApiClient) {

    fun getStudentAnalytics(): Flow<NetworkResult<StudentAnalyticsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getStudentAnalytics()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Analitik akademik belum tersedia dari server (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassAnalytics(className: String, subjectName: String): Flow<NetworkResult<ClassAnalyticsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getClassAnalytics(className, subjectName)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Analitik kelas belum tersedia dari server (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getParentProgress(): Flow<NetworkResult<ParentProgressData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getParentProgress()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Progres anak belum tersedia dari server (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getExecutiveKpi(): Flow<NetworkResult<ExecutiveAnalyticsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getExecutiveKpi()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("KPI eksekutif belum tersedia dari server (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}