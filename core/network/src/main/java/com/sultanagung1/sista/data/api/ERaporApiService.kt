package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ERaporApiService {

    @GET("rapor/current")
    suspend fun getCurrentRapor(): Response<RaporResponse<RaporDetailData>>

    @GET("rapor/child/{childId}")
    suspend fun getChildRapor(
        @Path("childId") childId: Long
    ): Response<RaporResponse<RaporDetailData>>

    @POST("rapor/generate-pdf")
    suspend fun generatePdf(
        @Query("report_card_id") reportCardId: Long,
        @Query("student_id") studentId: Long
    ): Response<RaporResponse<Any>>
}
