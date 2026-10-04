package com.sultanagung1.sista.ui.auth

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The sign-in screen as every account sees it. Record with
 * `./gradlew :feature:auth:recordRoborazziDebug`; images land in
 * feature/auth/screenshots.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h860dp-xhdpi")
class LoginScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        name: String,
        dark: Boolean = false,
        identifier: String = "",
        password: String = "",
        error: String? = null,
        biometric: Boolean = false,
    ) {
        compose.setContent {
            // The host theme only tells ShellTheme light or dark, like the app's ThemeManager does.
            // The app's manifest sets supportsRtl; this module's test manifest doesn't,
            // so take the direction from the locale like the app does.
            val rtl = LocalConfiguration.current.locales[0].language == "ar"
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    LoginContent(
                        identifier = identifier,
                        onIdentifierChange = {},
                        password = password,
                        onPasswordChange = {},
                        loading = false,
                        errorMessage = error,
                        onDismissError = {},
                        onSubmit = {},
                        onBiometric = if (biometric) ({}) else null,
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage("screenshots/login_$name.png")
    }

    @Test fun empty() = capture("empty")

    @Test fun filledWithBiometric() = capture("biometric", identifier = "0071234567", password = "rahasia", biometric = true)

    @Test fun wrongPassword() = capture(
        "error",
        identifier = "0071234567",
        password = "salah",
        error = "NISN/NIP atau kata sandi salah.",
    )

    @Test fun dark() = capture("dark", dark = true, identifier = "guru@sultanagung1.sch.id")

    // The server answers in the app's language, so its message arrives already translated.
    @Test @Config(qualifiers = "en-w400dp-h860dp-xhdpi")
    fun english() = capture("en", identifier = "0071234567", password = "salah", error = "These credentials do not match our records.", biometric = true)

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h860dp-xhdpi")
    fun arabic() = capture("ar", identifier = "0071234567", password = "rahasia", biometric = true)
}
