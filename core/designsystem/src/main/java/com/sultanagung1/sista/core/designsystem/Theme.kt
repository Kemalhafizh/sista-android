package com.sultanagung1.sista.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.sultanagung1.sista.core.accessibility.*
import com.sultanagung1.sista.core.designsystem.tokens.SulaoneRadiusTokens

/**
 * FASE 76.1: MaterialTheme.shapes formally sourced from [SulaoneRadiusTokens]
 * (which existed since FASE 70.1 but, like several other tokens in that
 * file, had zero real call sites anywhere in the app until now). Only
 * `small`/`medium`/`large` are overridden — those three happen to already
 * equal Compose's own M3 defaults (8dp/12dp/16dp), so this is a genuine
 * zero-visual-change wiring, not a redesign. `extraSmall` (M3 default 4dp)
 * and `extraLarge` (M3 default 28dp, and asymmetric top-only in some
 * components like bottom sheets) are deliberately left at Compose's
 * defaults rather than mapped to a same-named SulaoneRadiusTokens value
 * that would actually change their rendered corner radius — that's a real
 * design decision for a later, dedicated pass, not an accidental side
 * effect of this token-wiring commit.
 */
private val SulaoneShapes = Shapes(
    small = RoundedCornerShape(SulaoneRadiusTokens.sm),
    medium = RoundedCornerShape(SulaoneRadiusTokens.md),
    large = RoundedCornerShape(SulaoneRadiusTokens.lg)
)

val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Slate950,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    secondary = Gold500,
    onSecondary = Slate950,
    secondaryContainer = Gold800,
    onSecondaryContainer = Gold100,
    background = BackgroundDark,
    onBackground = Slate100,
    surface = CardSurfaceDark,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    surfaceDim = ObsidianBackground,
    surfaceBright = Slate700,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = Slate950,
    surfaceContainer = CardSurfaceDark,
    surfaceContainerHigh = Slate850,
    surfaceContainerHighest = Slate800,
    outline = Slate700,
    error = AccentRose
)

val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = CardSurfaceLight,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald900,
    secondary = Gold600,
    onSecondary = CardSurfaceLight,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold800,
    background = BackgroundLight,
    onBackground = Slate900,
    surface = CardSurfaceLight,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    surfaceDim = Slate200,
    surfaceBright = CardSurfaceLight,
    surfaceContainerLowest = CardSurfaceLight,
    surfaceContainerLow = Slate50,
    surfaceContainer = Slate100,
    surfaceContainerHigh = Slate200,
    surfaceContainerHighest = Slate300,
    outline = Slate200,
    error = AccentRose
)

val AmoledColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003822),
    onPrimaryContainer = Emerald100,
    secondary = Gold400,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382D00),
    onSecondaryContainer = Gold100,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFE0E0E0),
    surfaceDim = Color.Black,
    surfaceBright = Color(0xFF222222),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerHigh = Color(0xFF121212),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    outline = Color(0xFF333333),
    error = AccentRose
)

val HighContrastColorScheme = darkColorScheme(
    primary = Color(0xFF00FF88),
    onPrimary = Color.Black,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFD700),
    onSecondary = Color.Black,
    secondaryContainer = Color.Black,
    onSecondaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color.White,
    surfaceDim = Color.Black,
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF333333),
    outline = Color.White,
    error = Color(0xFFFF4444)
)

enum class ThemePreset { Default, NightStudy, ExamMode, RamadhanGold }

val NightStudyColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00332C),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFFFCC80),
    background = Color(0xFF0A1118),
    surface = Color(0xFF131D26),
    onBackground = Color(0xFFE0E6ED),
    onSurface = Color(0xFFE0E6ED),
    surfaceDim = Color(0xFF070D13),
    surfaceBright = Color(0xFF202E3D),
    surfaceContainerLowest = Color(0xFF0A1118),
    surfaceContainerLow = Color(0xFF0E1721),
    surfaceContainer = Color(0xFF131D26),
    surfaceContainerHigh = Color(0xFF192733),
    surfaceContainerHighest = Color(0xFF223445),
    outline = Color(0xFF263545)
)

val ExamModeColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFFD32F2F),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    surfaceDim = Color(0xFFE2E8F0),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = Color(0xFFCBD5E1)
)

