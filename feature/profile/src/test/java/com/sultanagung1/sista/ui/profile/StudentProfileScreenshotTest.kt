package com.sultanagung1.sista.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.core.ui.theme.DisplayPreferences
import com.sultanagung1.sista.core.ui.theme.LocalDisplayPreferences
import com.sultanagung1.sista.data.model.ProfileAcademicSummary
import com.sultanagung1.sista.data.model.ProfileAchievementItem
import com.sultanagung1.sista.data.model.ProfileBiodata
import com.sultanagung1.sista.data.model.ProfileDisciplineSummary
import com.sultanagung1.sista.data.model.ProfileExtracurricularItem
import com.sultanagung1.sista.data.model.ProfileHealthSummary
import com.sultanagung1.sista.data.model.ProfileIbadahSummary
import com.sultanagung1.sista.data.model.StudentProfile360Data
import com.sultanagung1.sista.ui.settings.AccessibilitySettingsContent
import com.sultanagung1.sista.ui.settings.SettingsContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The 360 profile, settings and accessibility pages. The student exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1100dp-xhdpi")
class StudentProfileScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val full = StudentProfile360Data(
        biodata = ProfileBiodata(id = 9, name = "Nadia Putri Rahmawati", nisn = "0091234567", className = "XI MIPA 2"),
        academicSummary = ProfileAcademicSummary(84.6, 3, 32, "Biologi", "Fisika", listOf(81.2, 83.0, 84.6)),
        ibadahSummary = ProfileIbadahSummary(2, 5, 40, "Al-Mulk", 6, 72.5),
        disciplineSummary = ProfileDisciplineSummary(25, 5, 5, "Aman", 0),
        extracurricularList = listOf(ProfileExtracurricularItem("Karya Ilmiah Remaja", "Sekretaris", "2025")),
        achievementList = listOf(ProfileAchievementItem("Juara 2 Lomba Karya Tulis Ilmiah", "Kota", 2026, "Akademik")),
        healthSummary = ProfileHealthSummary("O", 158.0, 47.5, listOf("Udang"), 2, "2026-09-12"),
    )

    /** A student the school has recorded nothing for yet. */
    private val empty = StudentProfile360Data(biodata = ProfileBiodata(id = 10, name = "Raka Pratama", className = "X IPS 1"))

    private fun capture(name: String, dark: Boolean = false, display: DisplayPreferences = DisplayPreferences(), content: @Composable () -> Unit) {
        compose.setContent {
            CompositionLocalProvider(LocalDisplayPreferences provides display) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() }
            }
        }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    private fun profile(name: String, state: StudentProfileUiState, dark: Boolean = false) = capture("student_profile_$name", dark) {
        StudentProfileContent(state, onRetry = {}, onTab = {}, onNavigateBack = {})
    }

    @Test fun academic() = profile("academic", StudentProfileUiState(isLoading = false, profile = full))

    @Test fun academicDark() = profile("academic_dark", StudentProfileUiState(isLoading = false, profile = full), dark = true)

    @Test fun ibadah() = profile("ibadah", StudentProfileUiState(isLoading = false, profile = full, tab = ProfileTab.Ibadah))

    @Test fun activities() = profile("activities", StudentProfileUiState(isLoading = false, profile = full, tab = ProfileTab.Activities))

    @Test fun health() = profile("health", StudentProfileUiState(isLoading = false, profile = full, tab = ProfileTab.Health))

    @Test fun nothingRecordedAcademic() = profile("empty_academic", StudentProfileUiState(isLoading = false, profile = empty))

    @Test fun nothingRecordedIbadah() = profile("empty_ibadah", StudentProfileUiState(isLoading = false, profile = empty, tab = ProfileTab.Ibadah))

    @Test fun nothingRecordedActivities() =
        profile("empty_activities", StudentProfileUiState(isLoading = false, profile = empty, tab = ProfileTab.Activities))

    @Test fun loading() = profile("loading", StudentProfileUiState())

    @Test fun notFound() = profile(
        "error",
        StudentProfileUiState(isLoading = false, errorMessage = "Data siswa untuk akun ini belum tercatat. Hubungi tata usaha sekolah."),
    )

    @Test fun settings() = capture("settings") {
        SettingsContent(
            theme = AppThemeMode.SYSTEM, language = AppLanguage.INDONESIAN, canOpen = { true },
            onTheme = {}, onLanguage = {}, onAccessibility = {}, onBiometrics = {}, onSecurity = {}, onNavigateBack = {},
        )
    }

    @Test fun settingsAmoled() = capture("settings_amoled", dark = true, display = DisplayPreferences(amoledBlack = true)) {
        SettingsContent(
            theme = AppThemeMode.AMOLED_BLACK, language = AppLanguage.INDONESIAN, canOpen = { true },
            onTheme = {}, onLanguage = {}, onAccessibility = {}, onBiometrics = {}, onSecurity = {}, onNavigateBack = {},
        )
    }

    @Test fun accessibility() = capture("accessibility") {
        AccessibilitySettingsContent(1.0f, dyslexicFriendly = false, highContrast = false, {}, {}, {}, onNavigateBack = {})
    }

    @Test fun accessibilityApplied() = capture(
        "accessibility_dyslexic_contrast",
        display = DisplayPreferences(highContrast = true, dyslexicFriendly = true),
    ) {
        AccessibilitySettingsContent(1.25f, dyslexicFriendly = true, highContrast = true, {}, {}, {}, onNavigateBack = {})
    }
}
