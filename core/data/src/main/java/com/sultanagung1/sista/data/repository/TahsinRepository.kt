package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.TahsinApiService
import com.sultanagung1.sista.data.model.TahsinAnnotateRequest
import com.sultanagung1.sista.data.model.TahsinAnnotationItem
import com.sultanagung1.sista.data.model.TahsinSubmissionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class TahsinRepository(private val apiService: TahsinApiService) {

    fun submitRecording(
        filePath: String,
        surahName: String,
        startAyah: Int?,
        endAyah: Int?,
        durationSeconds: Int
    ): Flow<NetworkResult<TahsinSubmissionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val file = File(filePath)
            if (!file.exists() || file.length() == 0L) {
                emit(NetworkResult.Error("Berkas rekaman tidak ditemukan di perangkat."))
                return@flow
            }

            val audioPart = MultipartBody.Part.createFormData(
                "audio",
                file.name,
                file.asRequestBody("audio/mp4".toMediaTypeOrNull())
            )
            val response = apiService.submitRecording(
                surahName = surahName.toRequestBody("text/plain".toMediaTypeOrNull()),
                startAyah = startAyah?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                endAyah = endAyah?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                durationSeconds = durationSeconds.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                audio = audioPart
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mengirim setoran tahsin (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat mengirim rekaman."))
        }
    }.flowOn(Dispatchers.IO)

    fun getMySubmissions(): Flow<NetworkResult<List<TahsinSubmissionItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMySubmissions()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat riwayat setoran (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getSubmissionDetail(id: Long): Flow<NetworkResult<TahsinSubmissionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getSubmissionDetail(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat detail setoran (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getAssignedSubmissions(status: String? = null): Flow<NetworkResult<List<TahsinSubmissionItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAssignedSubmissions(status)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar setoran siswa (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun annotate(submissionId: Long, timestampSeconds: Int, note: String, tajwidCategory: String?): Flow<NetworkResult<TahsinAnnotationItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.annotate(
                submissionId,
                TahsinAnnotateRequest(timestampSeconds, note, tajwidCategory)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal menyimpan catatan tajwid (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun markReviewed(submissionId: Long): Flow<NetworkResult<TahsinSubmissionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.markReviewed(submissionId)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal menandai evaluasi selesai (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}
