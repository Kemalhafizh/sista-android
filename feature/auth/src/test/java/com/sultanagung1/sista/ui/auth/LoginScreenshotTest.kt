package com.sultanagung1.sista.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
@Config(sdk = [35], qualifiers = "w400dp-h860dp-xhdpi")
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
}
