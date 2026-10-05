package com.sultanagung1.sista.ui.teacher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.data.model.CbtTokenInfoResponse
import com.sultanagung1.sista.data.model.LockedExamStudent
import com.sultanagung1.sista.data.model.TeacherCbtExamItem
import com.sultanagung1.sista.feature.teacher.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The proctor's exam list and exam screen, in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1100dp-xhdpi")
class ProctorScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val exams = listOf(
        TeacherCbtExamItem(1, "UTS Fisika Kelas XI", "uts", "Fisika", "XI MIPA 2", "2026-10-05T07:30:00+07:00", isOngoing = true),
        TeacherCbtExamItem(2, "PH 3 Hukum Newton", "ulangan_harian", "Fisika", "XI MIPA 1", "2026-10-07T09:00:00+07:00"),
        TeacherCbtExamItem(3, "Try Out UTBK 2", "try_out", null, null, null),
    )

    private fun list(name: String, state: TeacherProctorExamsUiState, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) { TeacherProctorExamsContent(state, onRefresh = {}, onOpen = {}, onNavigateBack = {}) }
        }
        compose.onRoot().captureRoboImage("screenshots/proctor_exams_$name.png")
    }

    private val loadedList = TeacherProctorExamsUiState(hasLoaded = true, exams = exams)

    @Test fun examList() = list("list", loadedList)

    @Test fun examListDark() = list("list_dark", loadedList, dark = true)

    @Test fun examListLoading() = list("loading", TeacherProctorExamsUiState(isLoading = true))

    @Test fun examListEmpty() = list("empty", TeacherProctorExamsUiState(hasLoaded = true))

    @Test fun examListError() = list("error", TeacherProctorExamsUiState(hasLoaded = true, errorMessage = "Tidak dapat terhubung ke server."))

    @Test @Config(qualifiers = "en-w400dp-h1100dp-xhdpi")
    fun examListEnglish() = list("list_en", loadedList)

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1100dp-xhdpi")
    fun examListArabic() = list("list_ar", loadedList)

    private val token = CbtTokenInfoResponse(examId = 1, title = "UTS Fisika Kelas XI", accessToken = "K7X4M2", remainingSeconds = 214, durationMinutes = 5)

    private val locked = listOf(
        LockedExamStudent(31, "Bagas Pratama", "22102", "XI MIPA 2", "split_screen", "Layar terbagi", 2),
        LockedExamStudent(34, "Dimas Anggara", "22104", "XI MIPA 2", "app_minimized", "Aplikasi diperkecil", 1),
    )

    private fun exam(name: String, state: CbtProctorUiState, remaining: Int? = state.token?.remainingSeconds, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) {
                TeacherProctorContent(
                    examId = 1,
                    state = state,
                    remainingSeconds = remaining,
                    onRenewToken = {},
                    onRetryToken = {},
                    onRefreshLocked = {},
                    onUnlock = {},
                    onDismissUnlockMessage = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/proctor_exam_$name.png")
    }

    private val live = CbtProctorUiState(token = token, lockedLoading = false, lockedStudents = locked)

    @Test fun examLive() = exam("live", live)

    @Test fun examLiveDark() = exam("live_dark", live, dark = true)

    @Test fun examLoading() = exam("loading", CbtProctorUiState(isLoading = true))

    @Test fun tokenExpiredNobodyLocked() = exam("expired", CbtProctorUiState(token = token.copy(isExpired = true), lockedLoading = false), remaining = 0)

    @Test fun tokenRefused() = exam(
        "refused",
        CbtProctorUiState(errorMessage = "Anda tidak berwenang melihat token ujian ini.", lockedLoading = false, lockedError = "Anda tidak berwenang mengawasi ujian ini."),
    )

    @Test fun studentLetBackIn() = exam(
        "unlocked",
        live.copy(lockedStudents = locked.drop(1), unlockMessage = UiText.Res(R.string.pr_unlocked, "Bagas Pratama")),
    )

    @Test @Config(qualifiers = "en-w400dp-h1100dp-xhdpi")
    fun examLiveEnglish() = exam("live_en", live.copy(lockedStudents = locked.map { it.copy(reason = "Split screen") }))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1100dp-xhdpi")
    fun examLiveArabic() = exam("live_ar", live.copy(lockedStudents = locked.map { it.copy(reason = "تقسيم الشاشة") }))
}
