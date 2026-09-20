package com.sultanagung1.sista.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Sultan Agung 1 Islamic Enterprise Color Palette
val Emerald950 = Color(0xFF032416)
val Emerald900 = Color(0xFF063823)
val Emerald800 = Color(0xFF0A4D30)
val Emerald700 = Color(0xFF0D5C3A) // Primary Brand
val Emerald600 = Color(0xFF107047)
val Emerald500 = Color(0xFF148554)
val Emerald400 = Color(0xFF1CB070)
val Emerald300 = Color(0xFF3ED290)
val Emerald200 = Color(0xFF7CE4B5)
val Emerald100 = Color(0xFFC7F4DE)
val Emerald50 = Color(0xFFEBFBF3)

val Gold900 = Color(0xFF6B550F)
val Gold800 = Color(0xFF8C7118)
val Gold700 = Color(0xFFB39224)
val Gold600 = Color(0xFFD4AF37) // Secondary Islamic Gold
val Gold500 = Color(0xFFE8C34E)
val Gold400 = Color(0xFFF7D97B)
val Gold300 = Color(0xFFFDE68A)
val Gold200 = Color(0xFFFEF08A)
val Gold100 = Color(0xFFFEF8E7)
val Gold50 = Color(0xFFFFFDF5)

val Slate950 = Color(0xFF080C14)
val Slate900 = Color(0xFF0F172A)
val Slate850 = Color(0xFF141E33)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val AccentBlue = Color(0xFF0284C7)
val AccentPurple = Color(0xFF7C3AED)
val AccentAmber = Color(0xFFD97706)
val AccentRose = Color(0xFFE11D48)
val AccentGreen = Color(0xFF16A34A)
val AccentCyan = Color(0xFF0891B2)

// Material 3 Expressive & Modern Glass Tokens
val GlassLight = Color(0xCCFFFFFF) // 80% opacity white
val GlassLightElevated = Color(0xE6FFFFFF) // 90% opacity white
val GlassBorderLight = Color(0x33FFFFFF)
val GlassDark = Color(0xB3162032) // 70% opacity slate
val GlassDarkElevated = Color(0xD9162032) // 85% opacity slate
val GlassBorderDark = Color(0x3338BDF8) // subtle cyan edge

// Ambient Glow Tokens
val EmeraldGlow = Color(0x4D10B981)
val GoldGlow = Color(0x4DF59E0B)
val SapphireGlow = Color(0x4D3B82F6)
val RoseGlow = Color(0x4DF43F5E)
val PurpleGlow = Color(0x4D8B5CF6)

// Obsidian OLED Dark Tokens
val ObsidianBackground = Color(0xFF070B12)
val ObsidianSurface = Color(0xFF0E1422)
val ObsidianCard = Color(0xFF141C2E)
val ObsidianCardBorder = Color(0xFF1E293B)

val CardSurfaceLight = Color(0xFFFFFFFF)
val CardSurfaceDark = Color(0xFF141C2E)
val BackgroundLight = Color(0xFFF8FAFC)
val BackgroundDark = Color(0xFF070B12)

// Islamic Luxury & Floating Component Tokens
val GoldShimmer = Color(0xFFFDE68A)
val EmeraldSheen = Color(0xFF34D399)
val IslamicArchBorder = Color(0x40D4AF37) // 25% gold border
val CardShadowSubtle = Color(0x14063823) // Soft emerald drop shadow
val FloatingNavBgLight = Color(0xF5FFFFFF) // 96% frosted white
val FloatingNavBgDark = Color(0xF50F172A) // 96% frosted slate
val FloatingNavBorderLight = Color(0x260D5C3A) // 15% emerald outline
val FloatingNavBorderDark = Color(0x33D4AF37) // 20% gold outline

fun Color.isDark(): Boolean = (0.299f * red + 0.587f * green + 0.114f * blue) < 0.5f

/**
 * Extended semantic color tokens specific to Sulaone (SMA Islam Sultan Agung 1 Semarang).
 * Includes Islamic brand identity, pedagogical statuses, and luxury gradients.
 */
data class SulaoneExtendedColors(
    val islamicGreen: Color,
    val islamicGold: Color,
    val successGreen: Color,
    val warningAmber: Color,
    val dangerRose: Color,
    val cardGradientStart: Color,
    val cardGradientEnd: Color,
    val infoBlue: Color = AccentBlue,
    val brandPurple: Color = AccentPurple,
    val surfaceSubtle: Color = Slate100,
    val borderSubtle: Color = Slate200
)

val LocalSulaoneColors = staticCompositionLocalOf {
    SulaoneExtendedColors(
        islamicGreen = Emerald700,
        islamicGold = Gold600,
        successGreen = AccentGreen,
        warningAmber = AccentAmber,
        dangerRose = AccentRose,
        cardGradientStart = Emerald800,
        cardGradientEnd = Emerald600,
        infoBlue = AccentBlue,
        brandPurple = AccentPurple,
        surfaceSubtle = Slate100,
        borderSubtle = Slate200
    )
}

val MaterialTheme.extendedColors: SulaoneExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSulaoneColors.current


