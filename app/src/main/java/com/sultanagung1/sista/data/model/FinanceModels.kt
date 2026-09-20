package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class BillingInvoice(
    @SerializedName("id") val id: Long,
    @SerializedName("category") val category: String,
    @SerializedName("month") val month: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("amount_formatted") val amountFormatted: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("status") val status: String, // e.g. paid, unpaid, pending — matches Billing.status values
    @SerializedName("remaining_balance") val remainingBalance: Double,
    @SerializedName("payment_url") val paymentUrl: String? = null
) {
    /** No "title" field exists server-side — this is the closest honest label built from real data. */
    val displayTitle: String get() = "$category — $month"
}

data class PaymentVaResponse(
    @SerializedName("billing_id") val billingId: Long,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("amount_formatted") val amountFormatted: String,
    @SerializedName("bank") val bank: String,
    @SerializedName("va_number") val vaNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("expiry_time") val expiryTime: String,
    @SerializedName("qr_content") val qrContent: String
)
