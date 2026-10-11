package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * POST counseling/bullying-reports (BullyingReportApiController::store).
 * Anyone signed in may report. An anonymous report keeps no trace of who
 * sent it; [incidentDate] is an ISO date (yyyy-MM-dd) not in the future.
 */
data class BullyingReportRequest(
    @SerializedName("description") val description: String,
    @SerializedName("incident_date") val incidentDate: String? = null,
    @SerializedName("location") val location: String? = null,
    /** One of BullyingReport::TYPES, or null when the reporter doesn't say. */
    @SerializedName("type") val type: String? = null,
    @SerializedName("is_anonymous") val isAnonymous: Boolean = true,
)

/** The report as the server stored it; labels are already in the user's language. */
data class BullyingReportItem(
    @SerializedName("id") val id: Long,
    @SerializedName("ticket_number") val ticketNumber: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("type_code") val typeCode: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("incident_date") val incidentDate: String? = null,
    @SerializedName("is_anonymous") val isAnonymous: Boolean = true,
    @SerializedName("status") val status: String? = null,
    @SerializedName("status_code") val statusCode: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)
