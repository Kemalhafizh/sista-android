package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface DailyAssessmentMobileApiService {

    @GET("assessments/teacher")
    suspend fun getTeacherAssessments(): Response<AssessmentResponse<Any>>

    @POST("assessments/teacher")
    suspend fun storeAssessment(
        @Body request: CreateAssessmentRequest
    ): Response<AssessmentResponse<DailyAssessmentItem>>

    @POST("assessments/{id}/batch-scores")
    suspend fun batchScores(
        @Path("id") assessmentId: Long,
        @Body request: BatchScoreRequest
    ): Response<AssessmentResponse<BatchScoreResult>>

    @POST("assessments/{id}/auto-remedial")
    suspend fun autoRemedial(
        @Path("id") assessmentId: Long,
        @Query("deadline") deadline: String? = null
    ): Response<AssessmentResponse<Any>>

    @GET("assessments/student")
    suspend fun getStudentAssessments(): Response<AssessmentResponse<List<StudentAssessmentHistoryItem>>>

    @GET("assessments/student/remedials")
    suspend fun getStudentRemedials(): Response<AssessmentResponse<List<RemedialItem>>>

    @POST("assessments/remedials/{sessionId}/score")
    suspend fun submitRemedialScore(
        @Path("sessionId") sessionId: Long,
        @Query("score") score: Double,
        @Query("teacher_notes") notes: String? = null
    ): Response<AssessmentResponse<Any>>
}
