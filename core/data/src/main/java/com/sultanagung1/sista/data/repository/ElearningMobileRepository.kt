package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ElearningMobileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ElearningMobileRepository(private val apiService: ElearningMobileApiService) {

    fun getStudentClasses(): Flow<NetworkResult<List<ElearningClassItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentClasses()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat kelas e-learning" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi e-learning"))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassMaterials(classId: Long): Flow<NetworkResult<List<ElearningMaterialItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getClassMaterials(classId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat materi kelas" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat materi"))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassAssignments(classId: Long): Flow<NetworkResult<List<ElearningAssignmentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getClassAssignments(classId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat daftar tugas" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat tugas"))
        }
    }.flowOn(Dispatchers.IO)

    fun submitAssignment(
        assignmentId: Long,
        content: String?,
        attachment: ElearningAttachment? = null
    ): Flow<NetworkResult<ElearningSubmissionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = attachment?.let {
                MultipartBody.Part.createFormData(
                    "file",
                    it.fileName,
                    it.bytes.toRequestBody(it.mimeType.toMediaTypeOrNull())
                )
            }
            val response = apiService.submitAssignment(assignmentId, contentBody, filePart)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mengirimkan tugas" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mengirimkan tugas"))
        }
    }.flowOn(Dispatchers.IO)
}
