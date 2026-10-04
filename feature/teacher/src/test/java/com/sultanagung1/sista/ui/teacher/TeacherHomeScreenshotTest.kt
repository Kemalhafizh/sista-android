package com.sultanagung1.sista.ui.teacher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.model.TeachingJournalEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The teacher's Beranda in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1600dp-xhdpi")
class TeacherHomeScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val slots = listOf(
        TeacherScheduleSlot(1, "Senin", "07:00:00", "08:30:00", 5, "Fisika", 21, "XI MIPA 1", "Lab Fisika"),
        TeacherScheduleSlot(2, "Senin", "08:30:00", "10:00:00", 5, "Fisika", 22, "XI MIPA 2", "R. 204"),
        TeacherScheduleSlot(3, "Senin", "10:15:00", "11:45:00", 5, "Fisika", 23, "XI MIPA 3", null),
    )

    private val journals = listOf(
        journal("a", "2026-09-25", "XI MIPA 2", "Hukum Newton II", 30, 2, "reviewed"),
        journal("b", "2026-09-24", "X-4", "Besaran dan satuan", 33, 1, "submitted"),
        journal("c", "2026-09-24", "XI MIPA 1", "Gerak parabola", 29, 2, "draft"),
    )

    private val running = listOf(
        ClassSessionDto(
            sessionId = 11, scheduleId = 2, subjectName = "Fisika", classroomName = "XI MIPA 2", jamKe = 3,
            scheduledStart = "08:30:00", scheduledEnd = "10:00:00", actualStart = "08:31:00",
            status = ClassSessionStatus.ACTIVE, totalStudents = 32, presentCount = 27, absentCount = 5,
        ),
    )

    private val shortcuts = TEACHER_SHORTCUTS.take(6)

    private fun capture(
        name: String,
        state: TeacherUiState,
        dark: Boolean = false,
        tiles: List<TeacherShortcut> = shortcuts,
        greeting: String = "Selamat pagi",
    ) {
        compose.setContent {
            AppLocaleContent(dark) {
                TeacherHomeContent(
                    greeting = greeting,
                    state = state,
                    shortcuts = tiles,
                    refreshing = false,
                    onRefresh = {},
                    onOpenRoute = {},
                    onAttendance = {},
                    onJournal = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/teacher_home_$name.png")
    }

    private val loaded = TeacherUiState(
        teacherName = "Siti Rahmawati, S.Pd.",
        nip = "198703122010012004",
        totalClasses = 6,
        teachingHoursThisWeek = 22.5,
        todaySchedules = slots,
        recentJournals = journals,
        classSessions = running,
        classSessionsAvailable = true,
        nowMinutes = 9 * 60 + 5,
    )

    @Test fun teaching() = capture("teaching", loaded)

    @Test fun teachingDark() = capture("teaching_dark", loaded, dark = true)

    @Test @Config(qualifiers = "en-w400dp-h1600dp-xhdpi")
    fun teachingEnglish() = capture("teaching_en", loaded, greeting = "Good morning")

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1600dp-xhdpi")
    fun teachingArabic() = capture("teaching_ar", loaded, greeting = "صباح الخير")

    @Test fun loading() = capture("loading", TeacherUiState(isLoading = true, teacherName = "Siti Rahmawati, S.Pd.", nip = "198703122010012004"))

    @Test fun noClassesToday() = capture(
        "free_day",
        loaded.copy(todaySchedules = emptyList(), classSessions = emptyList(), recentJournals = journals.take(1)),
        tiles = shortcuts.take(3),
    )

    @Test fun loadFailed() = capture(
        "error",
        TeacherUiState(teacherName = "Siti Rahmawati, S.Pd.", errorMessage = "Tidak dapat terhubung ke server."),
        tiles = emptyList(),
    )

    private fun journal(id: String, date: String, cls: String, topic: String, present: Int, absent: Int, status: String) =
        TeachingJournalEntry(
            uuid = id, teachingDate = date, jamKe = 3, classroomId = 1, classroomName = cls, subjectId = 5,
            subjectName = "Fisika", topic = topic, learningActivity = null, learningMethod = null,
            studentsPresent = present, studentsAbsent = absent, status = status,
        )
}
