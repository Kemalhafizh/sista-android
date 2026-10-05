package com.sultanagung1.sista.ui.teacher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AutoGenerateExamResponse
import com.sultanagung1.sista.data.model.QuestionBankCategory
import com.sultanagung1.sista.data.model.QuestionBankItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The question bank and its preview, in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1200dp-xhdpi")
class QuestionBankScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val categories = listOf(
        QuestionBankCategory(1, name = "Hukum Newton tentang gerak", gradeLevel = "F", curriculumRef = "CP Fisika F.2", itemsCount = 24, easyCount = 8, mediumCount = 11, hardCount = 5),
        QuestionBankCategory(2, name = "Gerak parabola", gradeLevel = "F", description = "Komponen kecepatan, titik tertinggi, dan jarak tempuh.", itemsCount = 9, easyCount = 2, mediumCount = 7, hardCount = 0),
        QuestionBankCategory(3, name = "Besaran dan satuan", itemsCount = 0),
    )

    private fun bank(name: String, state: QuestionBankUiState, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) { QuestionBankContent(state, onRetry = {}, onPreview = {}, onCreateExam = {}, onNavigateBack = {}) }
        }
        compose.onRoot().captureRoboImage("screenshots/question_bank_$name.png")
    }

    private val loaded = QuestionBankUiState(isLoading = false, categories = categories)

    @Test fun bankLoaded() = bank("list", loaded)

    @Test fun bankDark() = bank("list_dark", loaded, dark = true)

    @Test fun bankLoading() = bank("loading", QuestionBankUiState())

    @Test fun bankEmpty() = bank("empty", QuestionBankUiState(isLoading = false))

    @Test fun bankError() = bank("error", QuestionBankUiState(isLoading = false, errorMessage = "Tidak dapat terhubung ke server."))

    @Test @Config(qualifiers = "en-w400dp-h1200dp-xhdpi")
    fun bankEnglish() = bank("list_en", loaded)

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1200dp-xhdpi")
    fun bankArabic() = bank("list_ar", loaded)

    private val picked = AutoGenerateExamResponse(
        totalGenerated = 3,
        items = listOf(
            QuestionBankItem(11, categoryId = 1, questionText = "Sebuah benda bermassa 2 kg didorong dengan gaya 10 N. Berapa percepatannya?", correctAnswer = "B", difficulty = "mudah", cognitiveLevel = "C2"),
            QuestionBankItem(12, categoryId = 1, questionText = "Dua balok dihubungkan tali melalui katrol licin. Tentukan tegangan tali.", correctAnswer = "D", difficulty = "sedang", cognitiveLevel = "C3"),
            QuestionBankItem(13, categoryId = 1, questionText = "Rancang percobaan untuk membuktikan Hukum Newton III.", correctAnswer = "Esai", difficulty = null, cognitiveLevel = null),
        ),
    )

    private fun preview(name: String, state: QuestionBankUiState, form: QuestionPickForm = QuestionPickForm(categoryId = 1)) {
        compose.setContent {
            AppLocaleContent {
                AutoGenerateContent(state, form, onCategory = {}, onCount = {}, onMix = {}, onPick = {}, onNavigateBack = {})
            }
        }
        compose.onRoot().captureRoboImage("screenshots/question_preview_$name.png")
    }

    @Test fun previewForm() = preview("form", loaded)

    @Test fun previewPicked() = preview("picked", loaded.copy(generatedExam = picked))

    @Test fun previewInvalidCount() = preview("invalid", loaded, QuestionPickForm(count = "150"))

    @Test fun previewFailed() = preview("failed", loaded.copy(generateError = "Soal belum bisa dipilih dari bank soal (kode 500)."))

    @Test @Config(qualifiers = "en-w400dp-h1200dp-xhdpi")
    fun previewPickedEnglish() = preview("picked_en", loaded.copy(generatedExam = picked))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1200dp-xhdpi")
    fun previewPickedArabic() = preview("picked_ar", loaded.copy(generatedExam = picked))
}
