package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DocumentApiService {

    @GET("documents/reports")
    suspend fun getReportCards(): Response<ApiEnvelope<List<SchoolDocumentItem>>>

    @GET("documents/certificates")
    suspend fun getCertificates(): Response<ApiEnvelope<List<SchoolDocumentItem>>>

    // Backend expects a real multipart file (validated as file|mimes:jpeg,jpg,png,pdf),
    // not a JSON body — this was previously sending @Body Map<String,String>, which
    // would have failed validation on every real call.
    @Multipart
    @POST("documents/ocr/scan")
    suspend fun processOcrImage(
        @Part documentImage: MultipartBody.Part,
        @Part("document_type") documentType: RequestBody
    ): Response<ApiEnvelope<OcrScanStartResponse>>

    @GET("documents/ocr/{taskId}")
    suspend fun getOcrResult(@Path("taskId") taskId: Long): Response<ApiEnvelope<OcrScanResult>>

    @POST("documents/signature/submit")
    suspend fun submitDigitalSignature(
        @Body payload: SignatureSubmission
    ): Response<ApiEnvelope<Map<String, Any>>>
}
