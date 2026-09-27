package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

// POST mobile/attendance/verify-qr — real dynamic-TOTP QR identity check
// (GeofenceAttendanceService::verifyDynamicTotpQr). This only confirms WHO
// the scanned QR belongs to; it does not itself write an Attendance row.
data class AttendanceQrVerifyRequest(
    @SerializedName("qr_token") val qrToken: String
)

data class AttendanceQrVerifyResult(
    val valid: Boolean = false,
    val message: String? = null,
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("user_role") val userRole: String? = null,
    @SerializedName("verified_at") val verifiedAt: String? = null
)

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
