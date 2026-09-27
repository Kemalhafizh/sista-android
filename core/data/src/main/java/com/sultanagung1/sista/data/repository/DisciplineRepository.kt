package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.DisciplineApiService
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.DisciplineSummary
import com.sultanagung1.sista.data.model.SignWarningLetterRequest
import com.sultanagung1.sista.data.model.WarningLetterItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DisciplineRepository(private val apiService: DisciplineApiService) {

    fun getDisciplineSummary(): Flow<NetworkResult<DisciplineSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getDisciplineSummary()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat ringkasan poin kedisiplinan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getDisciplineRecords(): Flow<NetworkResult<List<DisciplineRecord>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getDisciplineRecords()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat riwayat poin kedisiplinan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getWarningLetters(): Flow<NetworkResult<List<WarningLetterItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getWarningLetters()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar surat peringatan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /** Emits the real "signed_at" timestamp the server recorded, not a placeholder. */
    fun signWarningLetter(letterId: Long, request: SignWarningLetterRequest): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.signWarningLetter(letterId, request)
            val signedAt = response.body()?.data?.get("signed_at")?.toString()
            if (response.isSuccessful && response.body()?.success == true && signedAt != null) {
                emit(NetworkResult.Success(signedAt))
            } else {
                // A parent must know their signature genuinely failed to save —
                // claiming success here regardless of the real server response
                // would let them believe a warning letter was acknowledged when
                // it wasn't.
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal menandatangani surat peringatan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}
