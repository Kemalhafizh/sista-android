package com.sultanagung1.sista.ui.analytics

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AttendanceBreakdown
import com.sultanagung1.sista.data.model.AttendanceDay
import com.sultanagung1.sista.data.model.ExecutiveAnalyticsData
import com.sultanagung1.sista.data.model.ExecutiveAttendance
import com.sultanagung1.sista.data.model.ExecutiveSpp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The executive analytics screen in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
class ExecutiveScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun day(date: String, recorded: Int, present: Int) = AttendanceDay(date, recorded, present, present * 100.0 / recorded)

    private val loaded = ExecutiveAnalyticsData(
        totalActiveStudents = 1043,
        attendance = ExecutiveAttendance(
            today = AttendanceBreakdown(recorded = 4120, present = 3871, sick = 118, permit = 76, absent = 55, rate = 94.0),
            days = listOf(
                day("2026-09-17", 4096, 3912), day("2026-09-18", 4102, 3860), day("2026-09-21", 4110, 3950),
                day("2026-09-22", 4108, 3901), day("2026-09-23", 4115, 3622), day("2026-09-24", 4090, 3884),
                day("2026-09-25", 4101, 3897), day("2026-09-28", 4118, 3925), day("2026-09-29", 4112, 3868),
                day("2026-09-30", 4120, 3871),
            ),
        ),
        spp = ExecutiveSpp(
            month = "September 2026", billed = 521_500_000.0, settled = 402_250_000.0, settledRate = 77.1,
            bills = 1043, billsPaid = 796, receivedThisMonth = 212_400_000.0, receivedLastMonth = 188_900_000.0, receivedChangePct = 12.4,
        ),
    )

    private fun capture(name: String, state: ExecutiveUiState, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                ExecutiveAnalyticsContent(state = state, refreshing = false, onRefresh = {}, onRetry = {}, onNavigateBack = {})
            }
        }
        compose.onRoot().captureRoboImage("screenshots/executive_$name.png")
    }

    @Test fun loaded() = capture("loaded", ExecutiveUiState(data = loaded))

    @Test fun dark() = capture("dark", ExecutiveUiState(data = loaded), dark = true)

    @Test fun nothingRecordedYet() = capture(
        "empty",
        ExecutiveUiState(
            data = ExecutiveAnalyticsData(
                totalActiveStudents = 1043,
                attendance = ExecutiveAttendance(today = AttendanceBreakdown(), days = emptyList()),
                spp = ExecutiveSpp(month = "Oktober 2026"),
            ),
        ),
    )

    @Test fun staleAfterFailedRefresh() = capture("stale", ExecutiveUiState(data = loaded, errorMessage = "Tidak dapat terhubung ke server."))

    @Test fun olderServer() = capture("older_server", ExecutiveUiState(data = ExecutiveAnalyticsData(totalActiveStudents = 1043)))

    @Test fun loading() = capture("loading", ExecutiveUiState(isLoading = true))

    @Test fun forbidden() = capture("error", ExecutiveUiState(errorMessage = "Akun ini tidak memiliki akses ke analitik eksekutif."))
}
