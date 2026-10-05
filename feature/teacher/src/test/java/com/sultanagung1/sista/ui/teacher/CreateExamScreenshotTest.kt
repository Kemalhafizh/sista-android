package com.sultanagung1.sista.ui.teacher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The teacher's exam form in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h2600dp-xhdpi")
class CreateExamScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val physics = ExamClassChoice(7, "Fisika", 12, "XI MIPA 2")
    private val unnamed = ExamClassChoice(9, null, 14, null)

    private fun capture(name: String, state: TeacherCreateExamUiState) {
        compose.setContent {
            AppLocaleContent {
                TeacherCreateExamForm(uiState = state, showLivePreview = false, onToggleLivePreview = {}, actions = CreateExamActions())
            }
        }
        compose.onRoot().captureRoboImage("screenshots/create_exam_$name.png")
    }

    private val filled = TeacherCreateExamUiState(
        isLoadingChoices = false,
        classChoices = listOf(physics, unnamed),
        selectedChoice = physics,
        title = "Ulangan Harian Bab 3: Hukum Newton",
        durationMinutes = "60",
        passingScore = "75",
        questions = listOf(
            DraftQuestion(
                localId = "q1",
                text = "Benda bermassa 2 kg didorong gaya 10 N. Berapa percepatannya?",
                options = listOf(DraftOption("A", "2 m/s²"), DraftOption("B", "5 m/s²"), DraftOption("C", "20 m/s²")),
                correctKey = "B",
            ),
            DraftQuestion(localId = "q2", text = "Hukum Newton III menyatakan…"),
        ),
    )

    @Test fun blank() = capture("blank", TeacherCreateExamUiState(isLoadingChoices = false, classChoices = listOf(physics, unnamed)))

    @Test fun filledIn() = capture("filled", filled)

    // The second question is incomplete: the form lists what to fix instead of publishing.
    @Test fun needsFixing() = capture("issues", filled.copy(showValidation = true, activeQuestionIndex = 1))

    @Test fun noSchedule() = capture("no_schedule", TeacherCreateExamUiState(isLoadingChoices = false))

    @Test fun refusedByServer() = capture("refused", filled.copy(submitError = "Tidak ada tahun ajaran aktif."))

    @Test @Config(qualifiers = "en-w400dp-h2600dp-xhdpi")
    fun needsFixingEnglish() = capture("issues_en", filled.copy(showValidation = true, activeQuestionIndex = 1))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h2600dp-xhdpi")
    fun filledArabic() = capture("filled_ar", filled)
}
