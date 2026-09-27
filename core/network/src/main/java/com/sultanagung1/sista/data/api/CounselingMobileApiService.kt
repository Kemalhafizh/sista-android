package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CounselingMobileApiService {

    @GET("counseling/dashboard")
    suspend fun getCounselorDashboard(): Response<CounselingApiResponse<CounselorDashboardData>>

    @POST("counseling/sessions")
    suspend fun createSession(
        @Body request: CreateCounselingSessionRequest
    ): Response<CounselingApiResponse<CounselingSessionItem>>

    @POST("counseling/appointments/request")
    suspend fun requestAppointment(
        @Body request: RequestAppointmentRequest
    ): Response<CounselingApiResponse<CounselingSessionItem>>

    @GET("counseling/appointments/my")
    suspend fun getMyStudentAppointments(): Response<CounselingApiResponse<List<CounselingSessionItem>>>

    @GET("counseling/at-risk")
    suspend fun getAtRiskStudents(): Response<CounselingApiResponse<List<CounselingAtRiskStudentItem>>>
}
