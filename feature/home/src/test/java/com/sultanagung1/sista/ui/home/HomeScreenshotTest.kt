package com.sultanagung1.sista.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.ui.home.sections.STUDENT_QUICK_ITEMS
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The student's Beranda in its states. The data below exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1000dp-xhdpi")
class HomeScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = listOf(
        ScheduleItem(1, "Senin", "07:00:00", "08:30:00", "Matematika Wajib", "Bu Rahma", "XI MIPA 2", "R. 2.03"),
        ScheduleItem(2, "Senin", "08:30:00", "10:00:00", "Fisika", "Pak Hendra", "XI MIPA 2", "Lab Fisika"),
        ScheduleItem(3, "Senin", "10:15:00", "11:45:00", "Bahasa Indonesia", "Bu Sari", "XI MIPA 2", "R. 2.03"),
    )

    private fun capture(
        name: String,
        schedules: List<ScheduleItem> = today,
        activeClass: ActiveClassSessionDto? = null,
        usage: Map<String, Int> = emptyMap(),
        loading: Boolean = false,
        error: String? = null,
        dark: Boolean = false,
        greeting: String = "Selamat pagi",
    ) {
        compose.setContent {
            // The app's manifest sets supportsRtl; this module's test manifest doesn't,
            // so take the direction from the locale like the app does.
            val rtl = LocalConfiguration.current.locales[0].language == "ar"
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    HomeContent(
                        greeting = greeting,
                        name = "Aisyah Putri Rahmadani",
                        classroom = "XI MIPA 2",
                        identifier = "0071234567",
                        unreadCount = 3,
                        activeClass = activeClass,
                        todaySchedules = schedules,
                        nowMinutes = 9 * 60,
                        isLoading = loading,
                        errorMessage = error,
                        quickItems = STUDENT_QUICK_ITEMS,
                        usage = usage,
                        canScanClassQr = true,
                        refreshing = false,
                        onRefresh = {},
                        onOpenRoute = {},
                        onOpenQuickItem = {},
                        onResetUsage = {},
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage("screenshots/home_$name.png")
    }

    @Test fun morning() = capture("morning")

    @Test fun morningDark() = capture("morning_dark", dark = true)

    @Test fun classRunning() = capture(
        "class_running",
        activeClass = ActiveClassSessionDto(sessionId = 7, subjectName = "Fisika", canScanQr = true),
    )

    // After the student opened Tagihan and Nilai most, those come first, labelled "Sering Dipakai".
    @Test fun personalised() = capture("frequent", usage = mapOf("student.billing" to 6, "student.grades" to 4))

    @Test fun firstLoad() = capture("loading", schedules = emptyList(), loading = true)

    @Test fun noLessons() = capture("free_day", schedules = emptyList())

    // Names, subjects and rooms come from the server as they are; only the app's own text changes.
    @Test @Config(qualifiers = "en-w400dp-h1000dp-xhdpi")
    fun english() = capture("en", greeting = "Good morning", activeClass = ActiveClassSessionDto(sessionId = 7, subjectName = "Fisika", canScanQr = true))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1000dp-xhdpi")
    fun arabic() = capture("ar", greeting = "صباح الخير", usage = mapOf("student.billing" to 6, "student.grades" to 4))
}
