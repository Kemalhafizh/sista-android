package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.StudentProfile360Data
import com.sultanagung1.sista.data.model.StudentProfileApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StudentProfileApiService {

    @GET("profile/student/me")
    suspend fun getMyProfile(): Response<StudentProfileApiResponse<StudentProfile360Data>>

    @GET("profile/student/{id}")
    suspend fun getStudentProfile(
        @Path("id") studentId: Long
    ): Response<StudentProfileApiResponse<StudentProfile360Data>>
}
