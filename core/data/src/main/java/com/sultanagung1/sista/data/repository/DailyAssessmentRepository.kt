package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.DailyAssessmentMobileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DailyAssessmentRepository(private val apiService: DailyAssessmentMobileApiService) {

    fun getTeacherAssessments(): Flow<NetworkResult<List<DailyAssessmentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherAssessments()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data.assessments))
            } else {
                emit(NetworkResult.Error("Gagal memuat daftar penilaian harian", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    fun createAssessment(request: CreateAssessmentRequest): Flow<NetworkResult<DailyAssessmentItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.storeAssessment(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error("Gagal membuat penilaian", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    fun submitBatchScores(assessmentId: Long, scores: List<StudentScoreInput>): Flow<NetworkResult<BatchScoreResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.batchScores(assessmentId, BatchScoreRequest(scores))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error("Gagal menyimpan nilai siswa secara massal", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat menyimpan nilai"))
        }
    }.flowOn(Dispatchers.IO)

    fun autoAssignRemedial(assessmentId: Long, deadline: String? = null): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.autoRemedial(assessmentId, deadline)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.message ?: "Otomasi penugasan remedial berhasil dijalankan."))
            } else {
                emit(NetworkResult.Error("Gagal menjalankan otomasi remedial", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat menjalankan otomasi remedial"))
        }
    }.flowOn(Dispatchers.IO)

    fun getStudentRemedials(): Flow<NetworkResult<List<RemedialItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentRemedials()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error("Gagal memuat daftar remedial", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    fun getStudentAssessmentHistory(): Flow<NetworkResult<List<StudentAssessmentHistoryItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentAssessments()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error("Gagal memuat riwayat penilaian", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)
}
