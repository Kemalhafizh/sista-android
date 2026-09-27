package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class AchievementItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String, // "Juara 1 Olimpiade Sains Nasional (OSN) Tingkat Provinsi Jawa Tengah"
    @SerializedName("field") val field: String, // "Matematika & Sains", "Keagamaan & Tahfidz", "Olahraga", "Seni Budaya"
    @SerializedName("level") val level: String, // "KOTA", "PROVINSI", "NASIONAL", "INTERNASIONAL"
    @SerializedName("organizer") val organizer: String, // "Kemendikbudristek RI / BPTI"
    @SerializedName("date") val date: String,
    @SerializedName("verification_status") val verificationStatus: String, // "VALIDATED", "PENDING_REVIEW", "REJECTED"
    @SerializedName("certificate_url") val certificateUrl: String? = null,
    @SerializedName("points_earned") val pointsEarned: Int = 0
)

/**
 * NOTE: blockchain_hash is not a real blockchain transaction hash — the
 * backend (App\Services\Blockchain\BlockchainCredentialService) generates it
 * locally as `'0x' . hash('sha256', ...)` with no actual blockchain network
 * involved (its own code even names the variable $mockTxHash). Flagged for a
 * product decision on whether to keep presenting it as "blockchain" or be
 * explicit that it's a local verification hash — left as-is (out of scope
 * for this pass) rather than unilaterally reworking that subsystem.
 */
data class CertificateItem(
    @SerializedName("id") val id: Long,
    @SerializedName("certificate_number") val certificateNumber: String, // "CERT/SA1/2026/089"
    @SerializedName("title") val title: String, // "Sertifikat Kepanitiaan Sultan Agung Fest 2026"
    @SerializedName("role") val role: String, // "Koordinator Divisi IT & Publikasi"
    @SerializedName("issued_date") val issuedDate: String,
    @SerializedName("blockchain_hash") val blockchainHash: String? = null
)

data class AcademicCvSummary(
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("nisn") val nisn: String? = null,
    @SerializedName("gpa_average") val gpaAverage: Double? = null,
    @SerializedName("total_achievements") val totalAchievements: Int = 0,
    @SerializedName("total_reward_points") val totalRewardPoints: Int = 0,
    @SerializedName("extracurriculars") val extracurriculars: List<String> = emptyList()
)

data class UploadAchievementRequest(
    @SerializedName("title") val title: String,
    @SerializedName("field") val field: String,
    @SerializedName("level") val level: String,
    @SerializedName("organizer") val organizer: String,
    @SerializedName("date") val date: String,
    @SerializedName("image_base64") val imageBase64: String
)
