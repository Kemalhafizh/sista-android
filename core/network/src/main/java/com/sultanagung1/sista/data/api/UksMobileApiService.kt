package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.HealthScreeningData
import com.sultanagung1.sista.data.model.MedicineItem
import com.sultanagung1.sista.data.model.SchoolOpsApiResponse
import com.sultanagung1.sista.data.model.UksRecordVisitItem
import com.sultanagung1.sista.data.model.UksStudentSearchItem
import com.sultanagung1.sista.data.model.UksVisitRequest
import retrofit2.Response
import retrofit2.http.*

interface UksMobileApiService {

    @GET("uks/visits/my")
    suspend fun getTodayVisits(
        @Query("student_id") studentId: Long? = null
    ): Response<SchoolOpsApiResponse<List<UksRecordVisitItem>>>

    @POST("uks/visits")
    suspend fun recordVisit(
        @Body request: UksVisitRequest
    ): Response<SchoolOpsApiResponse<UksRecordVisitItem>>

    @GET("uks/screening/{studentId}")
    suspend fun getHealthScreening(
        @Path("studentId") studentId: String
    ): Response<SchoolOpsApiResponse<HealthScreeningData>>

    @GET("uks/students/search")
    suspend fun searchStudents(
        @Query("q") query: String
    ): Response<SchoolOpsApiResponse<List<UksStudentSearchItem>>>

    @GET("uks/medicines")
    suspend fun getMedicines(): Response<SchoolOpsApiResponse<List<MedicineItem>>>
}
