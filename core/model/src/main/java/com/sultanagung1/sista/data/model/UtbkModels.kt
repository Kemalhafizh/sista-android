package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class UtbkTryoutItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("subtest_name") val subtestName: String, // "Tes Potensi Skolastik (TPS)", "Penalaran Matematika", "Literasi B. Indonesia", "Literasi B. Inggris"
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("status") val status: String, // "READY", "ONGOING", "COMPLETED"
    @SerializedName("irt_score") val irtScore: Double? = null,
    @SerializedName("national_percentile") val nationalPercentile: Double? = null
)

data class UtbkQuestion(
    @SerializedName("id") val id: Long,
    @SerializedName("subtest_id") val subtestId: Long,
    @SerializedName("number") val number: Int,
    @SerializedName("passage") val passage: String? = null,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("correct_answer_index") val correctAnswerIndex: Int? = null,
    @SerializedName("irt_difficulty_theta") val irtDifficultyTheta: Double = 0.0
)

data class MajorRecommendationItem(
    @SerializedName("university_name") val universityName: String, // e.g. "Universitas Gadjah Mada (UGM)"
    @SerializedName("major_name") val majorName: String, // e.g. "Kedokteran", "Teknik Informatika"
    @SerializedName("category") val category: String, // "SAINTEK" or "SOSHUM"
    @SerializedName("pass_probability") val passProbability: Int, // 85%
    @SerializedName("probability_badge") val probabilityBadge: String, // "TINGGI (Hijau)", "SEDANG (Kuning)", "KETAT (Merah)"
    @SerializedName("kktp_report_alignment") val kktpReportAlignment: String, // "Sangat Selaras dengan Nilai Biologi & Kimia (94.2)"
    @SerializedName("historical_alumni_count") val historicalAlumniCount: Int = 12
)

data class AlumniCampusItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("graduation_year") val graduationYear: String, // "Angkatan 2024"
    @SerializedName("university") val university: String,
    @SerializedName("major") val major: String,
    @SerializedName("admission_path") val admissionPath: String, // "SNBP", "SNBT", "Mandiri"
    @SerializedName("contact_available") val contactAvailable: Boolean = true
)
