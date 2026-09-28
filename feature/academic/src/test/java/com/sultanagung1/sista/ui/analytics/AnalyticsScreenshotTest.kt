package com.sultanagung1.sista.ui.analytics

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AtRiskStudentItem
import com.sultanagung1.sista.data.model.ClassAnalyticsData
import com.sultanagung1.sista.data.model.DailyAttendanceHeatmapItem
import com.sultanagung1.sista.data.model.ParentProgressData
import com.sultanagung1.sista.data.model.ScoreDistributionBucket
import com.sultanagung1.sista.data.model.SemesterTrendPoint
import com.sultanagung1.sista.data.model.StudentAnalyticsData
import com.sultanagung1.sista.data.model.SubjectAverage
import com.sultanagung1.sista.data.model.TeacherClassOption
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** The student, teacher and parent analytics screens in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1400dp-xhdpi")
class AnalyticsScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() } }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    // ── Student ──

    private val student = StudentAnalyticsData(
        studentName = "Nadia Putri Rahma", className = "XI MIPA 2", overallAverage = 82.4, attendanceRate = 96.5, attendanceRecorded = 57,
        semesterTrends = listOf(
            SemesterTrendPoint("2025/2026 Ganjil", 80.6f, rankInClass = 7),
            SemesterTrendPoint("2025/2026 Genap", 83.1f, rankInClass = 4),
        ),
        subjects = listOf(
            SubjectAverage("Bahasa Indonesia", 86.0, 75.0, 4), SubjectAverage("Biologi", 88.5, 75.0, 3),
            SubjectAverage("Fisika", 88.0, 75.0, 5), SubjectAverage("Kimia", 79.3, 75.0, 3),
            SubjectAverage("Matematika", 71.5, 78.0, 6), SubjectAverage("Sejarah", 84.0, 70.0, 2),
            SubjectAverage("Bahasa Inggris", 81.0, 75.0, 3),
        ),
    )

    private fun studentScreen(name: String, state: StudentAnalyticsUiState, dark: Boolean = false) = capture("student_analytics_$name", dark) {
        StudentAnalyticsContent(state, refreshing = false, onRefresh = {}, onRetry = {}, onNavigateBack = {})
    }

    @Test fun studentLoaded() = studentScreen("loaded", StudentAnalyticsUiState(data = student))

    @Test fun studentDark() = studentScreen("dark", StudentAnalyticsUiState(data = student), dark = true)

    @Test fun studentNothingYet() = studentScreen(
        "empty",
        StudentAnalyticsUiState(data = StudentAnalyticsData(studentName = "Raka Aditya", className = "X-3", attendanceRecorded = 0, subjects = emptyList())),
    )

    @Test fun studentError() = studentScreen("error", StudentAnalyticsUiState(errorMessage = "Data siswa tidak ditemukan untuk akun ini."))

    // ── Teacher ──

    private val classes = listOf(
        TeacherClassOption(11, 3, "XI MIPA 2", "Fisika", "2026/2027"),
        TeacherClassOption(12, 3, "XI MIPA 3", "Fisika", "2026/2027"),
        TeacherClassOption(21, 3, "X-4", "Fisika", "2026/2027"),
    )

    private val performance = ClassAnalyticsData(
        classroomId = 11, subjectId = 3, className = "XI MIPA 2", subjectName = "Fisika", teacherName = "Bu Siti",
        classAverage = 80.2, highestScore = 98, lowestScore = 52, passRatePercentage = 81.3, kkm = 75.0,
        scoresCount = 96, studentsGraded = 31, studentsCount = 32,
        distributionBuckets = listOf(
            ScoreDistributionBucket("< 75", 18, 18.8f), ScoreDistributionBucket("75 - 84", 41, 42.7f),
            ScoreDistributionBucket("85 - 92", 26, 27.1f), ScoreDistributionBucket("93 - 100", 11, 11.5f),
        ),
        atRiskStudents = listOf(
            AtRiskStudentItem("7", "Dimas Prasetyo", "Fisika", 61, 75),
            AtRiskStudentItem("9", "Putri Anggraini", "Fisika", 68, 75),
            AtRiskStudentItem("4", "Bagas Saputra", "Fisika", 72, 75),
        ),
    )

    private fun classScreen(name: String, state: ClassAnalyticsUiState, dark: Boolean = false) = capture("class_analytics_$name", dark) {
        ClassAnalyticsContent(state, refreshing = false, onRefresh = {}, onRetry = {}, onSelect = {}, onNavigateBack = {})
    }

    @Test fun classLoaded() = classScreen("loaded", ClassAnalyticsUiState(classes = classes, selected = classes[0], performance = performance))

    @Test fun classDark() = classScreen("dark", ClassAnalyticsUiState(classes = classes, selected = classes[0], performance = performance), dark = true)

    @Test fun classNotGradedYet() = classScreen(
        "no_grades",
        ClassAnalyticsUiState(
            classes = classes, selected = classes[2],
            performance = ClassAnalyticsData(classroomId = 21, subjectId = 3, className = "X-4", subjectName = "Fisika", scoresCount = 0, studentsCount = 30),
        ),
    )

    @Test fun classNone() = classScreen("no_classes", ClassAnalyticsUiState(classes = emptyList()))

    @Test fun classError() = classScreen("error", ClassAnalyticsUiState(classesError = "Tidak dapat terhubung ke server."))

    // ── Parent ──

    /** Thursday 10 Sep 2026. */
    private val today = LocalDate.of(2026, 9, 10)

    private fun heatmap(vararg statuses: String) =
        statuses.mapIndexed { index, status -> DailyAttendanceHeatmapItem(index + 1, "", status) }

    private val child = ParentProgressData(
        childName = "Nadia Putri Rahma", childClass = "XI MIPA 2",
        academicScore = 82.4, classAverageScore = 79.1, tahfidzCurrentJuz = 3, tahfidzTargetJuz = 5, totalSurahCompleted = 41,
        // Tue 1 … Thu 10 Sep: weekends have no record.
        attendanceHeatmap = heatmap("HADIR", "HADIR", "HADIR", "SAKIT", "KOSONG", "KOSONG", "HADIR", "IZIN", "HADIR", "HADIR"),
    )

    private fun childScreen(name: String, state: ChildProgressUiState, dark: Boolean = false) = capture("child_progress_$name", dark) {
        ChildProgressContent(state, today, refreshing = false, onRefresh = {}, onRetry = {}, onNavigateBack = {})
    }

    @Test fun childLoaded() = childScreen("loaded", ChildProgressUiState(data = child))

    @Test fun childDark() = childScreen("dark", ChildProgressUiState(data = child), dark = true)

    @Test fun childNothingYet() = childScreen(
        "empty",
        ChildProgressUiState(
            data = ParentProgressData(childName = "Raka Aditya", childClass = "X-3", attendanceHeatmap = heatmap(*Array(10) { "KOSONG" })),
        ),
    )

    @Test fun childNotFound() = childScreen("error", ChildProgressUiState(errorMessage = "Data anak tidak ditemukan untuk akun ini."))
}
