package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.CastVoteRequest
import com.sultanagung1.sista.data.model.EvaluableFacilityItem
import com.sultanagung1.sista.data.model.EvaluableTeacherItem
import com.sultanagung1.sista.data.model.OsisElectionInfo
import com.sultanagung1.sista.data.model.SubmitEvaluationRequest
import com.sultanagung1.sista.data.model.SubmitFacilityEvaluationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EvaluationApiService {

    @GET("evaluations/teachers")
    suspend fun getTeacherEvaluations(): Response<List<EvaluableTeacherItem>>

    @POST("evaluations/teachers/submit")
    suspend fun submitTeacherEvaluation(@Body request: SubmitEvaluationRequest): Response<Map<String, Any>>

    @GET("evaluations/facilities")
    suspend fun getFacilitySurveys(): Response<List<EvaluableFacilityItem>>

    @POST("evaluations/facilities/submit")
    suspend fun submitFacilitySurvey(@Body request: SubmitFacilityEvaluationRequest): Response<Map<String, Any>>

    @GET("evaluations/osis-election")
    suspend fun getOsisElection(): Response<OsisElectionInfo>

    @POST("evaluations/osis-election/vote")
    suspend fun castOsisVote(@Body request: CastVoteRequest): Response<Map<String, Any>>
}
