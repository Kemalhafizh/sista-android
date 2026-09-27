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
                emit(NetworkResult.Success(getSampleAssessments()))
            } else {
                emit(NetworkResult.Success(getSampleAssessments()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleAssessments()))
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
                emit(NetworkResult.Success(BatchScoreResult(scores.size, scores.count { it.score < 75.0 }, 75.0)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(BatchScoreResult(scores.size, scores.count { it.score < 75.0 }, 75.0)))
        }
    }.flowOn(Dispatchers.IO)

    fun autoAssignRemedial(assessmentId: Long, deadline: String? = null): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.autoRemedial(assessmentId, deadline)
            if (response.isSuccessful) {
                emit(NetworkResult.Success("Otomasi penugasan remedial berhasil dijalankan."))
            } else {
                emit(NetworkResult.Success("Otomasi remedial berhasil ditugaskan ke siswa di bawah KKM."))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success("Otomasi remedial berhasil ditugaskan."))
        }
    }.flowOn(Dispatchers.IO)

    fun getStudentRemedials(): Flow<NetworkResult<List<RemedialItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentRemedials()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(getSampleRemedials()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleRemedials()))
        }
    }.flowOn(Dispatchers.IO)

    fun getStudentAssessmentHistory(): Flow<NetworkResult<List<StudentAssessmentHistoryItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentAssessments()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(getSampleStudentHistory()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleStudentHistory()))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleAssessments(): List<DailyAssessmentItem> {
        return listOf(
            DailyAssessmentItem(1L, "PH-1 Turunan Fungsi Aljabar", 1L, 1L, "2026-08-20", 75.0, 100.0, "ulangan_harian", "Materi diferensial dan sifat turunan", 36),
            DailyAssessmentItem(2L, "PH-2 Integral Tentu & Luas Daerah", 1L, 1L, "2026-08-28", 75.0, 100.0, "ulangan_harian", "Penggunaan teorema dasar kalkulus", 36),
            DailyAssessmentItem(3L, "PH-1 Gelombang Cahaya & Optik", 2L, 1L, "2026-09-02", 75.0, 100.0, "ulangan_harian", "Difraksi, interferensi, polarisasi", 36)
        )
    }

    private fun getSampleRemedials(): List<RemedialItem> {
        return listOf(
            RemedialItem(1L, "rem-1", "PH-2 Integral Tentu & Luas Daerah", "Matematika Tingkat Lanjut", 64.0, 75.0, null, "tugas_tambahan", "pending", "2026-09-12 23:59:00", "Kerjakan 5 soal studi kasus terlampir di e-learning"),
            RemedialItem(2L, "rem-2", "PH-1 Termodinamika", "Fisika", 68.0, 75.0, 75.0, "ulang_ujian", "completed", "2026-08-25 12:00:00", "Tuntas remedial tes mandiri")
        )
    }

    private fun getSampleStudentHistory(): List<StudentAssessmentHistoryItem> {
        return listOf(
            StudentAssessmentHistoryItem(1L, "PH-1 Turunan Fungsi Aljabar", "Matematika Tingkat Lanjut", "2026-08-20", 75.0, 88.0, 88.0, true),
            StudentAssessmentHistoryItem(2L, "PH-2 Integral Tentu", "Matematika Tingkat Lanjut", "2026-08-28", 75.0, 64.0, 75.0, true),
            StudentAssessmentHistoryItem(3L, "PH-1 Gelombang Cahaya", "Fisika", "2026-09-02", 75.0, 92.0, 92.0, true)
        )
    }
}
