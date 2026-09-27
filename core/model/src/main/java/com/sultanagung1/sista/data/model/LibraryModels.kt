package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class BookItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("isbn") val isbn: String,
    @SerializedName("category") val category: String, // "Sains & Teknologi", "Keislaman & Tarbiyah", "Sastra & Bahasa", "Persiapan UTBK"
    @SerializedName("shelf_location") val shelfLocation: String, // "Rak A-04 (Lt. 1)"
    @SerializedName("synopsis") val synopsis: String,
    @SerializedName("available_copies") val availableCopies: Int,
    @SerializedName("total_copies") val totalCopies: Int,
    @SerializedName("rating") val rating: Double = 4.8,
    @SerializedName("is_available") val isAvailable: Boolean = true
)

data class BookLoanItem(
    @SerializedName("id") val id: Long,
    @SerializedName("book_id") val bookId: Long,
    @SerializedName("book_title") val bookTitle: String,
    @SerializedName("borrow_date") val borrowDate: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("days_remaining") val daysRemaining: Int, // e.g. 2 days left
    @SerializedName("status") val status: String, // "ACTIVE", "OVERDUE", "RETURNED"
    @SerializedName("fine_amount") val fineAmount: Long = 0
)

data class BorrowBookRequest(
    @SerializedName("book_qr_code") val bookQrCode: String,
    @SerializedName("student_id") val studentId: String
)
