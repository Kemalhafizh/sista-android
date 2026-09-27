package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface QuestionBankApiService {

    @GET("question-bank/categories")
    suspend fun getCategories(
        @Query("subject_id") subjectId: Long? = null
    ): Response<QuestionBankResponse<List<QuestionBankCategory>>>

    @GET("question-bank/items")
    suspend fun getItems(
        @Query("category_id") categoryId: Long? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("cognitive_level") cognitiveLevel: String? = null,
        @Query("type") type: String? = null,
        @Query("search") search: String? = null
    ): Response<QuestionBankResponse<Any>>

    @POST("question-bank/items")
    suspend fun store(
        @Body request: CreateQuestionBankRequest
    ): Response<QuestionBankResponse<QuestionBankItem>>

    @POST("question-bank/auto-generate")
    suspend fun autoGenerate(
        @Body request: AutoGenerateExamRequest
    ): Response<QuestionBankResponse<AutoGenerateExamResponse>>
}
