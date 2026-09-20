package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface DocumentApiService {

    @GET("documents/reports")
    suspend fun getReportCards(): Response<List<SchoolDocumentItem>>

    @GET("documents/certificates")
    suspend fun getCertificates(): Response<List<SchoolDocumentItem>>

    @POST("documents/ocr/scan")
    suspend fun processOcrImage(
        @Body payload: Map<String, String>
    ): Response<OcrScanResult>

    @POST("documents/signature/submit")
    suspend fun submitDigitalSignature(
        @Body payload: SignatureSubmission
    ): Response<Map<String, Any>>
}
