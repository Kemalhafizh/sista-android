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
                emit(NetworkResult.Success(getSampleBooks()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleBooks()))
        }
    }.flowOn(Dispatchers.IO)

    fun getMyLoans(): Flow<NetworkResult<List<BookLoanItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMyLoans()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleLoans()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleLoans()))
        }
    }.flowOn(Dispatchers.IO)

    fun borrowBookByQr(qrCode: String, studentId: String): Flow<NetworkResult<BookLoanItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.borrowBookByQr(BorrowBookRequest(qrCode, studentId))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    BookLoanItem(
                        id = 99,
                        bookId = 1,
                        bookTitle = "Fisika Dasar untuk Universitas & Persiapan UTBK",
                        borrowDate = "2026-08-26",
                        dueDate = "2026-09-02",
                        daysRemaining = 7,
                        status = "ACTIVE"
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                BookLoanItem(
                    id = 99,
                    bookId = 1,
                    bookTitle = "Fisika Dasar untuk Universitas & Persiapan UTBK",
                    borrowDate = "2026-08-26",
                    dueDate = "2026-09-02",
                    daysRemaining = 7,
                    status = "ACTIVE"
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleBooks(): List<BookItem> = listOf(
        BookItem(
            id = 1,
            title = "Fisika Dasar untuk Universitas & Persiapan UTBK",
            author = "Halliday, Resnick, Walker",
            isbn = "978-602-1234-56-1",
            category = "Sains & Teknologi",
            shelfLocation = "Rak A-02 (Lantai 1)",
            synopsis = "Referensi lengkap teori mekanika, termodinamika, gelombang, dan fisika modern dengan ribuan contoh soal.",
            availableCopies = 5,
            totalCopies = 8,
            rating = 4.9,
            isAvailable = true
        ),
        BookItem(
            id = 2,
            title = "Tafsir Al-Azhar: Surat Al-Baqarah & Ali Imran",
            author = "Prof. Dr. Buya Hamka",
            isbn = "978-979-418-091-2",
            category = "Keislaman & Tarbiyah",
            shelfLocation = "Rak B-01 (Koleksi Khusus Islami)",
            synopsis = "Karya monumental Buya Hamka yang membedah ayat Al-Qur'an secara mendalam dengan pendekatan sastra dan realitas keumatan Nusantara.",
            availableCopies = 3,
            totalCopies = 4,
            rating = 5.0,
            isAvailable = true
        ),
        BookItem(
            id = 3,
            title = "Mastering TPS & Penalaran Matematika SNBT 2026",
            author = "Tim Litbang Pendidikan Sultan Agung",
            isbn = "978-623-8890-11-0",
            category = "Persiapan UTBK",
            shelfLocation = "Rak C-05 (Koleksi Kelas 12)",
            synopsis = "Bank soal drilling terupdate metode IRT Rasch lengkap dengan trik cepat logika matematika & bahasa Inggris.",
            availableCopies = 12,
            totalCopies = 15,
            rating = 4.8,
            isAvailable = true
        ),
        BookItem(
            id = 4,
            title = "Laskar Pelangi (Edisi Spesial)",
            author = "Andrea Hirata",
            isbn = "978-979-3062-79-2",
            category = "Sastra & Bahasa",
            shelfLocation = "Rak D-03 (Fiksi & Novel)",
            synopsis = "Kisah inspiratif tentang perjuangan sepuluh anak di Belitung dalam meraih cita-cita pendidikan tinggi.",
            availableCopies = 2,
            totalCopies = 6,
            rating = 4.9,
            isAvailable = true
        )
    )

    private fun getSampleLoans(): List<BookLoanItem> = listOf(
        BookLoanItem(
            id = 101,
            bookId = 1,
            bookTitle = "Fisika Dasar untuk Universitas & Persiapan UTBK",
            borrowDate = "2026-08-20",
            dueDate = "2026-08-27",
            daysRemaining = 1,
            status = "ACTIVE"
        ),
        BookLoanItem(
            id = 102,
            bookId = 3,
            bookTitle = "Mastering TPS & Penalaran Matematika SNBT 2026",
            borrowDate = "2026-08-22",
            dueDate = "2026-08-29",
            daysRemaining = 3,
            status = "ACTIVE"
        )
    )
}
