package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/*
 * Mirrors DisciplineService (GET/POST api/v1/discipline/...) exactly. The
 * previous models used field names the server never sent (point_status,
 * is_signed_by_parent, letter_number, ...) and a request it never accepted
 * (signature_data), so every student read "Predikat: BAIK" with 0 points and
 * no parent could ever sign a warning letter.
 *
 * Points follow the server's convention: penalties are positive, rewards
 * negative; [DisciplineSummary.totalPoints] is the net balance that
 * triggers SP1/SP2/SP3.
 */
data class DisciplineSummary(
    @SerializedName("total_points") val totalPoints: Int = 0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("total_violation_points") val totalViolationPoints: Int = 0,
    @SerializedName("total_reward_points") val totalRewardPoints: Int = 0,
    @SerializedName("total_cases") val totalCases: Int = 0,
    // Null once SP3 has been reached.
    @SerializedName("next_warning_level") val nextWarningLevel: String? = null,
    @SerializedName("next_warning_threshold") val nextWarningThreshold: Int? = null,
    @SerializedName("last_updated") val lastUpdated: String? = null,
    @SerializedName("rules") val rules: List<DisciplineRule> = emptyList(),
    // Whose numbers these are — a parent may have several children.
    @SerializedName("student") val student: DisciplineStudent? = null
)

data class DisciplineStudent(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("classroom") val classroom: String? = null
)

/** An active point category from the school's rule book; [points] is a positive magnitude. */
data class DisciplineRule(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String, // penalty | reward
    @SerializedName("points") val points: Int
)

data class DisciplineRecord(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("points") val points: Int = 0,
    @SerializedName("type") val type: String? = null, // penalty | reward | adjustment
    // Null for automatic entries such as an Alpa penalty from attendance.
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("recorded_by") val recordedBy: String? = null
)

data class WarningLetterItem(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("level") val level: String? = null, // SP1 | SP2 | SP3
    @SerializedName("point_threshold") val pointThreshold: Int = 0,
    @SerializedName("issued_at") val issuedAt: String? = null,
    @SerializedName("is_signed") val isSigned: Boolean = false,
    @SerializedName("signed_at") val signedAt: String? = null,
    @SerializedName("notes") val notes: String? = null,
    // True only for this student's own parent while unsigned — the same rule
    // the sign endpoint enforces.
    @SerializedName("can_sign") val canSign: Boolean = false
)

/** The signer is the authenticated parent account; only the drawn signature is sent. */
data class SignWarningLetterRequest(
    @SerializedName("digital_signature") val digitalSignature: String // base64 PNG from captureSignatureAsBase64Png
)
