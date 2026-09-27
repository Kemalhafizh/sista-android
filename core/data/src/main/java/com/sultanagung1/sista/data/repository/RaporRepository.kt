package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ERaporApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RaporRepository(private val apiService: ERaporApiService) {

    fun getStudentRapor(childId: Long? = null): Flow<NetworkResult<RaporDetailData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = if (childId != null) apiService.getChildRapor(childId) else apiService.getCurrentRapor()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error("Gagal memuat data rapor", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    fun generatePdf(reportCardId: Long, studentId: Long): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.generatePdf(reportCardId, studentId)
            if (response.isSuccessful) {
                emit(NetworkResult.Success("PDF Rapor berhasil digenerate"))
            } else {
                emit(NetworkResult.Error("Gagal generate PDF", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)
}
