package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.TahsinAnnotateRequest
import com.sultanagung1.sista.data.model.TahsinAnnotationItem
import com.sultanagung1.sista.data.model.TahsinSubmissionItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TahsinApiService {

    @Multipart
    @POST("tahsin/submissions")
    suspend fun submitRecording(
        @Part("surah_name") surahName: RequestBody,
        @Part("start_ayah") startAyah: RequestBody?,
        @Part("end_ayah") endAyah: RequestBody?,
        @Part("duration_seconds") durationSeconds: RequestBody,
        @Part audio: MultipartBody.Part
    ): Response<ApiEnvelope<TahsinSubmissionItem>>

    @GET("tahsin/submissions")
    suspend fun getMySubmissions(): Response<ApiEnvelope<List<TahsinSubmissionItem>>>

    @GET("tahsin/submissions/{id}")
    suspend fun getSubmissionDetail(@Path("id") id: Long): Response<ApiEnvelope<TahsinSubmissionItem>>

    @GET("teacher/tahsin/submissions")
    suspend fun getAssignedSubmissions(@Query("status") status: String? = null): Response<ApiEnvelope<List<TahsinSubmissionItem>>>

    @POST("teacher/tahsin/submissions/{id}/annotate")
    suspend fun annotate(
        @Path("id") id: Long,
        @Body request: TahsinAnnotateRequest
    ): Response<ApiEnvelope<TahsinAnnotationItem>>

    @POST("teacher/tahsin/submissions/{id}/mark-reviewed")
    suspend fun markReviewed(@Path("id") id: Long): Response<ApiEnvelope<TahsinSubmissionItem>>
}
