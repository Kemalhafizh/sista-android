package com.sultanagung1.sista.ui.teacher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.StudentAttendanceInputItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Class attendance for one lesson, in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
class TeacherAttendanceScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val roster = listOf(
        StudentAttendanceInputItem(1, "Adinda Putri Maharani", nis = "22101"),
        StudentAttendanceInputItem(2, "Bagas Pratama", nis = "22102", status = "S"),
        StudentAttendanceInputItem(3, "Citra Lestari", nis = "22103"),
        StudentAttendanceInputItem(4, "Dimas Anggara", nisn = "0081234567", status = "A"),
        StudentAttendanceInputItem(5, "Eka Nur Rahmawati", nis = "22105", status = "I"),
        StudentAttendanceInputItem(6, "Fajar Setiawan"),
    )

    private fun capture(name: String, state: TeacherAttendanceUiState, dark: Boolean = false, className: String = "XI MIPA 2") {
        compose.setContent {
            AppLocaleContent(dark) {
                TeacherAttendanceContent(
                    className = className,
                    state = state,
                    onRetry = {},
                    onMark = { _, _ -> },
                    onMarkAllPresent = {},
                    onSave = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/class_attendance_$name.png")
    }

    private val loaded = TeacherAttendanceUiState(loading = false, students = roster)

    @Test fun marking() = capture("marking", loaded)

    @Test fun markingDark() = capture("marking_dark", loaded, dark = true)

    @Test fun loading() = capture("loading", TeacherAttendanceUiState())

    @Test fun empty() = capture("empty", TeacherAttendanceUiState(loading = false))

    // The server refuses a class the teacher does not teach this year, in its own words.
    @Test fun refused() = capture(
        "refused",
        TeacherAttendanceUiState(loading = false, loadError = "Anda tidak mengajar di kelas ini pada tahun ajaran aktif."),
    )

    @Test fun saveFailed() = capture("save_failed", loaded.copy(submitError = "Tahun akademik aktif tidak ditemukan"), className = "")

    @Test @Config(qualifiers = "en-w400dp-h1300dp-xhdpi")
    fun markingEnglish() = capture("marking_en", loaded)

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1300dp-xhdpi")
    fun markingArabic() = capture("marking_ar", loaded)
}
