package com.sultanagung1.sista

import androidx.compose.ui.graphics.Color
import com.sultanagung1.sista.core.designsystem.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class DesignSystemTokensTest {

    // Relative luminance calculation based on WCAG 2.2 specifications
    private fun calculateLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.04045) {
                c / 12.92
            } else {
                ((c + 0.055) / 1.055).pow(2.4)
            }
        }
        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun testAllThemesHaveConfiguredContainerTokens() {
        val themes = listOf(
            "Light" to LightColorScheme,
            "Dark" to DarkColorScheme,
            "Amoled" to AmoledColorScheme,
            "HighContrast" to HighContrastColorScheme,
            "NightStudy" to NightStudyColorScheme,
            "ExamMode" to ExamModeColorScheme,
            "RamadhanGold" to RamadhanGoldColorScheme
        )

        for ((name, scheme) in themes) {
            assertNotEquals("Theme $name: surfaceDim should not be Unspecified", Color.Unspecified, scheme.surfaceDim)
            assertNotEquals("Theme $name: surfaceBright should not be Unspecified", Color.Unspecified, scheme.surfaceBright)
            assertNotEquals("Theme $name: surfaceContainerLowest should not be Unspecified", Color.Unspecified, scheme.surfaceContainerLowest)
            assertNotEquals("Theme $name: surfaceContainerLow should not be Unspecified", Color.Unspecified, scheme.surfaceContainerLow)
            assertNotEquals("Theme $name: surfaceContainer should not be Unspecified", Color.Unspecified, scheme.surfaceContainer)
            assertNotEquals("Theme $name: surfaceContainerHigh should not be Unspecified", Color.Unspecified, scheme.surfaceContainerHigh)
            assertNotEquals("Theme $name: surfaceContainerHighest should not be Unspecified", Color.Unspecified, scheme.surfaceContainerHighest)
        }
    }

    @Test
    fun testWcagContrastRatios() {
        // WCAG AA requirement: >= 4.5:1 for normal text
        val lightBgContrast = calculateContrastRatio(LightColorScheme.onBackground, LightColorScheme.background)
        assertTrue("Light theme onBackground contrast $lightBgContrast should be >= 4.5", lightBgContrast >= 4.5)

        val lightSurfaceContrast = calculateContrastRatio(LightColorScheme.onSurface, LightColorScheme.surface)
        assertTrue("Light theme onSurface contrast $lightSurfaceContrast should be >= 4.5", lightSurfaceContrast >= 4.5)

        val darkBgContrast = calculateContrastRatio(DarkColorScheme.onBackground, DarkColorScheme.background)
        assertTrue("Dark theme onBackground contrast $darkBgContrast should be >= 4.5", darkBgContrast >= 4.5)

        val darkSurfaceContrast = calculateContrastRatio(DarkColorScheme.onSurface, DarkColorScheme.surface)
        assertTrue("Dark theme onSurface contrast $darkSurfaceContrast should be >= 4.5", darkSurfaceContrast >= 4.5)

        // WCAG AAA for High Contrast mode: >= 7.0:1
        val highContrast = calculateContrastRatio(HighContrastColorScheme.onSurface, HighContrastColorScheme.surface)
        assertTrue("High contrast onSurface contrast $highContrast should be >= 7.0", highContrast >= 7.0)
    }

    @Test
    fun testExtendedColorsTokens() {
        val extended = SulaoneExtendedColors(
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
        assertNotEquals(Color.Unspecified, extended.islamicGreen)
        assertNotEquals(Color.Unspecified, extended.islamicGold)
        assertNotEquals(Color.Unspecified, extended.successGreen)
        assertNotEquals(Color.Unspecified, extended.warningAmber)
        assertNotEquals(Color.Unspecified, extended.dangerRose)
        assertNotEquals(Color.Unspecified, extended.infoBlue)
        assertNotEquals(Color.Unspecified, extended.brandPurple)
        assertNotEquals(Color.Unspecified, extended.surfaceSubtle)
        assertNotEquals(Color.Unspecified, extended.borderSubtle)
    }

    @Test
    fun testNoHardcodedColorsInUiScreens() {
        val candidates = listOf(
            File("src/main/java/com/sultanagung1/sista/ui"),
            File("app/src/main/java/com/sultanagung1/sista/ui"),
            File("../app/src/main/java/com/sultanagung1/sista/ui")
        )
        val rootDir = candidates.firstOrNull { it.exists() && it.isDirectory }
        assertNotNull("UI directory should exist in one of the candidate paths", rootDir)

        val hardcodedRegex = Regex("""Color\s*\(\s*0x[0-9a-fA-F]{6,8}\s*\)""")
        val violations = mutableListOf<String>()

        rootDir!!.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            // Exclude theme definition file where palettes are intentionally defined
            if (!file.name.contains("Color.kt")) {
                file.readLines().forEachIndexed { index, line ->
                    if (hardcodedRegex.containsMatchIn(line)) {
                        violations.add("${file.name}:${index + 1}: ${line.trim()}")
                    }
                }
            }
        }

        assertTrue(
            "Found ${violations.size} hardcoded color usages in UI screens:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }
}
