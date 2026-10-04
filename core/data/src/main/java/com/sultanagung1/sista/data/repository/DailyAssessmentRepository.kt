package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.DailyAssessmentMobileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DailyAssessmentRepository(
    private val apiService: DailyAssessmentMobileApiService,
    private val messages: FallbackMessages,
) {

    fun getTeacherAssessments(): Flow<NetworkResult<TeacherAssessmentsData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherAssessments()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.assessments_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /** The assessed class with each student's stored score. */
    fun getScoreSheet(assessmentId: Long): Flow<NetworkResult<AssessmentScoreSheet>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getScoreSheet(assessmentId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                // A 403 carries the server's own "data ini milik kelas guru lain".
                emit(NetworkResult.Error(messages.failure(response, R.string.score_sheet_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun createAssessment(request: CreateAssessmentRequest): Flow<NetworkResult<DailyAssessmentItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.storeAssessment(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.assessment_create_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun submitBatchScores(assessmentId: Long, scores: List<StudentScoreInput>): Flow<NetworkResult<BatchScoreResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.batchScores(assessmentId, BatchScoreRequest(scores))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.scores_save_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun autoAssignRemedial(assessmentId: Long, deadline: String? = null): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.autoRemedial(assessmentId, deadline)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.message ?: messages.get(R.string.remedial_auto_done)))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.remedial_auto_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getStudentRemedials(): Flow<NetworkResult<List<RemedialItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentRemedials()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.remedials_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getStudentAssessmentHistory(): Flow<NetworkResult<List<StudentAssessmentHistoryItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentAssessments()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.assessment_history_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)
}
