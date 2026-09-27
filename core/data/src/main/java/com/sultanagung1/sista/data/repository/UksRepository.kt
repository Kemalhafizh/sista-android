package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.UksMobileApiService
import com.sultanagung1.sista.data.model.HealthScreeningData
import com.sultanagung1.sista.data.model.UksRecordVisitItem
import com.sultanagung1.sista.data.model.UksVisitRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UksRepository(private val apiService: UksMobileApiService) {

    fun getTodayVisits(studentId: Long? = null): Flow<NetworkResult<List<UksRecordVisitItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTodayVisits(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat catatan kunjungan UKS" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan UKS"))
        }
    }.flowOn(Dispatchers.IO)

    fun recordVisit(request: UksVisitRequest): Flow<NetworkResult<UksRecordVisitItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.recordVisit(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal menyimpan data kunjungan UKS" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat simpan UKS"))
        }
    }.flowOn(Dispatchers.IO)

    fun getHealthScreening(studentId: String): Flow<NetworkResult<HealthScreeningData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getHealthScreening(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat data skrining kesehatan" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan skrining UKS"))
        }
    }.flowOn(Dispatchers.IO)
}
