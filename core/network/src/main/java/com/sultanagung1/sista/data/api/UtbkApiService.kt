package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.AlumniCampusItem
import com.sultanagung1.sista.data.model.UtbkQuestion
import com.sultanagung1.sista.data.model.UtbkRecommendationResponse
import com.sultanagung1.sista.data.model.UtbkTryoutItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UtbkApiService {

    @GET("utbk/tryouts")
    suspend fun getUtbkTryouts(): Response<List<UtbkTryoutItem>>

    @GET("utbk/tryouts/{id}/questions")
    suspend fun getUtbkQuestions(@Path("id") tryoutId: Long): Response<List<UtbkQuestion>>

    @POST("utbk/tryouts/{id}/submit")
    suspend fun submitUtbkTryout(
        @Path("id") tryoutId: Long,
        @Body answers: Map<String, Int>
    ): Response<Map<String, Any>>

    @GET("utbk/recommendations")
    suspend fun getMajorRecommendations(): Response<UtbkRecommendationResponse>

    @GET("utbk/alumni-directory")
    suspend fun getCampusAlumniDirectory(): Response<List<AlumniCampusItem>>
}
