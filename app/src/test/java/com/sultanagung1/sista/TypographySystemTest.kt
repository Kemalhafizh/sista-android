package com.sultanagung1.sista

import androidx.compose.ui.text.font.FontFamily
import com.sultanagung1.sista.core.designsystem.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.min

class TypographySystemTest {

    @Test
    fun testSulaoneTypographyTokensComplete() {
        val styles = listOf(
            "displayLarge" to SulaoneTypography.displayLarge,
            "displayMedium" to SulaoneTypography.displayMedium,
            "displaySmall" to SulaoneTypography.displaySmall,
            "headlineLarge" to SulaoneTypography.headlineLarge,
            "headlineMedium" to SulaoneTypography.headlineMedium,
            "headlineSmall" to SulaoneTypography.headlineSmall,
            "titleLarge" to SulaoneTypography.titleLarge,
            "titleMedium" to SulaoneTypography.titleMedium,
            "titleSmall" to SulaoneTypography.titleSmall,
            "bodyLarge" to SulaoneTypography.bodyLarge,
            "bodyMedium" to SulaoneTypography.bodyMedium,
            "bodySmall" to SulaoneTypography.bodySmall,
            "labelLarge" to SulaoneTypography.labelLarge,
            "labelMedium" to SulaoneTypography.labelMedium,
            "labelSmall" to SulaoneTypography.labelSmall
        )

        for ((name, style) in styles) {
            assertNotNull("Style $name should not be null", style)
            assertTrue("Style $name fontSize should be positive", style.fontSize.value > 0f)
            assertTrue("Style $name lineHeight should be positive", style.lineHeight.value > 0f)
            assertEquals("Style $name should use PlusJakartaSansFontFamily", PlusJakartaSansFontFamily, style.fontFamily)
        }
    }

    @Test
    fun testDyslexicTypographyTokensComplete() {
        val styles = listOf(
            "displayLarge" to DyslexicTypography.displayLarge,
            "displayMedium" to DyslexicTypography.displayMedium,
            "displaySmall" to DyslexicTypography.displaySmall,
            "headlineLarge" to DyslexicTypography.headlineLarge,
            "headlineMedium" to DyslexicTypography.headlineMedium,
            "headlineSmall" to DyslexicTypography.headlineSmall,
            "titleLarge" to DyslexicTypography.titleLarge,
            "titleMedium" to DyslexicTypography.titleMedium,
            "titleSmall" to DyslexicTypography.titleSmall,
            "bodyLarge" to DyslexicTypography.bodyLarge,
            "bodyMedium" to DyslexicTypography.bodyMedium,
            "bodySmall" to DyslexicTypography.bodySmall,
            "labelLarge" to DyslexicTypography.labelLarge,
            "labelMedium" to DyslexicTypography.labelMedium,
            "labelSmall" to DyslexicTypography.labelSmall
        )

        for ((name, style) in styles) {
            assertNotNull("Style $name should not be null", style)
            assertTrue("Style $name fontSize should be positive", style.fontSize.value > 0f)
            assertTrue("Style $name lineHeight should be positive", style.lineHeight.value > 0f)
            assertEquals("Style $name should use DyslexicFontFamily", DyslexicFontFamily, style.fontFamily)
        }
    }

    @Test
    fun testDyslexicTypographySpacingAndLineHeightExpanded() {
        // Dyslexic mode must provide enhanced letter-spacing to prevent visual crowding
        assertTrue(
            "Dyslexic bodyLarge letterSpacing (${DyslexicTypography.bodyLarge.letterSpacing.value}) must exceed Sulaone (${SulaoneTypography.bodyLarge.letterSpacing.value})",
            DyslexicTypography.bodyLarge.letterSpacing.value > SulaoneTypography.bodyLarge.letterSpacing.value
        )
        assertTrue(
            "Dyslexic bodyMedium letterSpacing (${DyslexicTypography.bodyMedium.letterSpacing.value}) must exceed Sulaone (${SulaoneTypography.bodyMedium.letterSpacing.value})",
            DyslexicTypography.bodyMedium.letterSpacing.value > SulaoneTypography.bodyMedium.letterSpacing.value
        )
        assertTrue(
            "Dyslexic bodySmall letterSpacing (${DyslexicTypography.bodySmall.letterSpacing.value}) must exceed Sulaone (${SulaoneTypography.bodySmall.letterSpacing.value})",
            DyslexicTypography.bodySmall.letterSpacing.value > SulaoneTypography.bodySmall.letterSpacing.value
        )

        // Line heights must also be taller to prevent vertical crowding
        assertTrue(
            "Dyslexic bodyLarge lineHeight (${DyslexicTypography.bodyLarge.lineHeight.value}) must exceed Sulaone (${SulaoneTypography.bodyLarge.lineHeight.value})",
            DyslexicTypography.bodyLarge.lineHeight.value > SulaoneTypography.bodyLarge.lineHeight.value
        )
        assertTrue(
            "Dyslexic bodyMedium lineHeight (${DyslexicTypography.bodyMedium.lineHeight.value}) must exceed Sulaone (${SulaoneTypography.bodyMedium.lineHeight.value})",
            DyslexicTypography.bodyMedium.lineHeight.value > SulaoneTypography.bodyMedium.lineHeight.value
        )
    }

    @Test
    fun testFontFamiliesInitialized() {
        assertNotNull("PlusJakartaSansFontFamily should be initialized", PlusJakartaSansFontFamily)
        assertNotNull("AmiriFontFamily should be initialized", AmiriFontFamily)
        assertNotNull("JetBrainsMonoFontFamily should be initialized", JetBrainsMonoFontFamily)
        assertNotNull("DyslexicFontFamily should be initialized", DyslexicFontFamily)
    }

    @Test
    fun testQuranicAndMonospaceSpecializedStyles() {
        assertEquals("QuranicTextStyle must use Amiri font", AmiriFontFamily, QuranicTextStyle.fontFamily)
        assertTrue("Quranic text must be at least 20sp for Arabic legibility", QuranicTextStyle.fontSize.value >= 20f)
        assertTrue("Quranic text must have generous line height for harakat", QuranicTextStyle.lineHeight.value >= 36f)

        assertEquals("MonospaceTextStyle must use JetBrains Mono", JetBrainsMonoFontFamily, MonospaceTextStyle.fontFamily)
        assertTrue("Monospace font size should be at least 12sp", MonospaceTextStyle.fontSize.value >= 12f)
    }

    @Test
    fun testFontScaleCapFormula() {
        fun calculateCappedScale(systemDensityScale: Float, userMultiplier: Float): Float {
            return min(systemDensityScale * userMultiplier, 2.5f)
        }

        assertEquals(1.0f, calculateCappedScale(1.0f, 1.0f), 0.001f)
        assertEquals(1.5f, calculateCappedScale(1.0f, 1.5f), 0.001f)
        assertEquals(1.875f, calculateCappedScale(1.5f, 1.25f), 0.001f)
        assertEquals(2.5f, calculateCappedScale(2.0f, 1.5f), 0.001f) // Capped from 3.0f to 2.5f
        assertEquals(2.5f, calculateCappedScale(3.0f, 1.0f), 0.001f) // Capped from 3.0f to 2.5f
    }
}
