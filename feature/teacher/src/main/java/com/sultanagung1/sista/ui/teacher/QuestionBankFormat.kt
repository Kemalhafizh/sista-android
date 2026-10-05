package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.teacher.R

/** Wording and colour for the question bank's difficulty codes. */
object QuestionBankFormat {

    /** `question_bank_items.difficulty`, easiest first. */
    val DIFFICULTIES = listOf("mudah", "sedang", "sulit")

    /** Presets for the preview's difficulty mix, in percent of mudah/sedang/sulit. */
    val MIXES: List<Map<String, Int>> = listOf(
        mapOf("mudah" to 30, "sedang" to 50, "sulit" to 20),
        mapOf("mudah" to 50, "sedang" to 40, "sulit" to 10),
        mapOf("mudah" to 20, "sedang" to 40, "sulit" to 40),
    )

    fun difficulty(code: String?): UiText = when (code) {
        "mudah" -> UiText.Res(R.string.qb_easy)
        "sedang" -> UiText.Res(R.string.qb_medium)
        "sulit" -> UiText.Res(R.string.qb_hard)
        null -> UiText.Res(R.string.qb_no_difficulty)
        else -> UiText.Raw(code)
    }

    fun difficultyTone(code: String?): StatusTone = when (code) {
        "mudah" -> StatusTone.Success
        "sedang" -> StatusTone.Warning
        "sulit" -> StatusTone.Danger
        else -> StatusTone.Neutral
    }

    /** "30 / 50 / 20" for a mix, always with Latin digits. */
    fun mixLabel(mix: Map<String, Int>): String = DIFFICULTIES.joinToString(" / ") { (mix[it] ?: 0).toString() }
}
