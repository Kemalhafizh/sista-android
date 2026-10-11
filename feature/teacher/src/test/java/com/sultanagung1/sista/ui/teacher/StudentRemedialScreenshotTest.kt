package com.sultanagung1.sista.ui.teacher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.RemedialItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** "Remedial Saya" in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1100dp-xhdpi")
class StudentRemedialScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 10, 11)

    private val remedials = StudentRemedialRules.ordered(
        listOf(
            RemedialItem(
                id = 1, assessmentTitle = "UH 1 Dinamika", subjectName = "Fisika", originalScore = 60.0, kkm = 75.0,
                status = "pending", deadline = "2026-10-20T00:00:00+07:00", teacherNotes = "Kerjakan soal latihan bab 2 nomor 1–10.",
            ),
            RemedialItem(
                id = 2, assessmentTitle = "Kuis Larutan Penyangga", subjectName = "Kimia", originalScore = 55.5, kkm = 78.0,
                status = "pending", deadline = "2026-10-08T00:00:00+07:00",
            ),
            RemedialItem(
                id = 3, assessmentTitle = "UH Trigonometri", subjectName = "Matematika", originalScore = 62.0, kkm = null,
                remedialScore = 80.0, status = "completed",
            ),
        )
    )

    private fun capture(name: String, state: StudentRemedialUiState, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) { RemedialContent(state, today, onRetry = {}, onNavigateBack = {}) }
        }
        compose.onRoot().captureRoboImage("screenshots/student_remedial_$name.png")
    }

    @Test fun loaded() = capture("loaded", StudentRemedialUiState(isLoading = false, remedials = remedials))

    @Test fun loadedDark() = capture("loaded_dark", StudentRemedialUiState(isLoading = false, remedials = remedials), dark = true)

    @Test fun empty() = capture("empty", StudentRemedialUiState(isLoading = false, remedials = emptyList()))

    @Test fun loading() = capture("loading", StudentRemedialUiState())

    @Test fun error() = capture(
        "error",
        StudentRemedialUiState(isLoading = false, errorMessage = "Tidak bisa terhubung ke server. Periksa koneksi internet Anda."),
    )

    @Test fun refreshFailedKeepsTheList() = capture(
        "refresh_failed",
        StudentRemedialUiState(isLoading = false, remedials = remedials, errorMessage = "Tidak bisa terhubung ke server."),
    )

    @Test @Config(qualifiers = "en-w400dp-h1100dp-xhdpi")
    fun loadedEnglish() = capture("loaded_en", StudentRemedialUiState(isLoading = false, remedials = remedials))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1100dp-xhdpi")
    fun loadedArabic() = capture("loaded_ar", StudentRemedialUiState(isLoading = false, remedials = remedials))
}
