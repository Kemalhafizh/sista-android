package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.AttendanceQrVerifyRequest
import com.sultanagung1.sista.data.model.AttendanceQrVerifyResult
import com.sultanagung1.sista.data.model.EventTicketScanRequest
import com.sultanagung1.sista.data.model.EventTicketScanResult
import com.sultanagung1.sista.data.model.VisitorPassScanRequest
import com.sultanagung1.sista.data.model.VisitorPassScanResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Backs 3 of the multi-mode camera scanner's 4 real modes (the 4th, library
 * self-checkout, already has a real endpoint via LibraryApiService/
 * LibraryRepository). Each call here hits a genuine backend check/mutation
 * — dynamic-QR identity verification, event ticket scan, visitor badge
 * check-in/out — none fabricate a "verified"/"valid" result locally.
 */
interface ScannerMobileApiService {

    @POST("mobile/attendance/verify-qr")
    suspend fun verifyAttendanceQr(@Body request: AttendanceQrVerifyRequest): Response<AttendanceQrVerifyResult>

    @POST("mobile/scanner/event-ticket")
    suspend fun scanEventTicket(@Body request: EventTicketScanRequest): Response<EventTicketScanResult>

    @POST("mobile/scanner/visitor-pass")
    suspend fun scanVisitorPass(@Body request: VisitorPassScanRequest): Response<VisitorPassScanResult>
}
