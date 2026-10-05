package com.sultanagung1.sista.ui.cbt

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.core.security.ExamViolationRecord
import com.sultanagung1.sista.core.security.ExamViolationType
import com.sultanagung1.sista.data.model.CbtExamItem
import com.sultanagung1.sista.data.model.CbtOptionItem
import com.sultanagung1.sista.data.model.CbtQuestionItem
import com.sultanagung1.sista.data.model.TokenValidationState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The student's exam list, token gate and exam room, in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1100dp-xhdpi")
class CbtScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val exams = listOf(
        CbtExamItem(
            id = 1, title = "UTS Fisika Kelas XI", subject = "Fisika", type = "UTS", durationMinutes = 90, totalQuestions = 30,
            passingScore = 75.0, startsAt = "2026-10-05T07:30:00+07:00", endsAt = "2026-10-05T09:30:00+07:00",
            availabilityCode = "ongoing", isOngoing = true,
        ),
        CbtExamItem(
            id = 2, title = "PH 3 Hukum Newton", subject = "Fisika", type = "ULANGAN_HARIAN", durationMinutes = 45, totalQuestions = 15,
            passingScore = 70.0, startsAt = "2026-10-07T09:00:00+07:00", endsAt = "2026-10-07T10:00:00+07:00",
            availabilityCode = "upcoming",
        ),
        CbtExamItem(
            id = 3, title = "Ulangan Kimia Stoikiometri", subject = "Kimia", type = "ULANGAN_HARIAN", durationMinutes = 60, totalQuestions = 20,
            startsAt = "2026-10-01T07:30:00+07:00", endsAt = "2026-10-01T09:00:00+07:00",
            availabilityCode = "ended", hasAttempted = true, attemptStatus = "graded", score = 86.5,
        ),
        CbtExamItem(
            id = 4, title = "UTS Matematika", subject = "Matematika", type = "UTS", durationMinutes = 90,
            availabilityCode = "ongoing", isOngoing = true, hasAttempted = true, attemptStatus = "force_closed",
        ),
    )

    private fun list(name: String, exams: List<CbtExamItem>, loaded: Boolean = true, error: String? = null, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) {
                CbtExamListContent(
                    exams = exams, loaded = loaded, isLoading = !loaded, errorMessage = error, refreshing = false,
                    onRefresh = {}, onOpen = {}, onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/cbt_list_$name.png")
    }

    @Test fun listLoaded() = list("loaded", exams)

    @Test fun listDark() = list("dark", exams, dark = true)

    @Test fun listLoading() = list("loading", emptyList(), loaded = false)

    @Test fun listEmpty() = list("empty", emptyList())

    @Test fun listError() = list("error", emptyList(), error = "Tidak bisa terhubung ke server. Periksa koneksi internet Anda.")

    @Test @Config(qualifiers = "en-w400dp-h1100dp-xhdpi")
    fun listEnglish() = list("en", exams)

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1100dp-xhdpi")
    fun listArabic() = list("ar", exams)

    private fun token(name: String, token: String, validation: TokenValidationState, exam: CbtExamItem? = exams.first(), dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) {
                CbtTokenEntryContent(
                    exam = exam, examLoading = false, token = token, validation = validation,
                    onTokenChange = {}, onSubmit = {}, onNavigateBack = {}, requestFocus = false,
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/cbt_token_$name.png")
    }

    @Test fun tokenTyped() = token("typed", "K7X4M2", TokenValidationState.Idle)

    @Test fun tokenDark() = token("dark", "K7X4M2", TokenValidationState.Idle, dark = true)

    @Test fun tokenRefused() = token(
        "refused", "K7X4M9",
        TokenValidationState.Error("Token tidak valid. Periksa kembali kode token dari pengawas ujian."),
    )

    @Test fun tokenChecking() = token("checking", "K7X4M2", TokenValidationState.Loading)

    @Test fun tokenExamUnknown() = token("unknown_exam", "", TokenValidationState.Idle, exam = null)

    @Test @Config(qualifiers = "en-w400dp-h1100dp-xhdpi")
    fun tokenEnglish() = token("en", "K7X4", TokenValidationState.Idle)

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1100dp-xhdpi")
    fun tokenArabic() = token("ar", "K7X4", TokenValidationState.Idle)

    private val questions = listOf(
        CbtQuestionItem(
            id = 11, number = 1, questionText = "Sebuah benda bermassa 2 kg didorong gaya 10 N. Berapa percepatannya?",
            options = listOf(
                CbtOptionItem("A", "2 m/s²"), CbtOptionItem("B", "5 m/s²"), CbtOptionItem("C", "10 m/s²"),
                CbtOptionItem("D", "20 m/s²"), CbtOptionItem("E", "0,2 m/s²"),
            ),
        ),
    ) + (2..12).map { n ->
        CbtQuestionItem(id = 10L + n, number = n, questionText = "Soal $n", options = listOf(CbtOptionItem("A", "Ya"), CbtOptionItem("B", "Tidak")))
    }

    private val answers = mapOf("11" to "B", "12" to "A", "13" to "B", "15" to "A")

    private fun room(
        name: String,
        questions: List<CbtQuestionItem> = this.questions,
        remaining: Long? = 3909,
        violations: Int = 0,
        loading: Boolean = false,
        error: String? = null,
        dark: Boolean = false,
    ) {
        compose.setContent {
            AppLocaleContent(dark) {
                CbtExamRoomContent(
                    title = "UTS Fisika Kelas XI", questions = questions, currentIndex = 0, answers = answers,
                    remainingSeconds = remaining, violationCount = violations, maxViolations = 3, batteryLevel = 64,
                    isLoading = loading, errorMessage = error, onRetry = {}, onSelect = { _, _ -> }, onGoTo = {},
                    onSubmit = {}, onExit = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/cbt_room_$name.png")
    }

    @Test fun roomQuestion() = room("question")

    @Test fun roomDark() = room("dark", dark = true)

    @Test fun roomLastMinutes() = room("last_minutes", remaining = 45, violations = 2)

    @Test fun roomClockUnknown() = room("clock_unknown", remaining = null)

    @Test fun roomLoading() = room("loading", questions = emptyList(), loading = true)

    @Test fun roomError() = room(
        "error", questions = emptyList(),
        error = "Ujian belum dibuka atau waktu ujian telah berakhir.",
    )

    @Test @Config(qualifiers = "en-w400dp-h1100dp-xhdpi")
    fun roomEnglish() = room("en")

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1100dp-xhdpi")
    fun roomArabic() = room("ar")

    @Test fun roomLocked() {
        compose.setContent {
            AppLocaleContent {
                ExamLockedPanel(
                    maxViolations = 3,
                    history = listOf(
                        ExamViolationRecord(type = ExamViolationType.WINDOW_FOCUS_LOST, violationNumber = 1),
                        ExamViolationRecord(type = ExamViolationType.SCREENSHOT_ATTEMPT, violationNumber = 2),
                        ExamViolationRecord(type = ExamViolationType.MULTI_WINDOW_SPLIT, violationNumber = 3),
                    ),
                    onLeave = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/cbt_room_locked.png")
    }
}
