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
    @SerializedName("points_earned") val pointsEarned: Int = 25
)

data class CertificateItem(
    @SerializedName("id") val id: Long,
    @SerializedName("certificate_number") val certificateNumber: String, // "CERT/SA1/2026/089"
    @SerializedName("title") val title: String, // "Sertifikat Kepanitiaan Sultan Agung Fest 2026"
    @SerializedName("role") val role: String, // "Koordinator Divisi IT & Publikasi"
    @SerializedName("issued_date") val issuedDate: String,
    @SerializedName("blockchain_hash") val blockchainHash: String = "0x7f8a9b2c3d4e5f6a1b2c3d4e5f6a7b8c9d0e1f2a"
)

data class AcademicCvSummary(
    @SerializedName("student_name") val studentName: String,
    @SerializedName("nisn") val nisn: String,
    @SerializedName("gpa_average") val gpaAverage: Double = 92.4,
    @SerializedName("total_achievements") val totalAchievements: Int = 4,
    @SerializedName("total_reward_points") val totalRewardPoints: Int = 40,
    @SerializedName("extracurriculars") val extracurriculars: List<String> = listOf("Rohis Sultan Agung 1", "Klub Robotik")
)

data class UploadAchievementRequest(
    @SerializedName("title") val title: String,
    @SerializedName("field") val field: String,
    @SerializedName("level") val level: String,
    @SerializedName("organizer") val organizer: String,
    @SerializedName("date") val date: String,
    @SerializedName("image_base64") val imageBase64: String
)
