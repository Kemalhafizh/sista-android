package com.sultanagung1.sista.ui.attendance

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AttendanceCheckinResponse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The GPS check-in screen in its states. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h860dp-xhdpi")
class GeofenceScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val radius = 250.0

    private fun capture(
        name: String,
        location: LocationState,
        distance: Double? = null,
        result: AttendanceCheckinResponse? = null,
        queued: Boolean = false,
        error: String? = null,
        dark: Boolean = false,
    ) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                GeofenceAttendanceContent(
                    location = location,
                    status = GeofenceStatus.of(location, distance, radius),
                    radiusMeters = radius,
                    submitting = false,
                    result = result,
                    queuedOffline = queued,
                    errorMessage = error,
                    onAllowLocation = {},
                    onOpenSettings = {},
                    onRefreshLocation = {},
                    onCheckIn = {},
                    onDismissError = {},
                    onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/geofence_$name.png")
    }

    private val atSchool = LocationState.Fixed(-6.99616, 110.42851, 12f, false)

    @Test fun inside() = capture("inside", atSchool, distance = 42.0)

    @Test fun insideDark() = capture("inside_dark", atSchool, distance = 42.0, dark = true)

    @Test fun outside() = capture("outside", atSchool, distance = 1840.0)

    @Test fun mock() = capture("mock", atSchool.copy(isMock = true), distance = 10.0)

    @Test fun permission() = capture("permission", LocationState.NeedsPermission)

    @Test fun locating() = capture("locating", LocationState.Locating)

    @Test fun done() = capture(
        "done",
        atSchool,
        distance = 42.0,
        result = AttendanceCheckinResponse("success", "Presensi berhasil", "07:02", "HADIR"),
    )

    @Test fun rejected() = capture(
        "rejected",
        atSchool,
        distance = 42.0,
        error = "Sinyal GPS terlalu lemah (akurasi > 80m). Silakan pindah ke ruang terbuka.",
    )
}
