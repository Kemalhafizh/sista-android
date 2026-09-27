package com.sultanagung1.sista.core.designsystem.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentAmber
import com.sultanagung1.sista.core.designsystem.AccentBlue
import com.sultanagung1.sista.core.designsystem.AccentCyan
import com.sultanagung1.sista.core.designsystem.AccentGreen
import com.sultanagung1.sista.core.designsystem.AccentPurple
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.designsystem.AmiriFontFamily
import com.sultanagung1.sista.core.designsystem.DyslexicTypography
import com.sultanagung1.sista.core.designsystem.MonospaceTextStyle
import com.sultanagung1.sista.core.designsystem.PlusJakartaSansFontFamily
import com.sultanagung1.sista.core.designsystem.QuranicTextStyle
import com.sultanagung1.sista.core.designsystem.SulaoneTypography
import com.sultanagung1.sista.core.designsystem.Emerald50 as SourceEmerald50
import com.sultanagung1.sista.core.designsystem.Emerald100 as SourceEmerald100
import com.sultanagung1.sista.core.designsystem.Emerald200 as SourceEmerald200
import com.sultanagung1.sista.core.designsystem.Emerald300 as SourceEmerald300
import com.sultanagung1.sista.core.designsystem.Emerald400 as SourceEmerald400
import com.sultanagung1.sista.core.designsystem.Emerald500 as SourceEmerald500
import com.sultanagung1.sista.core.designsystem.Emerald600 as SourceEmerald600
import com.sultanagung1.sista.core.designsystem.Emerald700 as SourceEmerald700
import com.sultanagung1.sista.core.designsystem.Emerald800 as SourceEmerald800
import com.sultanagung1.sista.core.designsystem.Emerald900 as SourceEmerald900
import com.sultanagung1.sista.core.designsystem.Emerald950 as SourceEmerald950
import com.sultanagung1.sista.core.designsystem.Gold50 as SourceGold50
import com.sultanagung1.sista.core.designsystem.Gold100 as SourceGold100
import com.sultanagung1.sista.core.designsystem.Gold200 as SourceGold200
import com.sultanagung1.sista.core.designsystem.Gold300 as SourceGold300
import com.sultanagung1.sista.core.designsystem.Gold400 as SourceGold400
import com.sultanagung1.sista.core.designsystem.Gold500 as SourceGold500
import com.sultanagung1.sista.core.designsystem.Gold600 as SourceGold600
import com.sultanagung1.sista.core.designsystem.Gold700 as SourceGold700
import com.sultanagung1.sista.core.designsystem.Gold800 as SourceGold800
import com.sultanagung1.sista.core.designsystem.Gold900 as SourceGold900
import com.sultanagung1.sista.core.designsystem.Slate50 as SourceSlate50
import com.sultanagung1.sista.core.designsystem.Slate100 as SourceSlate100
import com.sultanagung1.sista.core.designsystem.Slate200 as SourceSlate200
import com.sultanagung1.sista.core.designsystem.Slate300 as SourceSlate300
import com.sultanagung1.sista.core.designsystem.Slate400 as SourceSlate400
import com.sultanagung1.sista.core.designsystem.Slate500 as SourceSlate500
import com.sultanagung1.sista.core.designsystem.Slate600 as SourceSlate600
import com.sultanagung1.sista.core.designsystem.Slate700 as SourceSlate700
import com.sultanagung1.sista.core.designsystem.Slate800 as SourceSlate800
import com.sultanagung1.sista.core.designsystem.Slate850 as SourceSlate850
import com.sultanagung1.sista.core.designsystem.Slate900 as SourceSlate900
import com.sultanagung1.sista.core.designsystem.Slate950 as SourceSlate950

