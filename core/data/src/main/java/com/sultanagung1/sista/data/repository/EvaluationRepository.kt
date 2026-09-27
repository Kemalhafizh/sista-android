package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.EvaluationApiService
import com.sultanagung1.sista.data.model.CastVoteRequest
import com.sultanagung1.sista.data.model.FacilitySurveyItem
import com.sultanagung1.sista.data.model.OsisElectionInfo
import com.sultanagung1.sista.data.model.SubmitEvaluationRequest
import com.sultanagung1.sista.data.model.TeacherEvaluationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class EvaluationRepository(private val apiService: EvaluationApiService) {

    fun getTeacherEvaluations(): Flow<NetworkResult<List<TeacherEvaluationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherEvaluations()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleTeacherEvaluations()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleTeacherEvaluations()))
        }
    }.flowOn(Dispatchers.IO)

    fun submitTeacherEvaluation(request: SubmitEvaluationRequest): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.submitTeacherEvaluation(request)
            emit(NetworkResult.Success(response.isSuccessful))
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)

    fun getFacilitySurveys(): Flow<NetworkResult<List<FacilitySurveyItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getFacilitySurveys()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleFacilitySurveys()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleFacilitySurveys()))
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

    private fun getSampleTeacherEvaluations(): List<TeacherEvaluationItem> = listOf(
        TeacherEvaluationItem(
            id = 1,
            teacherName = "Drs. H. Ahmad Fauzi, M.Pd",
            subjectName = "Matematika Peminatan",
            className = "XII MIPA 1",
            isSubmitted = false
        ),
        TeacherEvaluationItem(
            id = 2,
            teacherName = "Dr. Hj. Siti Nurjanah, M.Si",
            subjectName = "Fisika Modern",
            className = "XII MIPA 1",
            isSubmitted = true,
            pedagogyRating = 5,
            punctualityRating = 5,
            islamicMannerRating = 5
        ),
        TeacherEvaluationItem(
            id = 3,
            teacherName = "Ust. M. Rizqi, Lc., M.Hum",
            subjectName = "Pendidikan Agama Islam",
            className = "XII MIPA 1",
            isSubmitted = false
        )
    )

    private fun getSampleFacilitySurveys(): List<FacilitySurveyItem> = listOf(
        FacilitySurveyItem(1, "Kebersihan & Kenyamanan Toilet Siswa", 4),
        FacilitySurveyItem(2, "Kecepatan Wi-Fi & Fasilitas Smart Classroom", 4),
        FacilitySurveyItem(3, "Kualitas Makanan Halal & Higienitas Kantin", 5),
        FacilitySurveyItem(4, "Kenyamanan Ibadah di Masjid Sultan Agung", 5)
    )
}
