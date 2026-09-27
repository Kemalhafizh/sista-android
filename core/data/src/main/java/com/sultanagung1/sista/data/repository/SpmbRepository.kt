package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.SpmbMobileApiService
import com.sultanagung1.sista.data.model.SpmbDocumentAttachment
import com.sultanagung1.sista.data.model.SpmbRegisterRequest
import com.sultanagung1.sista.data.model.SpmbRegistrationStatus
import com.sultanagung1.sista.data.model.SpmbWaveItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class SpmbRepository(private val apiService: SpmbMobileApiService) {

    fun getWaves(): Flow<NetworkResult<List<SpmbWaveItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getWaves()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat gelombang SPMB" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi SPMB"))
        }
    }.flowOn(Dispatchers.IO)

    fun register(request: SpmbRegisterRequest): Flow<NetworkResult<SpmbRegistrationStatus>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mengirimkan formulir SPMB" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat registrasi SPMB"))
        }
    }.flowOn(Dispatchers.IO)

    fun trackRegistration(registrationNo: String): Flow<NetworkResult<SpmbRegistrationStatus>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.trackRegistration(registrationNo)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Data pendaftaran tidak ditemukan" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan saat melacak berkas SPMB"))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadDocument(
        registrationNo: String,
        documentType: String,
        attachment: SpmbDocumentAttachment
    ): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val filePart = MultipartBody.Part.createFormData(
                "file",
                attachment.fileName,
                attachment.bytes.toRequestBody(attachment.mimeType.toMediaTypeOrNull())
            )
            val response = apiService.uploadDocument(
                registrationNo,
                documentType.toRequestBody("text/plain".toMediaTypeOrNull()),
                filePart
            )
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error("Gagal mengunggah dokumen $documentType (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mengunggah dokumen."))
        }
    }.flowOn(Dispatchers.IO)
}
