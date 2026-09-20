package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.CastVoteRequest
import com.sultanagung1.sista.data.model.FacilitySurveyItem
import com.sultanagung1.sista.data.model.OsisCandidateItem
import com.sultanagung1.sista.data.model.SubmitEvaluationRequest
import com.sultanagung1.sista.data.model.TeacherEvaluationItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EvaluationApiService {

    @GET("evaluations/teachers")
    suspend fun getTeacherEvaluations(): Response<List<TeacherEvaluationItem>>

    @POST("evaluations/teachers/submit")
    suspend fun submitTeacherEvaluation(@Body request: SubmitEvaluationRequest): Response<Map<String, Any>>

    @GET("evaluations/facilities")
    suspend fun getFacilitySurveys(): Response<List<FacilitySurveyItem>>

    @POST("evaluations/facilities/submit")
    suspend fun submitFacilitySurvey(@Body surveys: List<FacilitySurveyItem>): Response<Map<String, Any>>

    @GET("evaluations/osis-election")
    suspend fun getOsisElection(): Response<List<OsisCandidateItem>>

    @POST("evaluations/osis-election/vote")
    suspend fun castOsisVote(@Body request: CastVoteRequest): Response<Map<String, Any>>
}
