package com.sultanagung1.sista.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * The display choices change what rebuilt screens draw. Before, ShellTheme
 * ignored them, so "Hitam pekat", "Kontras tinggi" and "Ramah disleksia" did
 * nothing on those screens.
 */
class DisplayPreferencesTest {

    @Test
    fun defaultsAreTheBrandTheme() {
        assertSame(LightColors, colorsFor(dark = false, DisplayPreferences()))
        assertSame(DarkColors, colorsFor(dark = true, DisplayPreferences()))
        assertSame(SistaTypography, typographyFor(DisplayPreferences()))
    }

    @Test
    fun amoledMakesTheDarkBackgroundTrueBlack() {
        val scheme = colorsFor(dark = true, DisplayPreferences(amoledBlack = true))
        assertEquals(Color.Black, scheme.background)
        assertEquals(Color.Black, scheme.surface)
        // Cards stay visible above the black.
        assertNotEquals(Color.Black, scheme.surfaceContainerLow)
    }

    @Test
    fun amoledDoesNothingInLightMode() {
        assertSame(LightColors, colorsFor(dark = false, DisplayPreferences(amoledBlack = true)))
    }

    @Test
    fun highContrastDropsMutedText() {
        val light = colorsFor(dark = false, DisplayPreferences(highContrast = true))
        assertEquals(Color.Black, light.onSurfaceVariant)
        assertEquals(Color.Black, light.outline)
        val dark = colorsFor(dark = true, DisplayPreferences(highContrast = true, amoledBlack = true))
        assertEquals(Color.White, dark.onSurface)
        assertEquals(Color.Black, dark.background)
    }

    @Test
    fun dyslexicFriendlySpacesLettersAndLines() {
        val body = typographyFor(DisplayPreferences(dyslexicFriendly = true)).bodyMedium
        assertEquals(0.06.em, body.letterSpacing)
        assertEquals((SistaTypography.bodyMedium.fontSize.value * 1.5f).sp, body.lineHeight)
        assertEquals(SistaTypography.bodyMedium.fontSize, body.fontSize)
    }
}
