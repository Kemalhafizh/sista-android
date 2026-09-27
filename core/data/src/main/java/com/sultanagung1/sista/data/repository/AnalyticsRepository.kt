package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.ClassAnalyticsData
import com.sultanagung1.sista.data.model.ExecutiveAnalyticsData
import com.sultanagung1.sista.data.model.ParentProgressData
import com.sultanagung1.sista.data.model.StudentAnalyticsData
import com.sultanagung1.sista.data.model.TeacherClassOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * analytics/student/summary, analytics/teacher/{my-classes,class-performance},
 * analytics/parent/child-progress and analytics/executive/kpi are backed by
 * App\Services\Analytics\MobileAnalyticsService, which queries real
 * Grade/Attendance/ReportCard/TahfidzTarget/Billing/Payment rows — no
 * fabricated data. A non-2xx or empty body surfaces a real error rather
 * than silently faking success.
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

    fun getTeacherClasses(): Flow<NetworkResult<List<TeacherClassOption>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.analyticsApi.getTeacherClasses()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Daftar kelas mengajar belum tersedia dari server (Kode: ${response.code()}).", response.code()))
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