package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The teacher's class sessions of today, in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1300dp-xhdpi")
class TeacherSessionsScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val day = listOf(
        ClassSessionDto(
            sessionId = 11, scheduleId = 1, subjectName = "Fisika", classroomName = "XI MIPA 2", jamKe = 3,
            scheduledStart = "08:30:00", scheduledEnd = "10:00:00", actualStart = "08:31:00",
            status = ClassSessionStatus.ACTIVE, totalStudents = 32, presentCount = 27, absentCount = 5,
        ),
        ClassSessionDto(
            scheduleId = 2, subjectName = "Fisika", classroomName = "XI MIPA 3", jamKe = 5,
            scheduledStart = "10:15:00", scheduledEnd = "11:45:00", totalStudents = 30,
        ),
        ClassSessionDto(
            scheduleId = 3, subjectName = "Fisika", classroomName = "X-4", jamKe = 7,
            scheduledStart = "12:30:00", scheduledEnd = "14:00:00", totalStudents = 34,
        ),
        ClassSessionDto(
            sessionId = 9, scheduleId = 4, subjectName = "Fisika", classroomName = "XI MIPA 1", jamKe = 1,
            scheduledStart = "07:00:00", scheduledEnd = "08:30:00", actualStart = "07:02:00", actualEnd = "08:29:00",
            status = ClassSessionStatus.COMPLETED, totalStudents = 31, presentCount = 30, absentCount = 1,
        ),
    )

    private fun capture(name: String, state: TeacherTodaySessionsState, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                TeacherTodaySessionsContent(
                    state = state,
                    onRefresh = {},
                    onStart = { _, _ -> },
                    onOpen = {},
                    onOpenWeeklySchedule = {},
                    onNavigateBack = null,
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/sessions_$name.png")
    }

    // 10:10: Fisika XI MIPA 2 running, XI MIPA 3 can start, X-4 not open yet, XI MIPA 1 done.
    @Test fun today() = capture("today", TeacherTodaySessionsState(sessions = day, isLoading = false, nowMinutes = 10 * 60 + 10, todayLabel = "Senin, 28 September"))

    @Test fun todayDark() = capture("today_dark", TeacherTodaySessionsState(sessions = day, isLoading = false, nowMinutes = 10 * 60 + 10, todayLabel = "Senin, 28 September"), dark = true)

    @Test fun empty() = capture("empty", TeacherTodaySessionsState(isLoading = false, todayLabel = "Sabtu, 3 Oktober"))

    @Test fun notDeployed() = capture("unavailable", TeacherTodaySessionsState(isLoading = false, notDeployed = true))

    private val live = ClassSessionDto(
        sessionId = 11, scheduleId = 1, subjectName = "Fisika", classroomName = "XI MIPA 2", jamKe = 3,
        scheduledStart = "08:30:00", scheduledEnd = "10:00:00", actualStart = "08:31:00",
        status = ClassSessionStatus.ACTIVE, totalStudents = 32, presentCount = 27, absentCount = 5,
    )

    private fun captureSession(name: String, state: TeacherActiveSessionState) {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                TeacherActiveSessionContent(
                    state = state,
                    onRetry = {},
                    onOpenAttendanceList = {},
                    onEndConfirmed = { _, _ -> },
                    onDismissTimeUp = {},
                    onContinueAfterTimeUp = {},
                    onOpenTeachingJournal = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/session_$name.png")
    }

    private val counts = com.sultanagung1.sista.data.model.ClassSessionRules.Counts(
        present = 27, alpha = 5, total = 32,
        breakdown = com.sultanagung1.sista.data.model.ClassSessionRules.Counts.Breakdown(hadir = 24, telat = 3, sakit = 0, izin = 0),
    )

    // QR fresh with 18 s left, 42 minutes of class to go.
    @Test fun live() = captureSession(
        "live",
        TeacherActiveSessionState(
            sessionId = 11, session = live, qrToken = "3f2a9c1e-7b1d-4a55-9d0e-2c1b7e6f9a10.b64signature",
            qrExpiresAtMs = 18_000, nowMs = 0, remainingSeconds = 42 * 60, counts = counts, isLoading = false,
        ),
    )

    @Test fun overtime() = captureSession(
        "overtime",
        TeacherActiveSessionState(
            sessionId = 11, session = live, qrToken = null, qrExpiresAtMs = 0, nowMs = 120_000,
            remainingSeconds = -3 * 60, counts = counts, isLoading = false,
        ),
    )

    @Test fun finished() = captureSession(
        "finished",
        TeacherActiveSessionState(
            sessionId = 11,
            session = live.copy(
                status = ClassSessionStatus.COMPLETED, actualEnd = "09:58:00",
                topic = "Gerak parabola", teachingJournalId = 77,
            ),
            counts = counts, isLoading = false,
        ),
    )
}
