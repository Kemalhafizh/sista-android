package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.OcrScanResult
import com.sultanagung1.sista.data.model.OcrScanStartResponse
import com.sultanagung1.sista.data.model.SchoolDocumentItem
import com.sultanagung1.sista.data.model.SignatureSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DocumentRepository(private val apiClient: ApiClient) {

    fun getReportCards(): Flow<NetworkResult<List<SchoolDocumentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.documentApi.getReportCards()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar rapor (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getCertificates(): Flow<NetworkResult<List<SchoolDocumentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.documentApi.getCertificates()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar sertifikat (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Uploads the image and returns the queued task id — the OCR job runs
     * async, see pollOcrResult(). Takes raw bytes (read by the caller via
     * ContentResolver) rather than a File, since a picked content:// Uri
     * isn't guaranteed to resolve to a real filesystem path.
     */
    fun startOcrScan(imageBytes: ByteArray, fileName: String, mimeType: String, documentType: String): Flow<NetworkResult<OcrScanStartResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val imagePart = MultipartBody.Part.createFormData(
                "document_image", fileName, imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            val typePart = documentType.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiClient.documentApi.processOcrImage(imagePart, typePart)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mengunggah dokumen untuk OCR (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Polls GET documents/ocr/{taskId} every 1.5s until the async job reports
     * "completed" or "failed" (or [maxAttempts] is reached), then emits the
     * final result once. The extraction itself is a backend-side simulated
     * placeholder — see OcrScanResult's docs — but this polling loop and the
     * status it reflects are real.
     */
    fun pollOcrResult(taskId: Long, maxAttempts: Int = 20): Flow<NetworkResult<OcrScanResult>> = flow {
        emit(NetworkResult.Loading)
        try {
            repeat(maxAttempts) { attempt ->
                val response = apiClient.documentApi.getOcrResult(taskId)
                val data = response.body()?.data
                if (!response.isSuccessful || data == null) {
                    emit(NetworkResult.Error(response.body()?.message ?: "Gagal memeriksa status OCR (Kode: ${response.code()}).", response.code()))
                    return@flow
                }
                if (data.status == "completed" || data.status == "failed") {
                    emit(NetworkResult.Success(data))
                    return@flow
                }
                if (attempt < maxAttempts - 1) delay(1500)
            }
            emit(NetworkResult.Error("Proses OCR memakan waktu lebih lama dari perkiraan. Coba periksa lagi nanti."))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun submitDigitalSignature(payload: SignatureSubmission): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.documentApi.submitDigitalSignature(payload)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal menyimpan tanda tangan digital (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}
