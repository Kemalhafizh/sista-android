package com.sultanagung1.sista.ui.academic

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.ScheduleItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The student timetable in its states. Record with
 * `./gradlew :feature:academic:recordRoborazziDebug`; images land in
 * feature/academic/screenshots. The lessons below exist only in this test.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h860dp-xhdpi")
class ScheduleScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun lesson(id: Long, day: String, start: String, end: String, subject: String, teacher: String, room: String?) =
        ScheduleItem(id, day, start, end, subject, teacher, "XI MIPA 2", room)

    private val week = listOf(
        lesson(1, "Senin", "07:00:00", "08:30:00", "Matematika Wajib", "Bu Rahma", "R. 2.03"),
        lesson(2, "Senin", "08:30:00", "10:00:00", "Fisika", "Pak Hendra", "Lab Fisika"),
        lesson(3, "Senin", "10:15:00", "11:45:00", "Bahasa Indonesia", "Bu Sari", "R. 2.03"),
        lesson(4, "Senin", "12:30:00", "14:00:00", "Pendidikan Agama Islam", "Ust. Fauzan", null),
        lesson(5, "Selasa", "07:00:00", "08:30:00", "Kimia", "Bu Dewi", "Lab Kimia"),
    )

    private fun capture(
        name: String,
        schedules: List<ScheduleItem> = week,
        day: String = "Senin",
        nowMinutes: Int = 9 * 60,
        loading: Boolean = false,
        error: String? = null,
        dark: Boolean = false,
    ) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                ScheduleContent(
                    schedules = schedules,
                    selectedDay = day,
                    todayName = "Senin",
                    nowMinutes = nowMinutes,
                    isLoading = loading,
                    errorMessage = error,
                    refreshing = false,
                    onSelectDay = {},
                    onRefresh = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/schedule_$name.png")
    }

    // 09:00 on Monday: Fisika is on, the rest are still to come.
    @Test fun today() = capture("today")

    @Test fun todayDark() = capture("today_dark", dark = true)

    // In the break: the next lesson is marked, the first two are done.
    @Test fun breakTime() = capture("break", nowMinutes = 10 * 60 + 5)

    @Test fun otherDayOffline() = capture("offline", day = "Selasa", error = "Tidak ada koneksi internet.")

    @Test fun dayWithoutLessons() = capture("free_day", day = "Kamis")

    @Test fun firstLoadFailed() = capture("error", schedules = emptyList(), error = "Server tidak menjawab.")
}
