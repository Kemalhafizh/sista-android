package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.EventTicketScanRequest
import com.sultanagung1.sista.data.model.EventTicketScanResult
import com.sultanagung1.sista.data.model.VisitorPassScanRequest
import com.sultanagung1.sista.data.model.VisitorPassScanResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Backs two of the camera scanner's modes (the third, library self-checkout,
 * goes through LibraryApiService/LibraryRepository). Each call hits a genuine
 * backend check/mutation (event ticket scan, visitor badge check-in/out);
 * none fabricate a "verified"/"valid" result locally.
 */
interface ScannerMobileApiService {

    @POST("mobile/scanner/event-ticket")
    suspend fun scanEventTicket(@Body request: EventTicketScanRequest): Response<EventTicketScanResult>

    @POST("mobile/scanner/visitor-pass")
    suspend fun scanVisitorPass(@Body request: VisitorPassScanRequest): Response<VisitorPassScanResult>
}
