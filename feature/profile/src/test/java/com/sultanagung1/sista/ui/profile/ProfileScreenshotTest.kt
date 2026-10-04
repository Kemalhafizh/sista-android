package com.sultanagung1.sista.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.core.display.DisplayCapabilities
import com.sultanagung1.sista.core.display.RefreshRateMode
import com.sultanagung1.sista.core.security.DeviceIntegrityReport
import com.sultanagung1.sista.core.ui.theme.DisplayPreferences
import com.sultanagung1.sista.core.ui.theme.LocalDisplayPreferences
import com.sultanagung1.sista.data.model.MeAcademicYear
import com.sultanagung1.sista.data.model.MeChild
import com.sultanagung1.sista.data.model.MeClassroom
import com.sultanagung1.sista.data.model.MeEmployeeData
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.MeStudentData
import com.sultanagung1.sista.data.model.SchoolIdentity
import com.sultanagung1.sista.ui.settings.SecuritySettingsContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The profile and device-security pages in their states. The people exist only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1500dp-xhdpi")
class ProfileScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val year = MeAcademicYear(1, "2026/2027", "Ganjil")
    private val school = SchoolIdentity(name = "SMA Islam Sultan Agung 1 Semarang", npsn = "20328918")
    private val display = DisplayChoice(
        DisplayCapabilities(120f, 120f, listOf(60f, 90f, 120f), isHighRefreshRateSupported = true, isLtpoSupported = false, summaryText = "120 Hz"),
        RefreshRateMode.ADAPTIVE_SMOOTH,
    )

    private val student = MeProfile(
        name = "Nadia Putri Rahmawati", email = "nadia@siswa.sch.id", roleLabel = "Siswa", academicYear = year,
        studentData = MeStudentData(nis = "24051", nisn = "0091234567", classroom = "XI MIPA 2", classroomId = 7),
    )
    private val teacher = MeProfile(
        name = "Budi Santoso, S.Pd.", email = "budi@guru.sch.id", roleLabel = "Guru", academicYear = year,
        employeeData = MeEmployeeData("198001012005011001", "Guru Fisika"),
        homeroomClassrooms = listOf(MeClassroom(7, "XI MIPA 2")),
    )
    private val parent = MeProfile(
        name = "Siti Aminah", phoneNumber = "081234567890", roleLabel = "Wali Murid", academicYear = year,
        children = listOf(MeChild("u1", "Nadia Putri Rahmawati", "XI MIPA 2"), MeChild("u2", "Raka Pratama", "X IPS 1")),
    )

    private fun capture(name: String, dark: Boolean = false, display: DisplayPreferences = DisplayPreferences(), content: @Composable () -> Unit) {
        compose.setContent {
            // The app's manifest sets supportsRtl; this module's test manifest doesn't,
            // so take the direction from the locale like the app does.
            val rtl = LocalConfiguration.current.locales[0].language == "ar"
            CompositionLocalProvider(
                LocalDisplayPreferences provides display,
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() }
            }
        }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    private fun profile(name: String, state: ProfileUiState, dark: Boolean = false, theme: AppThemeMode = AppThemeMode.SYSTEM) =
        capture("profile_$name", dark, DisplayPreferences(amoledBlack = theme == AppThemeMode.AMOLED_BLACK)) {
            ProfileContent(
                state = state, sessionName = "Nadia Putri Rahmawati", themeMode = theme, display = display,
                appVersion = appVersionLabel("1.4.0", 140), canOpen = { true }, onRetry = {}, onNavigate = {}, onThemeMode = {},
                onRefreshMode = {}, onLogout = {}, onNavigateBack = null,
            )
        }

    @Test fun student() = profile("student", ProfileUiState(profile = student, isLoading = false, school = school))

    @Test fun studentDark() = profile("student_dark", ProfileUiState(profile = student, isLoading = false), dark = true, theme = AppThemeMode.AMOLED_BLACK)

    @Test fun teacher() = profile("teacher", ProfileUiState(profile = teacher, isLoading = false))

    @Test fun parent() = profile("parent", ProfileUiState(profile = parent, isLoading = false))

    @Test fun parentWithoutChildren() =
        profile("parent_no_children", ProfileUiState(profile = parent.copy(children = emptyList()), isLoading = false))

    @Test fun loading() = profile("loading", ProfileUiState())

    @Test @Config(qualifiers = "en-w400dp-h1500dp-xhdpi")
    fun teacherEnglish() = profile("teacher_en", ProfileUiState(profile = teacher.copy(roleLabel = "Teacher"), isLoading = false))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1500dp-xhdpi")
    fun parentArabic() = profile("parent_ar", ProfileUiState(profile = parent.copy(roleLabel = "ولي الأمر"), isLoading = false))

    @Test fun error() = profile("error", ProfileUiState(isLoading = false, errorMessage = "Tidak dapat terhubung ke server."))

    private fun security(name: String, report: DeviceIntegrityReport?) = capture("security_$name") {
        SecuritySettingsContent(report = report, canOpenBiometrics = true, onOpenBiometrics = {}, onNavigateBack = {})
    }

    @Test fun securitySafe() = security("safe", DeviceIntegrityReport(false, false, false, "3F9A".repeat(16), isSecure = true))

    @Test fun securityWarning() = security("warning", DeviceIntegrityReport(true, false, true, "", isSecure = false))

    @Test fun securityLoading() = security("loading", null)
}
