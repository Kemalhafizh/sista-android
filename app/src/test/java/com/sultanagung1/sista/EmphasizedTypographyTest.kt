package com.sultanagung1.sista

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.sultanagung1.sista.core.designsystem.DyslexicTypography
import com.sultanagung1.sista.core.designsystem.SulaoneEmphasizedTypography
import com.sultanagung1.sista.core.designsystem.SulaoneTypography
import com.sultanagung1.sista.core.designsystem.emphasized
import com.sultanagung1.sista.core.designsystem.emphasizedWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 76.1 Part B — verifies SulaoneEmphasizedTypography is a genuine
 * Material 3 Expressive "emphasized" tier: identical size/line-height/
 * letter-spacing to the baseline (the property that makes it safe to ship
 * without an emulator pass — nothing that affects layout changes), with a
 * strictly heavier font weight on every one of the 15 type-scale roles
 * (the property that makes it actually an emphasis tier and not a no-op).
 */
class EmphasizedTypographyTest {

    // Typography has no built-in way to enumerate its 15 named styles, so
    // list them explicitly — this also documents the full role set in one place.
    private fun roles(typography: Typography): Map<String, TextStyle> = mapOf(
        "displayLarge" to typography.displayLarge,
        "displayMedium" to typography.displayMedium,
        "displaySmall" to typography.displaySmall,
        "headlineLarge" to typography.headlineLarge,
        "headlineMedium" to typography.headlineMedium,
        "headlineSmall" to typography.headlineSmall,
        "titleLarge" to typography.titleLarge,
        "titleMedium" to typography.titleMedium,
        "titleSmall" to typography.titleSmall,
        "bodyLarge" to typography.bodyLarge,
        "bodyMedium" to typography.bodyMedium,
        "bodySmall" to typography.bodySmall,
        "labelLarge" to typography.labelLarge,
        "labelMedium" to typography.labelMedium,
        "labelSmall" to typography.labelSmall
    )

    @Test
    fun testEmphasizedMatchesBaselineOnEveryLayoutAffectingDimension() {
        val baseline = roles(SulaoneTypography)
        val emphasized = roles(SulaoneEmphasizedTypography)

        for ((role, baseStyle) in baseline) {
            val empStyle = emphasized.getValue(role)
            assertEquals("$role: fontSize must match baseline exactly (zero layout risk)", baseStyle.fontSize, empStyle.fontSize)
            assertEquals("$role: lineHeight must match baseline exactly (zero layout risk)", baseStyle.lineHeight, empStyle.lineHeight)
            assertEquals("$role: letterSpacing must match baseline exactly (zero layout risk)", baseStyle.letterSpacing, empStyle.letterSpacing)
        }
    }

    @Test
    fun testEmphasizedIsStrictlyHeavierThanBaselineOnEveryRole() {
        val baseline = roles(SulaoneTypography)
        val emphasized = roles(SulaoneEmphasizedTypography)

        for ((role, baseStyle) in baseline) {
            val empStyle = emphasized.getValue(role)
            val baseWeight = baseStyle.fontWeight?.weight ?: 0
            val empWeight = empStyle.fontWeight?.weight ?: 0
            assertTrue(
                "$role: emphasized weight ($empWeight) must be heavier than baseline ($baseWeight) — otherwise this tier does nothing",
                empWeight > baseWeight
            )
        }
    }

    @Test
    fun testEmphasizedWeightStepsUpOneTierAndCapsAtExtraBold() {
        assertEquals(FontWeight.Medium, emphasizedWeight(FontWeight.Normal))
        assertEquals(FontWeight.Medium, emphasizedWeight(null)) // unspecified weight renders as Normal
        assertEquals(FontWeight.SemiBold, emphasizedWeight(FontWeight.Medium))
        assertEquals(FontWeight.Bold, emphasizedWeight(FontWeight.SemiBold))
        assertEquals(FontWeight.ExtraBold, emphasizedWeight(FontWeight.Bold))
        assertEquals(FontWeight.ExtraBold, emphasizedWeight(FontWeight.ExtraBold))
        assertEquals(FontWeight.ExtraBold, emphasizedWeight(FontWeight.Black))
    }

    @Test
    fun testEmphasizedExtensionAgreesWithTheReferenceTableForEveryRole() {
        // SulaoneEmphasizedTypography is the spec table; .emphasized() is what
        // UI code uses. They must never drift apart.
        val baseline = roles(SulaoneTypography)
        val table = roles(SulaoneEmphasizedTypography)
        for ((role, style) in baseline) {
            assertEquals("$role: .emphasized() must match the reference table", table.getValue(role), style.emphasized())
        }
    }

    @Test
    fun testEmphasizedKeepsDyslexicFontFamilyAndMetrics() {
        // The reason UI code must use .emphasized() instead of the Plus Jakarta
        // Sans reference table: in Dyslexic-friendly mode the theme swaps to
        // Lexend, and emphasis must not silently switch the font back.
        for ((role, style) in roles(DyslexicTypography)) {
            val emphasized = style.emphasized()
            assertEquals("$role: font family must stay Lexend", style.fontFamily, emphasized.fontFamily)
            assertEquals("$role: size must not change", style.fontSize, emphasized.fontSize)
            assertEquals("$role: line height must not change", style.lineHeight, emphasized.lineHeight)
            assertTrue("$role: must get heavier", (emphasized.fontWeight?.weight ?: 0) > (style.fontWeight?.weight ?: 0))
        }
    }

    @Test
    fun testNoRoleWasBumpedPastExtraBold() {
        // FontWeight.ExtraBold = 800; Black (900) was never declared as a
        // downloadable font resource for PlusJakartaSansFontFamily, so a
        // TextStyle referencing it would silently fall back to the nearest
        // declared weight at render time rather than fail — this test
        // catches that mistake at the token level instead.
        val emphasized = roles(SulaoneEmphasizedTypography)
        for ((role, style) in emphasized) {
            val weight = style.fontWeight?.weight ?: 0
            assertTrue("$role: weight $weight exceeds ExtraBold (800) — no such font resource is declared", weight <= 800)
        }
    }
}
