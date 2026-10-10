package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.ClassAnalyticsData
import com.sultanagung1.sista.data.model.ParentProgressData
import com.sultanagung1.sista.data.model.StudentAnalyticsData
import com.sultanagung1.sista.data.model.TeacherClassOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

/**
 * analytics/student/summary, analytics/teacher/{my-classes,class-performance},
 * analytics/parent/child-progress and analytics/executive/kpi are backed by
 * App\Services\Analytics\MobileAnalyticsService, which queries real
 * Grade/Attendance/ReportCard/TahfidzTarget/Billing/Payment rows — no
 * fabricated data. A non-2xx or empty body surfaces the server's own
 * message rather than silently faking success.
 */
class AnalyticsRepository(private val apiClient: ApiClient) {

    fun getStudentAnalytics(): Flow<NetworkResult<StudentAnalyticsData>> =
        fetch("analitik belajar") { apiClient.analyticsApi.getStudentAnalytics() }

    fun getTeacherClasses(): Flow<NetworkResult<List<TeacherClassOption>>> =
        fetch("daftar kelas mengajar") { apiClient.analyticsApi.getTeacherClasses() }

    /** Sends the ids when known; a server without them falls back to the names. */
    fun getClassAnalytics(option: TeacherClassOption): Flow<NetworkResult<ClassAnalyticsData>> =
        fetch("analitik kelas") {
            apiClient.analyticsApi.getClassAnalytics(option.className, option.subjectName, option.classroomId, option.subjectId)
        }

    /** [studentUuid] null = the first child linked to this account. */
    fun getParentProgress(studentUuid: String? = null): Flow<NetworkResult<ParentProgressData>> =
        fetch("progres anak") { apiClient.analyticsApi.getParentProgress(studentUuid) }

    private fun <T : Any> fetch(what: String, call: suspend () -> Response<T>): Flow<NetworkResult<T>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(NetworkResult.Success(body))
            } else {
                val message = serverMessageOf(response.errorBody()?.string())
                    ?: if (response.code() == 403) "Akun ini tidak memiliki akses ke $what." else "Data $what belum bisa dimuat (kode ${response.code()})."
                emit(NetworkResult.Error(message, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}