val RamadhanGoldColorScheme = darkColorScheme(
    primary = Gold400,
    onPrimary = Color(0xFF2D2000),
    primaryContainer = Gold800,
    onPrimaryContainer = Gold100,
    secondary = Emerald400,
    background = Color(0xFF0D1813),
    surface = Color(0xFF14241D),
    onBackground = Color(0xFFFFF8E7),
    onSurface = Color(0xFFFFF8E7),
    surfaceDim = Color(0xFF09120E),
    surfaceBright = Color(0xFF253E32),
    surfaceContainerLowest = Color(0xFF0D1813),
    surfaceContainerLow = Color(0xFF101E18),
    surfaceContainer = Color(0xFF14241D),
    surfaceContainerHigh = Color(0xFF1B3027),
    surfaceContainerHighest = Color(0xFF243F33),
    outline = Gold700
)

@Composable
fun SulaoneTheme(
    contextualPreset: ThemePreset = ThemePreset.Default,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isHighContrast: Boolean = false,
    dynamicColor: Boolean = false,
    fontScale: Float = 1.0f,
    isDyslexicFriendly: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED_BLACK, AppThemeMode.HIGH_CONTRAST -> true
    }

    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (isDark) androidx.compose.material3.dynamicDarkColorScheme(context) else androidx.compose.material3.dynamicLightColorScheme(context)
        }
        contextualPreset == ThemePreset.NightStudy -> NightStudyColorScheme
        contextualPreset == ThemePreset.ExamMode -> ExamModeColorScheme
        contextualPreset == ThemePreset.RamadhanGold -> RamadhanGoldColorScheme
        themeMode == AppThemeMode.LIGHT -> LightColorScheme
        themeMode == AppThemeMode.DARK -> DarkColorScheme
        themeMode == AppThemeMode.AMOLED_BLACK -> AmoledColorScheme
        themeMode == AppThemeMode.HIGH_CONTRAST || isHighContrast -> HighContrastColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val extendedColors = when {
        contextualPreset == ThemePreset.RamadhanGold -> SulaoneExtendedColors(
            islamicGreen = Emerald400,
            islamicGold = Gold400,
            successGreen = AccentGreen,
            warningAmber = Gold500,
            dangerRose = AccentRose,
            cardGradientStart = Gold800,
            cardGradientEnd = Gold600,
            surfaceSubtle = Color(0xFF14241D),
            borderSubtle = Gold700
        )
        contextualPreset == ThemePreset.ExamMode -> SulaoneExtendedColors(
            islamicGreen = Color(0xFF1565C0),
            islamicGold = Gold600,
            successGreen = AccentGreen,
            warningAmber = AccentAmber,
            dangerRose = AccentRose,
            cardGradientStart = Color(0xFF1565C0),
            cardGradientEnd = Color(0xFF0D47A1),
            surfaceSubtle = Color(0xFFF1F5F9),
            borderSubtle = Color(0xFFCBD5E1)
        )
        themeMode == AppThemeMode.AMOLED_BLACK -> SulaoneExtendedColors(
            islamicGreen = Emerald400,
            islamicGold = Gold400,
            successGreen = Color(0xFF00FF88),
            warningAmber = Gold400,
            dangerRose = AccentRose,
            cardGradientStart = Color(0xFF002214),
            cardGradientEnd = Color.Black,
            surfaceSubtle = Color(0xFF0A0A0A),
            borderSubtle = Color(0xFF222222)
        )
        isDark -> SulaoneExtendedColors(
            islamicGreen = Emerald400,
            islamicGold = Gold500,
            successGreen = AccentGreen,
            warningAmber = AccentAmber,
            dangerRose = AccentRose,
            cardGradientStart = Emerald950,
            cardGradientEnd = Slate900,
            surfaceSubtle = Slate850,
            borderSubtle = Slate700
        )
        else -> SulaoneExtendedColors(
            islamicGreen = Emerald700,
            islamicGold = Gold600,
            successGreen = AccentGreen,
            warningAmber = AccentAmber,
            dangerRose = AccentRose,
            cardGradientStart = Emerald800,
            cardGradientEnd = Emerald600,
            surfaceSubtle = Slate100,
            borderSubtle = Slate200
        )
    }

    val density = LocalDensity.current
    val customDensity = Density(
        density = density.density,
        fontScale = FontScaleManager.calculateEffectiveFontScale(density.fontScale, fontScale)
    )
    val typography = if (isDyslexicFriendly) DyslexicTypography else SulaoneTypography

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalSulaoneColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = SulaoneShapes,
            content = content
        )
    }
}
