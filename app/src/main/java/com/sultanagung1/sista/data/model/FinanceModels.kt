package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class BillingInvoice(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("formatted_amount") val formattedAmount: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("status") val status: String, // PAID, UNPAID, PENDING
    @SerializedName("payment_channel") val paymentChannel: String? = null,
    @SerializedName("va_number") val vaNumber: String? = null
)

data class PaymentVaResponse(
    @SerializedName("status") val status: String,
    @SerializedName("invoice_id") val invoiceId: Long,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("va_number") val vaNumber: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("expiry_time") val expiryTime: String
)
