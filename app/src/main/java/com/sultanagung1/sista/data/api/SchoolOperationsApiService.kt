package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SchoolOperationsApiService {

    @GET("academic-calendar")
    suspend fun getCalendarEvents(
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
        @Query("category") category: String? = null
    ): Response<SchoolOpsApiResponse<List<AcademicCalendarEventItem>>>

    @GET("spmb/waves")
    suspend fun getSpmbWaves(): Response<SchoolOpsApiResponse<List<SpmbWaveItem>>>

    @POST("spmb/register")
    suspend fun registerSpmb(
        @Body request: SpmbRegisterRequest
    ): Response<SchoolOpsApiResponse<SpmbRegistrationStatus>>

    @GET("spmb/status/{registrationNo}")
    suspend fun checkSpmbStatus(
        @Path("registrationNo") registrationNo: String
    ): Response<SchoolOpsApiResponse<SpmbRegistrationStatus>>

    @GET("uks/visits/my")
    suspend fun getUksVisits(
        @Query("student_id") studentId: Long? = null
    ): Response<SchoolOpsApiResponse<List<UksRecordVisitItem>>>

    @GET("uks/screening/{studentId}")
    suspend fun getHealthScreening(
        @Path("studentId") studentId: Long
    ): Response<SchoolOpsApiResponse<HealthScreeningData>>

    @GET("teaching-journals/my")
    suspend fun getTeachingJournals(): Response<SchoolOpsApiResponse<List<SchoolTeachingJournalItem>>>

    @POST("teaching-journals")
    suspend fun storeTeachingJournal(
        @Body request: StoreTeachingJournalRequest
    ): Response<SchoolOpsApiResponse<SchoolTeachingJournalItem>>
}
