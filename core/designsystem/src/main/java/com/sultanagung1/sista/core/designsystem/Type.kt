package com.sultanagung1.sista.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.R

// Google Play Services Downloadable Font Provider
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// 1. Primary Institutional UI Font — Plus Jakarta Sans
val PlusJakartaSansName = GoogleFont("Plus Jakarta Sans")
val PlusJakartaSansFontFamily = FontFamily(
    Font(googleFont = PlusJakartaSansName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSansName, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSansName, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSansName, fontProvider = fontProvider, weight = FontWeight.Bold),
    // FASE 76.1 Part B: added for SulaoneEmphasizedTypography below — the
    // roles that were already at Bold (display/headline/titleLarge) had no
    // weight headroom left to express Material 3 Expressive's "emphasized"
    // tier without this.
    Font(googleFont = PlusJakartaSansName, fontProvider = fontProvider, weight = FontWeight.ExtraBold)
)

// 2. Classical Arabic & Quranic Font — Amiri (Tahsin, Mutabaah, Doa, Hadith)
val AmiriName = GoogleFont("Amiri")
val AmiriFontFamily = FontFamily(
    Font(googleFont = AmiriName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = AmiriName, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// 3. Technical & Monospace Font — JetBrains Mono (CBT Token, QR Payload, NISN, Logs)
val JetBrainsMonoName = GoogleFont("JetBrains Mono")
val JetBrainsMonoFontFamily = FontFamily(
    Font(googleFont = JetBrainsMonoName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = JetBrainsMonoName, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = JetBrainsMonoName, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// 4. Dyslexia-Friendly Font — Lexend (Engineered to reduce visual stress and boost readability)
val DyslexicFontName = GoogleFont("Lexend")
val DyslexicFontFamily = FontFamily(
    Font(googleFont = DyslexicFontName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = DyslexicFontName, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = DyslexicFontName, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Specialized Typographic Styles
val QuranicTextStyle = TextStyle(
    fontFamily = AmiriFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 38.sp,
    letterSpacing = 0.5.sp
)

val MonospaceTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 1.sp
)

/**
 * Standard Sulaone Enterprise Typography (Plus Jakarta Sans)
 */
val SulaoneTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * FASE 76.1 Part B — Material 3 Expressive "emphasized" typography tier.
 *
 * This is NOT a blanket font-size increase. Real M3 Expressive documentation
 * (m3.material.io) defines "emphasized" as a *parallel* style set with the
 * SAME size/line-height/letter-spacing as baseline but one weight step
 * heavier — meant for selective highlight moments (a hero score, a primary
 * CTA, an unread-message count), explicitly NOT for applying wholesale to
 * a screen ("never use all emphasized styles on a single screen" — M3 spec).
 *
 * Every value below is copy-identical to [SulaoneTypography] except
 * fontWeight, which is why this is safe to ship without an emulator/visual
 * pass: nothing that affects layout (size, line height, tracking) changes,
 * so there is no overflow/clipping/reflow risk on any of the 85 existing
 * screens — none of which reference this object yet. It is not wired as the
 * app's default Typography (see SulaoneTheme in Theme.kt, unchanged).
 *
 * It is a reference table of the tier (pinned to Plus Jakarta Sans). In UI
 * code prefer `MaterialTheme.typography.x.emphasized()` (below), which
 * respects the Dyslexic-friendly Lexend mode instead of overriding it.
 *
 * Weight steps (one tier up from SulaoneTypography, per role):
 * Bold -> ExtraBold (added to PlusJakartaSansFontFamily above specifically
 * for this, since Bold already had no headroom for display/headline/titleLarge),
 * SemiBold -> Bold, Medium -> SemiBold, Normal -> Medium.
 */
val SulaoneEmphasizedTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * FASE 76.2 — theme-aware emphasis. Shared components must use this rather
 * than referencing [SulaoneEmphasizedTypography] directly: that object is
 * pinned to Plus Jakarta Sans, so using it inside a component would silently
 * override SulaoneTheme's Dyslexic-friendly mode (which swaps the whole
 * Typography to Lexend). `MaterialTheme.typography.x.emphasized()` keeps
 * whatever font family, size and line height the active theme provides and
 * only steps the weight up by one tier.
 */
fun TextStyle.emphasized(): TextStyle = copy(fontWeight = emphasizedWeight(fontWeight))

/**
 * One step heavier than [weight], capped at ExtraBold (800) — the heaviest
 * weight declared for PlusJakartaSansFontFamily. For families without
 * ExtraBold (Lexend, Amiri) Compose falls back to their nearest declared
 * weight, i.e. Bold, which is still visibly emphasized.
 */
fun emphasizedWeight(weight: FontWeight?): FontWeight = when ((weight ?: FontWeight.Normal).weight) {
    in 0..449 -> FontWeight.Medium
    in 450..549 -> FontWeight.SemiBold
    in 550..649 -> FontWeight.Bold
    else -> FontWeight.ExtraBold
}

/**
 * Dyslexia-Friendly Typography (Lexend)
 * Features increased letter-spacing, heavier minimum body weights, and taller line heights
 * to eliminate crowding and character confusion.
 */
val DyslexicTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.5.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.6.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.6.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.7.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.8.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.8.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.9.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.9.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.9.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DyslexicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.0.sp
    )
)

// Backward compatibility alias
val Typography = SulaoneTypography
