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

data class UtbkRecommendationResponse(
    @SerializedName("estimated_score") val estimatedScore: Double,
    @SerializedName("recommendations") val recommendations: List<PtnRecommendationItem> = emptyList(),
    @SerializedName("message") val message: String? = null // e.g. "Anda belum memiliki skor tryout." when estimatedScore is 0
)

data class PtnRecommendationItem(
    @SerializedName("university") val university: String,
    @SerializedName("program") val program: String,
    @SerializedName("category") val category: String, // "Saintek" or "Soshum"
    @SerializedName("passing_grade") val passingGrade: Double,
    @SerializedName("acceptance_chance") val acceptanceChance: String // "Tinggi", "Sedang", "Rendah"
)

data class AlumniCampusItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("graduation_year") val graduationYear: String, // "Angkatan 2024"
    @SerializedName("university") val university: String,
    @SerializedName("major") val major: String? = null, // null until the alumnus has a tracer-study response on file
    @SerializedName("admission_path") val admissionPath: String? = null, // not tracked yet, always null for now
    @SerializedName("contact_available") val contactAvailable: Boolean = true
)
