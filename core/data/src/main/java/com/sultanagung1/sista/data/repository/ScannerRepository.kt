package com.sultanagung1.sista.data.repository

import com.google.gson.Gson
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ScannerMobileApiService
import com.sultanagung1.sista.data.model.AttendanceQrVerifyRequest
import com.sultanagung1.sista.data.model.AttendanceQrVerifyResult
import com.sultanagung1.sista.data.model.EventTicketScanRequest
import com.sultanagung1.sista.data.model.EventTicketScanResult
import com.sultanagung1.sista.data.model.VisitorPassScanRequest
import com.sultanagung1.sista.data.model.VisitorPassScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class ScannerRepository(private val apiService: ScannerMobileApiService) {

    private val gson = Gson()

    // These 3 endpoints return the SAME {valid, message, ...} shape whether
    // the HTTP status is 200 or 422 — Retrofit only populates body() on a
    // 2xx response, so the 422 case is parsed from errorBody() instead of
    // being treated as a network-level failure.
    private inline fun <reified T> bodyOrParsedError(response: Response<T>): T? {
        return response.body() ?: response.errorBody()?.string()?.let {
            try {
                gson.fromJson(it, T::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun verifyAttendanceQr(qrToken: String): Flow<NetworkResult<AttendanceQrVerifyResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.verifyAttendanceQr(AttendanceQrVerifyRequest(qrToken))
            val result = bodyOrParsedError(response)
            if (result != null) {
                emit(NetworkResult.Success(result))
            } else {
                emit(NetworkResult.Error("Gagal memverifikasi QR presensi (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat memverifikasi QR presensi."))
        }
    }.flowOn(Dispatchers.IO)

    fun scanEventTicket(ticketCode: String): Flow<NetworkResult<EventTicketScanResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.scanEventTicket(EventTicketScanRequest(ticketCode))
            val result = bodyOrParsedError(response)
            if (result != null) {
                emit(NetworkResult.Success(result))
            } else {
                emit(NetworkResult.Error("Gagal memindai tiket (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat memindai tiket."))
        }
    }.flowOn(Dispatchers.IO)

    fun scanVisitorPass(badgeNumber: String): Flow<NetworkResult<VisitorPassScanResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.scanVisitorPass(VisitorPassScanRequest(badgeNumber))
            val result = bodyOrParsedError(response)
            if (result != null) {
                emit(NetworkResult.Success(result))
            } else {
                emit(NetworkResult.Error("Gagal memindai kartu tamu (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat memindai kartu tamu."))
        }
    }.flowOn(Dispatchers.IO)
}