/**
 * FASE 70.1 — Single-source-of-truth design tokens for Sulaone.
 *
 * No Figma file is actually wired into this workspace (no `figma.com/design/...`
 * URL exists anywhere in the project or docs), so nothing here is pulled from
 * `get_design_context` — that would mean guessing values and presenting them as
 * pixel-accurate, which is exactly the "hallucinated design" FASE 67/70 exist to
 * prevent. Instead this file formalizes the palette/type scale that FASE 60–67
 * already established and that every real screen in the app already uses
 * (`Color.kt`, `Type.kt`) into the namespaced structure the roadmap describes —
 * as thin aliases, not new hex literals, so there is exactly one place a color
 * or type style is ever defined. If a real Figma file is connected later,
 * `get_design_context` output should be reconciled INTO `Color.kt`/`Type.kt`
 * directly; this file will then reflect it automatically.
 */
object SulaoneColorTokens {
    // Neutral Off-White Canvas
    val Slate50 = SourceSlate50
    val Slate100 = SourceSlate100
    val Slate200 = SourceSlate200
    val Slate300 = SourceSlate300
    val Slate400 = SourceSlate400
    val Slate500 = SourceSlate500
    val Slate600 = SourceSlate600
    val Slate700 = SourceSlate700
    val Slate800 = SourceSlate800
    val Slate850 = SourceSlate850
    val Slate900 = SourceSlate900
    val Slate950 = SourceSlate950

    // Islamic Institutional Accent — restricted to CTA & badges per FASE 60 brand rule
    val Emerald50 = SourceEmerald50
    val Emerald100 = SourceEmerald100
    val Emerald200 = SourceEmerald200
    val Emerald300 = SourceEmerald300
    val Emerald400 = SourceEmerald400
    val Emerald500 = SourceEmerald500
    val Emerald600 = SourceEmerald600
    val Emerald700 = SourceEmerald700 // Primary Brand
    val Emerald800 = SourceEmerald800
    val Emerald900 = SourceEmerald900
    val Emerald950 = SourceEmerald950

    // Gold Prestasi — gamification & verified-only accents
    val Gold50 = SourceGold50
    val Gold100 = SourceGold100
    val Gold200 = SourceGold200
    val Gold300 = SourceGold300
    val Gold400 = SourceGold400
    val Gold500 = SourceGold500
    val Gold600 = SourceGold600 // Secondary Islamic Gold
    val Gold700 = SourceGold700
    val Gold800 = SourceGold800
    val Gold900 = SourceGold900

    // Semantic accents
    val Info = AccentBlue
    val Brand = AccentPurple
    val Warning = AccentAmber
    val Danger = AccentRose
    val Success = AccentGreen
    val Cyan = AccentCyan
}

/** FASE 70.1 — Modular type scale (Plus Jakarta Sans / Amiri), WCAG 2.2 AAA line-height ratios already baked into [SulaoneTypography]. */
object SulaoneTypographyTokens {
    val latin = SulaoneTypography
    val dyslexicFriendly = DyslexicTypography
    val latinFontFamily = PlusJakartaSansFontFamily
    val arabicFontFamily = AmiriFontFamily

    /** For Qur'an ayat, du'a, and hadith text — Amiri, generous line-height for tashkeel legibility. */
    val quranic: TextStyle = QuranicTextStyle

    /** For CBT tokens, QR payloads, NISN/NIP, and technical logs. */
    val monospace: TextStyle = MonospaceTextStyle
}

/** FASE 70.1 — 4dp base spacing scale observed consistently across every real screen (padding(16.dp)/(20.dp) etc.). */
object SulaoneSpacingTokens {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
}

/** FASE 70.1 — Corner-radius scale matching the RoundedCornerShape values already used throughout the app. */
object SulaoneRadiusTokens {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val pill: Dp = 999.dp
}

/**
 * FASE 70.1 — Elevation scale. Sulaone's established visual language (FASE 60/62)
 * is flat: 0dp cards with a 0.5–1dp hairline border instead of drop shadows,
 * reserving real elevation for sheets/dialogs that must visually float.
 */
object SulaoneElevationTokens {
    val flat: Dp = 0.dp
    val hairline: Dp = 0.5.dp
    val border: Dp = 1.dp
    val card: Dp = 1.dp
    val raised: Dp = 4.dp
    val floating: Dp = 8.dp
}

/** FASE 70.3 — Minimum interactive touch target, WCAG 2.2 AA/AAA (2.5.8 / 2.5.5). */
val SulaoneMinTouchTarget: Dp = 48.dp
