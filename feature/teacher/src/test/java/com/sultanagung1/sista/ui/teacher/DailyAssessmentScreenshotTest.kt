package com.sultanagung1.sista.ui.teacher

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AssessmentScoreRow
import com.sultanagung1.sista.data.model.AssessmentScoreSheet
import com.sultanagung1.sista.data.model.AssessmentSheetHeader
import com.sultanagung1.sista.data.model.DailyAssessmentItem
import com.sultanagung1.sista.data.model.NamedRef
import com.sultanagung1.sista.data.model.RemedialDashboard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Daily assessments: the list and the score sheet. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1300dp-xhdpi")
class DailyAssessmentScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun item(id: Long, title: String, cls: String, date: String, scored: Int, total: Int, kkm: Double = 75.0) =
        DailyAssessmentItem(
            id = id, title = title, assessmentDate = date, kkm = kkm, scoredCount = scored,
            subject = NamedRef(5, "Fisika"), classroom = NamedRef(id, cls, studentsCount = total),
        )

    private val assessments = listOf(
        item(1, "UH 2 Hukum Newton", "XI MIPA 2", "2026-09-28", 12, 32),
        item(2, "Kuis Gerak Parabola", "XI MIPA 1", "2026-09-24", 31, 31),
        item(3, "UH 1 Besaran dan Satuan", "X-4", "2026-09-15", 0, 34, kkm = 70.0),
    )

    private fun list(name: String, items: List<DailyAssessmentItem>, loading: Boolean = false, error: String? = null, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                DailyAssessmentContent(
                    assessments = items,
                    remedial = RemedialDashboard(total = 9, pending = 4, completed = 5).takeIf { items.isNotEmpty() },
                    isLoading = loading,
                    errorMessage = error,
                    refreshing = false,
                    onRefresh = {},
                    onOpen = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/assessments_$name.png")
    }

    @Test fun listLoaded() = list("list", assessments)

    @Test fun listDark() = list("list_dark", assessments, dark = true)

    @Test fun listEmpty() = list("empty", emptyList())

    @Test fun listError() = list("error", emptyList(), error = "Tidak dapat terhubung ke server.")

    private val sheet = AssessmentScoreSheet(
        assessment = AssessmentSheetHeader(
            id = 1, title = "UH 2 Hukum Newton", assessmentDate = "2026-09-28", kkm = 75.0, maxScore = 100.0,
            subjectName = "Fisika", classroomName = "XI MIPA 2",
        ),
        students = listOf(
            AssessmentScoreRow(1, "Adinda Putri Maharani", nis = "22101", score = 88.0),
            AssessmentScoreRow(2, "Bagas Pratama", nis = "22102", score = 64.5),
            AssessmentScoreRow(3, "Citra Lestari", nis = "22103", score = null),
            AssessmentScoreRow(4, "Dimas Anggara", nis = "22104", score = null),
            AssessmentScoreRow(5, "Eka Nur Rahmawati", nis = "22105", score = 92.0),
            AssessmentScoreRow(6, "Fajar Setiawan", nis = "22106", score = null),
        ),
    )

    private fun sheet(name: String, edits: Map<Long, String>, success: String? = null, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                ScoreInputContent(
                    sheet = sheet,
                    loading = false,
                    loadError = null,
                    edits = edits,
                    submitting = false,
                    errorMessage = null,
                    successMessage = success,
                    onEdit = { _, _ -> },
                    onSave = {},
                    onAutoRemedial = {},
                    onRetry = {},
                    onDismissMessage = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/scores_$name.png")
    }

    // Citra typed (unsaved), Dimas out of range, Bagas's stored score shown.
    @Test fun sheetEditing() = sheet("editing", mapOf(3L to "79", 4L to "105"))

    @Test fun sheetEditingDark() = sheet("editing_dark", mapOf(3L to "79", 4L to "105"), dark = true)

    @Test fun sheetSaved() = sheet("saved", emptyMap(), success = "2 nilai tersimpan. 1 siswa di bawah KKM.")

    @Test fun sheetLoadFailed() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                ScoreInputContent(
                    sheet = null, loading = false, loadError = "Penilaian ini bukan milik Anda.", edits = emptyMap(),
                    submitting = false, errorMessage = null, successMessage = null, onEdit = { _, _ -> }, onSave = {},
                    onAutoRemedial = {}, onRetry = {}, onDismissMessage = {}, onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/scores_error.png")
    }
}
