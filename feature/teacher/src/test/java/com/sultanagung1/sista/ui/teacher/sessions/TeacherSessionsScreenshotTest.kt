package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import java.time.LocalDate
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
import android.content.res.Configuration
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sultanagung1.sista.core.accessibility.AppLocale
import com.sultanagung1.sista.core.accessibility.AppLanguage

/** The teacher's class sessions of today, in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
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

    private fun show(dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent {
            // The app runs Arabic as "ar-u-nu-latn" (Latin digits, AppLocale.localeOf) and its
            // manifest sets supportsRtl; a qualifier can carry neither, so apply both here.
            val config = LocalConfiguration.current
            val rtl = config.locales[0].language == "ar"
            val context = LocalContext.current
            val appContext = remember(rtl) {
                if (!rtl) context else context.createConfigurationContext(
                    Configuration(config).apply { setLocale(AppLocale.localeOf(AppLanguage.ARABIC)) }
                )
            }
            CompositionLocalProvider(
                LocalContext provides appContext,
                LocalConfiguration provides appContext.resources.configuration,
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() }
            }
        }
    }

    private fun capture(name: String, state: TeacherTodaySessionsState, dark: Boolean = false) {
        show(dark) {
            run {
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
    @Test fun today() = capture("today", TeacherTodaySessionsState(sessions = day, isLoading = false, nowMinutes = 10 * 60 + 10, today = LocalDate.of(2026, 9, 28)))

    @Test fun todayDark() = capture("today_dark", TeacherTodaySessionsState(sessions = day, isLoading = false, nowMinutes = 10 * 60 + 10, today = LocalDate.of(2026, 9, 28)), dark = true)

    @Test fun empty() = capture("empty", TeacherTodaySessionsState(isLoading = false, today = LocalDate.of(2026, 10, 3)))

    @Test fun notDeployed() = capture("unavailable", TeacherTodaySessionsState(isLoading = false, notDeployed = true))

    private val live = ClassSessionDto(
        sessionId = 11, scheduleId = 1, subjectName = "Fisika", classroomName = "XI MIPA 2", jamKe = 3,
        scheduledStart = "08:30:00", scheduledEnd = "10:00:00", actualStart = "08:31:00",
        status = ClassSessionStatus.ACTIVE, totalStudents = 32, presentCount = 27, absentCount = 5,
    )

    private fun captureSession(name: String, state: TeacherActiveSessionState) {
        show {
            run {
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

    private val rows = listOf(
        "Aisyah Putri Rahmadani" to com.sultanagung1.sista.data.model.SessionAttendanceStatus.HADIR,
        "Bagas Aditya Nugroho" to com.sultanagung1.sista.data.model.SessionAttendanceStatus.TELAT,
        "Citra Maharani" to com.sultanagung1.sista.data.model.SessionAttendanceStatus.ALPHA,
        "Dimas Prasetyo" to com.sultanagung1.sista.data.model.SessionAttendanceStatus.HADIR,
        "Evi Nurhaliza" to com.sultanagung1.sista.data.model.SessionAttendanceStatus.ALPHA,
    ).mapIndexed { i, (name, status) ->
        com.sultanagung1.sista.data.model.SessionAttendanceDto(
            id = i + 1L, studentId = i + 100L, studentName = name, studentNis = "2024${100 + i}",
            status = status,
            checkedInAt = if (status == com.sultanagung1.sista.data.model.SessionAttendanceStatus.ALPHA) null else "2026-09-28T08:3${i}:00+07:00",
            checkInMethod = if (status == com.sultanagung1.sista.data.model.SessionAttendanceStatus.ALPHA) com.sultanagung1.sista.data.model.SessionCheckInMethod.AUTO_ALPHA
            else com.sultanagung1.sista.data.model.SessionCheckInMethod.QR_SCAN,
        )
    }

    // Citra was marked Sakit by the teacher and not saved yet.
    @Test fun attendanceList() = attendanceList("attendance_list")

    private fun attendanceList(name: String) {
        show {
            run {
                TeacherAttendanceListContent(
                    state = TeacherAttendanceListState(
                        sessionId = 11, session = live, rows = rows, isLoading = false,
                        edits = mapOf(102L to com.sultanagung1.sista.data.model.SessionAttendanceStatus.SAKIT),
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    @Test @Config(qualifiers = "en-w400dp-h1300dp-xhdpi")
    fun todayEnglish() = capture("today_en", TeacherTodaySessionsState(sessions = day, isLoading = false, nowMinutes = 10 * 60 + 10, today = LocalDate.of(2026, 9, 28)))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1300dp-xhdpi")
    fun liveArabic() = captureSession(
        "live_ar",
        TeacherActiveSessionState(
            sessionId = 11, session = live, qrToken = "3f2a9c1e-7b1d-4a55-9d0e-2c1b7e6f9a10.b64signature",
            qrExpiresAtMs = 18_000, nowMs = 0, remainingSeconds = 42 * 60, counts = counts, isLoading = false,
        ),
    )

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1300dp-xhdpi")
    fun attendanceListArabic() = attendanceList("attendance_list_ar")
}
