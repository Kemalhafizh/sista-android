package com.sultanagung1.sista.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * SISTA colour system.
 *
 * Brand: Sultan Agung emerald as primary, a warm gold as tertiary (used
 * sparingly: highlights, achievements), and quiet green-tinted neutrals.
 * Every role pair (x / onX) meets WCAG AA (4.5:1) for body text in both
 * themes. Screens never reference these hex values directly: they use
 * MaterialTheme.colorScheme roles or SistaTheme.extendedColors.
 */

internal val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF047857),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    inversePrimary = Color(0xFF6EE7B7),
    secondary = Color(0xFF3F6357),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8DF),
    onSecondaryContainer = Color(0xFF0F2A21),
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE8B5),
    onTertiaryContainer = Color(0xFF2C1B00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7FAF8),
    onBackground = Color(0xFF111814),
    surface = Color(0xFFF7FAF8),
    onSurface = Color(0xFF111814),
    surfaceVariant = Color(0xFFDCE5DF),
    onSurfaceVariant = Color(0xFF3F4944),
    surfaceTint = Color(0xFF047857),
    inverseSurface = Color(0xFF2D322F),
    inverseOnSurface = Color(0xFFEEF2EF),
    outline = Color(0xFF6F7973),
    outlineVariant = Color(0xFFBFC9C3),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF7FAF8),
    surfaceDim = Color(0xFFD8DCDA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5F2),
    surfaceContainer = Color(0xFFEBF0ED),
    surfaceContainerHigh = Color(0xFFE5EAE7),
    surfaceContainerHighest = Color(0xFFDFE4E1),
)

internal val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF6EE7B7),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFFA7F3D0),
    inversePrimary = Color(0xFF047857),
    secondary = Color(0xFFB9CCC3),
    onSecondary = Color(0xFF243530),
    secondaryContainer = Color(0xFF3A4B45),
    onSecondaryContainer = Color(0xFFD5E8DF),
    tertiary = Color(0xFFF5C35B),
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF614000),
    onTertiaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1412),
    onBackground = Color(0xFFDEE4E0),
    surface = Color(0xFF0F1412),
    onSurface = Color(0xFFDEE4E0),
    surfaceVariant = Color(0xFF3F4944),
    onSurfaceVariant = Color(0xFFBFC9C3),
    surfaceTint = Color(0xFF6EE7B7),
    inverseSurface = Color(0xFFDEE4E0),
    inverseOnSurface = Color(0xFF2D322F),
    outline = Color(0xFF89938D),
    outlineVariant = Color(0xFF3F4944),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF353A37),
    surfaceDim = Color(0xFF0F1412),
    surfaceContainerLowest = Color(0xFF0A0F0D),
    surfaceContainerLow = Color(0xFF171D1A),
    surfaceContainer = Color(0xFF1B211E),
    surfaceContainerHigh = Color(0xFF252B28),
    surfaceContainerHighest = Color(0xFF303633),
)

/**
 * Status colours Material 3 does not have roles for. Used by [StatusTone]
 * (pills, banners, stat tiles) so "Hadir", "Terlambat", "Lunas", "Jatuh
 * tempo"… look the same on every screen.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

internal val LightExtendedColors = ExtendedColors(
    success = Color(0xFF1B6C3A),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFC8F0D2),
    onSuccessContainer = Color(0xFF002110),
    warning = Color(0xFF7A5900),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE08B),
    onWarningContainer = Color(0xFF261A00),
    info = Color(0xFF1F5FA8),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFD6E3FF),
    onInfoContainer = Color(0xFF001B3E),
)

internal val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF8DD8A0),
    onSuccess = Color(0xFF00391B),
    successContainer = Color(0xFF00522A),
    onSuccessContainer = Color(0xFFC8F0D2),
    warning = Color(0xFFF0C048),
    onWarning = Color(0xFF3F2E00),
    warningContainer = Color(0xFF5C4300),
    onWarningContainer = Color(0xFFFFE08B),
    info = Color(0xFFA9C7FF),
    onInfo = Color(0xFF003062),
    infoContainer = Color(0xFF00468C),
    onInfoContainer = Color(0xFFD6E3FF),
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

// Display preferences (DisplayPreferences.kt) — kept here with every other colour.

/** True black behind everything, so OLED pixels switch off; cards stay a step above it. */
internal fun ColorScheme.amoledBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0C100E),
    surfaceContainer = Color(0xFF111614),
    surfaceContainerHigh = Color(0xFF181D1B),
    surfaceContainerHighest = Color(0xFF1F2522),
)

/** Text at full strength (no muted grey) and outlines that read as lines. */
internal fun ColorScheme.highContrast(dark: Boolean): ColorScheme {
    val ink = if (dark) Color.White else Color.Black
    return copy(
        onBackground = ink,
        onSurface = ink,
        onSurfaceVariant = ink,
        outline = ink,
        outlineVariant = if (dark) Color(0xFFB8C2BC) else Color(0xFF3F4944),
    )
}
