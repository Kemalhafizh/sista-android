package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class QuestionBankCategory(
    val id: Long,
    val uuid: String? = null,
    val name: String,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    @SerializedName("curriculum_ref") val curriculumRef: String? = null,
    val description: String? = null,
    @SerializedName("items_count") val itemsCount: Int = 0,
    // How many of the items are mudah / sedang / sulit, counted by the server.
    @SerializedName("easy_count") val easyCount: Int? = null,
    @SerializedName("medium_count") val mediumCount: Int? = null,
    @SerializedName("hard_count") val hardCount: Int? = null,
)

data class QuestionBankItem(
    val id: Long,
    val uuid: String? = null,
    @SerializedName("category_id") val categoryId: Long,
    val type: String = "pilihan_ganda",
    @SerializedName("question_text") val questionText: String,
    val options: Any? = null,
    @SerializedName("correct_answer") val correctAnswer: String,
    val explanation: String? = null,
    // mudah, sedang or sulit; null when the item has none.
    val difficulty: String? = null,
    @SerializedName("cognitive_level") val cognitiveLevel: String? = null,
    @SerializedName("is_validated") val isValidated: Boolean = false,
    @SerializedName("usage_count") val usageCount: Int = 0
)

data class CreateQuestionBankRequest(
    @SerializedName("category_id") val categoryId: Long,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    val difficulty: String = "sedang",
    @SerializedName("cognitive_level") val cognitiveLevel: String = "C3",
    val type: String = "pilihan_ganda",
    val explanation: String? = null,
    val options: List<String>? = null
)

data class AutoGenerateExamRequest(
    @SerializedName("category_id") val categoryId: Long?,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("exam_id") val examId: Long? = null,
    @SerializedName("difficulty_distribution") val difficultyDistribution: Map<String, Int> = mapOf(
        "mudah" to 30,
        "sedang" to 50,
        "sulit" to 20
    )
)

data class AutoGenerateExamResponse(
    @SerializedName("total_generated") val totalGenerated: Int,
    val items: List<QuestionBankItem> = emptyList(),
    val distribution: Map<String, Int> = emptyMap()
)

data class QuestionBankResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T
)
