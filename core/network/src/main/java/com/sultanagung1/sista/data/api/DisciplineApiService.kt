package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.DisciplineSummary
import com.sultanagung1.sista.data.model.SignWarningLetterRequest
import com.sultanagung1.sista.data.model.WarningLetterItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DisciplineApiService {

    @GET("discipline/summary")
    suspend fun getDisciplineSummary(): Response<ApiEnvelope<DisciplineSummary>>

    @GET("discipline/records")
    suspend fun getDisciplineRecords(): Response<ApiEnvelope<List<DisciplineRecord>>>

    @GET("discipline/warning-letters")
    suspend fun getWarningLetters(): Response<ApiEnvelope<List<WarningLetterItem>>>

    @POST("discipline/warning-letters/{id}/sign")
    suspend fun signWarningLetter(
        @Path("id") letterId: Long,
        @Body request: SignWarningLetterRequest
    ): Response<ApiEnvelope<Map<String, Any>>>
}
