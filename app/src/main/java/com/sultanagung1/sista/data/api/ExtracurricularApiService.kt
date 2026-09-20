package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.EkskulAttendanceRequest
import com.sultanagung1.sista.data.model.EkskulItem
import com.sultanagung1.sista.data.model.OsisPostItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExtracurricularApiService {

    @GET("extracurricular/list")
    suspend fun getEkskulList(): Response<List<EkskulItem>>

    @POST("extracurricular/{id}/register")
    suspend fun registerEkskul(@Path("id") ekskulId: Long): Response<Map<String, Any>>

    @POST("extracurricular/attendance/submit")
    suspend fun submitEkskulAttendance(@Body request: EkskulAttendanceRequest): Response<Map<String, Any>>

    @GET("extracurricular/osis-feed")
    suspend fun getOsisFeed(): Response<List<OsisPostItem>>
}
