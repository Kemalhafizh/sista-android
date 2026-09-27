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

    fun getDisciplineSummary(studentUuid: String? = null): Flow<NetworkResult<DisciplineSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getDisciplineSummary(studentUuid)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: serverMessage(response.errorBody()?.string()) ?: "Gagal memuat ringkasan poin kedisiplinan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getDisciplineRecords(studentUuid: String? = null): Flow<NetworkResult<List<DisciplineRecord>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getDisciplineRecords(studentUuid)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: serverMessage(response.errorBody()?.string()) ?: "Gagal memuat riwayat poin kedisiplinan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getWarningLetters(studentUuid: String? = null): Flow<NetworkResult<List<WarningLetterItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getWarningLetters(studentUuid)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: serverMessage(response.errorBody()?.string()) ?: "Gagal memuat daftar surat peringatan (Kode: ${response.code()}).", response.code()))
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
                // it wasn't. Errors (403 wrong parent, already signed, 422)
                // arrive in the error body, not body(), so read that too.
                val message = response.body()?.message
                    ?: serverMessage(response.errorBody()?.string())
                    ?: "Gagal menandatangani surat peringatan (Kode: ${response.code()})."
                emit(NetworkResult.Error(message, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    private fun serverMessage(errorBody: String?): String? = errorBody
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { com.google.gson.JsonParser.parseString(it).asJsonObject }.getOrNull() }
        ?.get("message")
        ?.takeIf { it.isJsonPrimitive }
        ?.asString
        ?.takeIf { it.isNotBlank() }
}
