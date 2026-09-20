package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class DisciplineSummary(
    @SerializedName("total_violation_points") val totalViolationPoints: Int = 0,
    @SerializedName("total_reward_points") val totalRewardPoints: Int = 0,
    @SerializedName("point_status") val pointStatus: String = "BAIK", // BAIK, PERINGATAN, SP1, SP2, SP3, KRITIS
    @SerializedName("max_allowed_points") val maxAllowedPoints: Int = 100,
    @SerializedName("total_cases") val totalCases: Int = 0
)

data class DisciplineRecord(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String, // "VIOLATION" or "REWARD"
    @SerializedName("points") val points: Int,
    @SerializedName("date") val date: String,
    @SerializedName("recorded_by") val recordedBy: String,
    @SerializedName("description") val description: String,
    @SerializedName("action_taken") val actionTaken: String? = null
)

data class WarningLetterItem(
    @SerializedName("id") val id: Long,
    @SerializedName("letter_number") val letterNumber: String,
    @SerializedName("level") val level: String, // "SP1", "SP2", "SP3"
    @SerializedName("issued_date") val issuedDate: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("total_points_at_issue") val totalPointsAtIssue: Int,
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
