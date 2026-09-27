package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.BookItem
import com.sultanagung1.sista.data.model.BookLoanItem
import com.sultanagung1.sista.data.model.BorrowBookRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApiService {

    @GET("library/books")
    suspend fun getBooks(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null
    ): Response<List<BookItem>>

    @GET("library/books/{id}")
    suspend fun getBookDetail(@Path("id") bookId: Long): Response<BookItem>

    @POST("library/self-checkout")
    suspend fun borrowBookByQr(@Body request: BorrowBookRequest): Response<BookLoanItem>

    @GET("library/my-loans")
    suspend fun getMyLoans(): Response<List<BookLoanItem>>
}
