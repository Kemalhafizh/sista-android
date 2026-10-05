package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.QuestionBankApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class QuestionBankRepository(
    private val apiService: QuestionBankApiService,
    private val messages: FallbackMessages,
) {

    fun getCategories(subjectId: Long? = null): Flow<NetworkResult<List<QuestionBankCategory>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCategories(subjectId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.question_bank_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Never fabricate exam content on failure — auto-generate must pull
     * real QuestionBankItem rows or surface a real error, the same
     * standard already enforced for manually-authored exams.
     */
    fun autoGenerateExam(request: AutoGenerateExamRequest): Flow<NetworkResult<AutoGenerateExamResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.autoGenerate(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.question_bank_pick_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun createQuestion(request: CreateQuestionBankRequest): Flow<NetworkResult<QuestionBankItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.store(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.question_bank_save_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)
}
