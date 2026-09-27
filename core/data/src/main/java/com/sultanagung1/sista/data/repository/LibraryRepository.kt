package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.LibraryApiService
import com.sultanagung1.sista.data.model.BookItem
import com.sultanagung1.sista.data.model.BookLoanItem
import com.sultanagung1.sista.data.model.BorrowBookRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LibraryRepository(private val apiService: LibraryApiService) {

    fun getBooks(query: String? = null, category: String? = null): Flow<NetworkResult<List<BookItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getBooks(query, category)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat katalog perpustakaan" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi perpustakaan"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMyLoans(): Flow<NetworkResult<List<BookLoanItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMyLoans()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat riwayat peminjaman" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi perpustakaan"))
        }
    }.flowOn(Dispatchers.IO)

    fun borrowBookByQr(qrCode: String, studentId: String? = null): Flow<NetworkResult<BookLoanItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.borrowBookByQr(BorrowBookRequest(qrCode, studentId))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Gagal meminjam buku (Kode: ${response.code()})", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat meminjam buku"))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            com.google.gson.JsonParser.parseString(errorBody).asJsonObject.get("message")?.asString
        } catch (e: Exception) {
            null
        }
    }
}
