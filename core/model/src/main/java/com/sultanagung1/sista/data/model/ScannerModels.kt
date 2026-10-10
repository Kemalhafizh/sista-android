package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

// Library scan mode reuses the existing real BorrowBookRequest/BookLoanItem
// (LibraryModels.kt) via LibraryRepository.borrowBookByQr() — no separate
// model needed here.

// POST mobile/scanner/event-ticket — real EventTicketingService::scanTicket
data class EventTicketScanRequest(
    @SerializedName("ticket_code") val ticketCode: String
)

data class EventTicketScanResult(
    val valid: Boolean = false,
    val message: String? = null,
    val attendeeName: String? = null,
    val ticketType: String? = null,
    val eventName: String? = null,
    val venue: String? = null,
    val eventDatetime: String? = null
)

// POST mobile/scanner/visitor-pass — real VisitorService::scanBadge
data class VisitorPassScanRequest(
    @SerializedName("badge_number") val badgeNumber: String
)

data class VisitorPassScanResult(
    val valid: Boolean = false,
    val message: String? = null,
    val visitorName: String? = null,
    val purpose: String? = null,
    val status: String? = null,
    val checkInTime: String? = null,
    val checkOutTime: String? = null
)
