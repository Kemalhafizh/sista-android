package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ExtracurricularApiService
import com.sultanagung1.sista.data.model.EkskulAttendanceRequest
import com.sultanagung1.sista.data.model.EkskulItem
import com.sultanagung1.sista.data.model.OsisPostItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ExtracurricularRepository(private val apiService: ExtracurricularApiService) {

    fun getEkskulList(): Flow<NetworkResult<List<EkskulItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getEkskulList()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat daftar ekstrakurikuler" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi ekstrakurikuler"))
        }
    }.flowOn(Dispatchers.IO)

    fun registerEkskul(ekskulId: Long): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.registerEkskul(ekskulId)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mendaftar ekstrakurikuler" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mendaftar ekstrakurikuler"))
        }
    }.flowOn(Dispatchers.IO)

    fun submitEkskulAttendance(request: EkskulAttendanceRequest): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.submitEkskulAttendance(request)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mengirim presensi ekstrakurikuler" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mengirim presensi ekstrakurikuler"))
        }
    }.flowOn(Dispatchers.IO)

    fun getOsisFeed(): Flow<NetworkResult<List<OsisPostItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getOsisFeed()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat feed OSIS" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat feed OSIS"))
        }
    }.flowOn(Dispatchers.IO)
}
