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
import retrofit2.http.Query

interface DisciplineApiService {

    // studentUuid: which child a parent is viewing (ignored for a student,
    // who always gets their own record). Null → the server's default child.

    @GET("discipline/summary")
    suspend fun getDisciplineSummary(
        @Query("student_uuid") studentUuid: String? = null
    ): Response<ApiEnvelope<DisciplineSummary>>

    @GET("discipline/history")
    suspend fun getDisciplineRecords(
        @Query("student_uuid") studentUuid: String? = null
    ): Response<ApiEnvelope<List<DisciplineRecord>>>

    @GET("discipline/warning-letters")
    suspend fun getWarningLetters(
        @Query("student_uuid") studentUuid: String? = null
    ): Response<ApiEnvelope<List<WarningLetterItem>>>

    @POST("discipline/warning-letters/{id}/sign")
    suspend fun signWarningLetter(
        @Path("id") letterId: Long,
        @Body request: SignWarningLetterRequest
    ): Response<ApiEnvelope<Map<String, Any>>>
}
