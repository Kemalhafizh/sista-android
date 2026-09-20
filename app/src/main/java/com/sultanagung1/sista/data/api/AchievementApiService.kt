package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.AcademicCvSummary
import com.sultanagung1.sista.data.model.AchievementItem
import com.sultanagung1.sista.data.model.CertificateItem
import com.sultanagung1.sista.data.model.UploadAchievementRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AchievementApiService {

    @GET("achievements/list")
    suspend fun getAchievements(): Response<List<AchievementItem>>

    @POST("achievements/upload")
    suspend fun uploadAchievement(@Body request: UploadAchievementRequest): Response<AchievementItem>

    @GET("achievements/certificates")
    suspend fun getCertificates(): Response<List<CertificateItem>>

    @GET("achievements/academic-cv-summary")
    suspend fun getAcademicCvSummary(): Response<AcademicCvSummary>
}
