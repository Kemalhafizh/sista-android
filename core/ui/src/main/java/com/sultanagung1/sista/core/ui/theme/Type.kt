package com.sultanagung1.sista.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.ui.R

/*
 * Plus Jakarta Sans, bundled (variable font, OFL — see FONT-LICENSE-OFL.txt).
 * Bundled rather than downloaded so text looks the same offline, on devices
 * without Play Services, and in screenshot tests.
 */
@OptIn(ExperimentalTextApi::class)
private fun jakarta(weight: Int) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val PlusJakartaSans = FontFamily(
    jakarta(400),
    jakarta(500),
    jakarta(600),
    jakarta(700),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.em,
)

/** Material 3 type scale; headings slightly tighter, body at comfortable reading sizes. */
internal val SistaTypography = Typography(
    displayLarge = style(52, 60, FontWeight.Bold, -0.02),
    displayMedium = style(44, 52, FontWeight.Bold, -0.02),
    displaySmall = style(36, 44, FontWeight.Bold, -0.015),
    headlineLarge = style(30, 38, FontWeight.Bold, -0.01),
    headlineMedium = style(26, 34, FontWeight.SemiBold, -0.01),
    headlineSmall = style(22, 30, FontWeight.SemiBold, -0.005),
    titleLarge = style(20, 28, FontWeight.SemiBold),
    titleMedium = style(16, 24, FontWeight.SemiBold),
    titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 20, FontWeight.Normal),
    bodySmall = style(12, 16, FontWeight.Normal, 0.005),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.005),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.01),
    labelSmall = style(11, 16, FontWeight.Medium, 0.02),
)
