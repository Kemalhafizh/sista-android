package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * One bill from GET student/billings (BillingResource). [month] and [dueDate]
 * are nullable columns on the server; nothing is filled in for them.
 */
data class BillingInvoice(
    @SerializedName("id") val id: Long,
    @SerializedName("category") val category: String? = null,
    @SerializedName("month") val month: String? = null,
    @SerializedName("amount") val amount: Double,
    @SerializedName("amount_formatted") val amountFormatted: String? = null,
    /** yyyy-MM-dd */
    @SerializedName("due_date") val dueDate: String? = null,
    /** unpaid / partial / paid (billings.status). */
    @SerializedName("status") val status: String,
    @SerializedName("remaining_balance") val remainingBalance: Double,
    @SerializedName("payment_url") val paymentUrl: String? = null
)
