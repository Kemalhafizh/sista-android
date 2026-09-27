package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.EvaluationApiService
import com.sultanagung1.sista.data.model.CastVoteRequest
import com.sultanagung1.sista.data.model.FacilitySurveyItem
import com.sultanagung1.sista.data.model.SubmitEvaluationRequest
import com.sultanagung1.sista.data.model.SubmitFacilityEvaluationRequest
import com.sultanagung1.sista.data.model.OsisElectionInfo
import com.sultanagung1.sista.data.model.TeacherEvaluationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.roundToInt

class EvaluationRepository(private val apiService: EvaluationApiService) {

    fun getTeacherEvaluations(): Flow<NetworkResult<List<TeacherEvaluationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherEvaluations()
            if (response.isSuccessful && response.body() != null) {
                val mapped = response.body()!!.map {
                    TeacherEvaluationItem(id = it.id, teacherName = it.name, subjectName = it.subject)
                }
                emit(NetworkResult.Success(mapped))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat daftar guru untuk dievaluasi" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat daftar guru"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * The backend has one [rating] (1-5) + [comments] column, not separate
     * pedagogy/punctuality/manner columns — the three UI sliders are
     * averaged into [rating] and their individual values are folded into
     * [comments] so they aren't silently dropped by the real submission.
     */
    fun submitTeacherEvaluation(
        teacherId: Long,
        pedagogy: Int,
        punctuality: Int,
        islamicManner: Int,
        feedback: String
    ): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val rating = ((pedagogy + punctuality + islamicManner) / 3.0).roundToInt().coerceIn(1, 5)
            val comments = buildString {
                append("Pedagogik: $pedagogy★, Ketepatan Waktu: $punctuality★, Karakter Islami: $islamicManner★.")
                if (feedback.isNotBlank()) append(" Catatan: $feedback")
            }
            val response = apiService.submitTeacherEvaluation(SubmitEvaluationRequest(teacherId, rating, comments))
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mengirim evaluasi guru" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mengirim evaluasi guru"))
        }
    }.flowOn(Dispatchers.IO)

    fun getFacilitySurveys(): Flow<NetworkResult<List<FacilitySurveyItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getFacilitySurveys()
            if (response.isSuccessful && response.body() != null) {
                val mapped = response.body()!!.map { FacilitySurveyItem(id = it.id, facilityName = it.name) }
                emit(NetworkResult.Success(mapped))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat daftar fasilitas" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat daftar fasilitas"))
        }
    }.flowOn(Dispatchers.IO)

    fun submitFacilityEvaluation(facilityId: Long, rating: Int, comments: String? = null): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.submitFacilitySurvey(SubmitFacilityEvaluationRequest(facilityId, rating, comments))
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mengirim survei fasilitas" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mengirim survei fasilitas"))
        }
    }.flowOn(Dispatchers.IO)

    fun getOsisElection(): Flow<NetworkResult<OsisElectionInfo>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getOsisElection()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message(), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Gagal memuat data pemilihan OSIS."))
        }
    }.flowOn(Dispatchers.IO)

    fun castOsisVote(electionId: Long, candidateId: Long, biometricSignature: String): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.castOsisVote(CastVoteRequest(electionId, candidateId, biometricSignature))
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(response.message(), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Gagal mengirimkan suara."))
        }
    }.flowOn(Dispatchers.IO)
}
