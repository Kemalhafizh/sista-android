package com.sultanagung1.sista.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * The display choices a person makes in Pengaturan > Tampilan/Aksesibilitas.
 * The app provides them at the root; [ShellTheme] applies them, so every
 * rebuilt screen follows them. (Before this, ShellTheme only read light/dark:
 * "Hitam pekat", "Kontras tinggi" and "Ramah disleksia" changed nothing on
 * the rebuilt screens.) Text size needs nothing here: it comes through
 * LocalDensity.
 */
@Immutable
data class DisplayPreferences(
    val amoledBlack: Boolean = false,
    val highContrast: Boolean = false,
    val dyslexicFriendly: Boolean = false,
)

val LocalDisplayPreferences = staticCompositionLocalOf { DisplayPreferences() }

/**
 * Wider letter spacing and taller lines, the two changes that help most
 * readers with dyslexia; the typeface stays the same.
 */
internal fun Typography.dyslexicFriendly(): Typography {
    fun TextStyle.spaced(): TextStyle = copy(
        letterSpacing = 0.06.em,
        lineHeight = if (fontSize.isSpecified) (fontSize.value * 1.5f).sp else lineHeight,
    )
    return copy(
        displayLarge = displayLarge.spaced(), displayMedium = displayMedium.spaced(), displaySmall = displaySmall.spaced(),
        headlineLarge = headlineLarge.spaced(), headlineMedium = headlineMedium.spaced(), headlineSmall = headlineSmall.spaced(),
        titleLarge = titleLarge.spaced(), titleMedium = titleMedium.spaced(), titleSmall = titleSmall.spaced(),
        bodyLarge = bodyLarge.spaced(), bodyMedium = bodyMedium.spaced(), bodySmall = bodySmall.spaced(),
        labelLarge = labelLarge.spaced(), labelMedium = labelMedium.spaced(), labelSmall = labelSmall.spaced(),
    )
}

/** The colour scheme for these choices. */
internal fun colorsFor(dark: Boolean, preferences: DisplayPreferences): ColorScheme {
    var scheme = if (dark) DarkColors else LightColors
    if (dark && preferences.amoledBlack) scheme = scheme.amoledBlack()
    if (preferences.highContrast) scheme = scheme.highContrast(dark)
    return scheme
}

internal fun typographyFor(preferences: DisplayPreferences): Typography =
    if (preferences.dyslexicFriendly) SistaTypography.dyslexicFriendly() else SistaTypography
