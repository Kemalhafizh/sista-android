package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.SchoolOpsApiResponse
import com.sultanagung1.sista.data.model.SpmbRegisterRequest
import com.sultanagung1.sista.data.model.SpmbRegistrationStatus
import com.sultanagung1.sista.data.model.SpmbWaveItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SpmbMobileApiService {

    @GET("spmb/waves")
    suspend fun getWaves(): Response<SchoolOpsApiResponse<List<SpmbWaveItem>>>

    @POST("spmb/register")
    suspend fun register(
        @Body request: SpmbRegisterRequest
    ): Response<SchoolOpsApiResponse<SpmbRegistrationStatus>>

    @GET("spmb/status/{registrationNo}")
    suspend fun trackRegistration(
        @Path("registrationNo") registrationNo: String
    ): Response<SchoolOpsApiResponse<SpmbRegistrationStatus>>

    @Multipart
    @POST("spmb/registrations/{registrationNo}/documents")
    suspend fun uploadDocument(
        @Path("registrationNo") registrationNo: String,
        @Part("document_type") documentType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Map<String, Any>>
}
