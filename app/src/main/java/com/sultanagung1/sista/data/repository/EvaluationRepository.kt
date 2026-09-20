package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.EvaluationApiService
import com.sultanagung1.sista.data.model.CastVoteRequest
import com.sultanagung1.sista.data.model.FacilitySurveyItem
import com.sultanagung1.sista.data.model.OsisCandidateItem
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

    fun getOsisElection(): Flow<NetworkResult<List<OsisCandidateItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getOsisElection()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleOsisCandidates()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleOsisCandidates()))
        }
    }.flowOn(Dispatchers.IO)

    fun castOsisVote(candidateId: Long, voterHash: String): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.castOsisVote(CastVoteRequest(candidateId, voterHash))
            emit(NetworkResult.Success(response.isSuccessful))
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
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

    private fun getSampleOsisCandidates(): List<OsisCandidateItem> = listOf(
        OsisCandidateItem(
            id = 1,
            number = 1,
            presidentName = "Muhammad Farhan Al-Ghifari (XI MIPA 2)",
            vicePresidentName = "Nabila Putri Maharani (XI IPS 1)",
            vision = "Mewujudkan OSIS SMA Islam Sultan Agung 1 yang Unggul, Berakhlak Qur'ani, dan Berdaya Saing Global di Era Digital.",
            mission = "1. Digitalisasi kegiatan kesiswaan terpadu.\n2. Optimalisasi program tahfidz & kajian remaja.\n3. Membangun sinergi prestasi akademik & seni olahraga.",
            totalVotes = 342,
            isVoted = false
        ),
        OsisCandidateItem(
            id = 2,
            number = 2,
            presidentName = "Zaidan Ilham Pratama (XI MIPA 1)",
            vicePresidentName = "Aisyah Nur Salsabila (XI MIPA 3)",
            vision = "SULAONE BERSINERGI: Membangun Ekosistem Kesiswaan yang Kolaboratif, Inovatif, dan Berkarakter Islami.",
            mission = "1. Memperluas festival seni & sains antarsekolah.\n2. Wadah aspirasi suara siswa berbasis SuperApp.\n3. Gerakan kepedulian sosial bakti nusantara.",
            totalVotes = 298,
            isVoted = false
        )
    )
}
