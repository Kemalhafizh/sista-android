package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class DisciplineSummary(
    @SerializedName("total_violation_points") val totalViolationPoints: Int = 0,
    @SerializedName("total_reward_points") val totalRewardPoints: Int = 0,
    @SerializedName("point_status") val pointStatus: String? = "BAIK", // BAIK, PERINGATAN, SP1, SP2, SP3, KRITIS
    @SerializedName("max_allowed_points") val maxAllowedPoints: Int = 100,
    @SerializedName("total_cases") val totalCases: Int = 0
)

data class DisciplineRecord(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String? = null,
    @SerializedName("category") val category: String? = null, // "VIOLATION" or "REWARD"
    @SerializedName("points") val points: Int = 0,
    @SerializedName("date") val date: String? = null,
    @SerializedName("recorded_by") val recordedBy: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("action_taken") val actionTaken: String? = null
)

data class WarningLetterItem(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("letter_number") val letterNumber: String? = null,
    @SerializedName("level") val level: String? = null, // "SP1", "SP2", "SP3"
    @SerializedName("issued_date") val issuedDate: String? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("total_points_at_issue") val totalPointsAtIssue: Int = 0,
    @SerializedName("is_signed_by_parent") val isSignedByParent: Boolean = false,
    @SerializedName("parent_signed_at") val parentSignedAt: String? = null,
    @SerializedName("pdf_download_url") val pdfDownloadUrl: String? = null
)

data class SignWarningLetterRequest(
    @SerializedName("letter_id") val letterId: Long,
    @SerializedName("signature_data") val signatureData: String, // Base64 SVG/PNG or HMAC
    @SerializedName("parent_name") val parentName: String,
    @SerializedName("parent_phone") val parentPhone: String
)
