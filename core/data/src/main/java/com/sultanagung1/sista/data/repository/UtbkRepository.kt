package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.UtbkApiService
import com.sultanagung1.sista.data.model.AlumniCampusItem
import com.sultanagung1.sista.data.model.UtbkRecommendationResponse
import com.sultanagung1.sista.data.model.UtbkTryoutItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UtbkRepository(private val apiService: UtbkApiService) {

    fun getUtbkTryouts(): Flow<NetworkResult<List<UtbkTryoutItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getUtbkTryouts()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat daftar tryout UTBK", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMajorRecommendations(): Flow<NetworkResult<UtbkRecommendationResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMajorRecommendations()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat rekomendasi jurusan", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    fun getCampusAlumniDirectory(): Flow<NetworkResult<List<AlumniCampusItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCampusAlumniDirectory()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat direktori alumni", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)
}
