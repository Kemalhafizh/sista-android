package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.AchievementApiService
import com.sultanagung1.sista.data.model.AcademicCvSummary
import com.sultanagung1.sista.data.model.AchievementItem
import com.sultanagung1.sista.data.model.CertificateItem
import com.sultanagung1.sista.data.model.UploadAchievementRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AchievementRepository(private val apiService: AchievementApiService) {

    fun getAchievements(): Flow<NetworkResult<List<AchievementItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAchievements()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat daftar prestasi", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadAchievement(request: UploadAchievementRequest): Flow<NetworkResult<AchievementItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.uploadAchievement(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal mengunggah bukti prestasi (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat mengunggah prestasi."))
        }
    }.flowOn(Dispatchers.IO)

    fun getCertificates(): Flow<NetworkResult<List<CertificateItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCertificates()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat sertifikat digital", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getAcademicCvSummary(): Flow<NetworkResult<AcademicCvSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAcademicCvSummary()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat ringkasan CV akademik", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}
