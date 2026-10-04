package com.sultanagung1.sista.ui.academic

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.GradeEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The student's grades in their states. The grades below exist only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h860dp-xhdpi")
class GradesScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val grades = listOf(
        GradeEntry(1, "Matematika Wajib", "UH", 82.0, "Barisan dan deret", "2026-09-02"),
        GradeEntry(2, "Matematika Wajib", "UTS", 88.5, null, "2026-09-20"),
        GradeEntry(3, "Fisika", "UH", 76.0, "Gerak parabola", "2026-09-05"),
        GradeEntry(4, "Fisika", "PRAKTIK", 90.0, "Praktikum bandul", "2026-09-15"),
        GradeEntry(5, "Bahasa Indonesia", "UH", 91.0, "Teks eksposisi", "2026-09-10"),
    )

    private fun capture(name: String, data: List<GradeEntry> = grades, loading: Boolean = false, error: String? = null, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                GradesContent(
                    grades = data,
                    loading = loading,
                    errorMessage = error,
                    refreshing = false,
                    onRefresh = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/grades_$name.png")
    }

    @Test fun list() = capture("list")
    @Test fun listDark() = capture("list_dark", dark = true)
    @Test fun offline() = capture("offline", error = "Tidak ada koneksi internet.")
    @Test fun empty() = capture("empty", data = emptyList())
    @Test fun loading() = capture("loading", data = emptyList(), loading = true)
}
