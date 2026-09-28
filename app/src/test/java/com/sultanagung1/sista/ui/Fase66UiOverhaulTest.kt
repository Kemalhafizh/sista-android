package com.sultanagung1.sista.ui

import androidx.compose.ui.graphics.Color
import com.sultanagung1.sista.core.designsystem.Emerald600
import com.sultanagung1.sista.core.designsystem.Slate200
import com.sultanagung1.sista.core.designsystem.Slate50
import com.sultanagung1.sista.core.designsystem.Slate900
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit test verifying compliance with FASE 66 (DEEP-DIVE PERBAIKAN UI BERANDA)
 * and FASE 67 (FIGMA-DRIVEN FRONTEND OVERHAUL token parity).
 */
class Fase66UiOverhaulTest {

    private fun findSourceFile(relativePath: String): File {
        val filename = relativePath.substringAfterLast("/")
        val candidates = listOf(
            File(relativePath),
            File("app/$relativePath"),
            File("../$relativePath"),
            File("../../$relativePath")
        )
        candidates.firstOrNull { it.exists() }?.let { return it }

        val rootDir = File("..").takeIf { File("..", "settings.gradle").exists() || File("..", "build.gradle").exists() } ?: File(".")
        return rootDir.walkTopDown().firstOrNull { it.isFile && it.name == filename }
            ?: throw IllegalStateException("Cannot locate $relativePath in any candidate paths")
    }

    @Test
    fun testFase66_HomeIsBuiltFromTheDesignSystem() {
        val home = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt").readText()
        val sections = listOf("HomeHeader.kt", "HomeTodayCard.kt", "HomeQuickAccess.kt")
            .joinToString("\n") { findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/sections/$it").readText() }

        // 66.1: no dark gradient headers; the theme decides light/dark.
        assertFalse(home.contains("headerGradient") || home.contains("streakGradient"))
        assertTrue("Follows the app theme", home.contains("ShellTheme {"))
        // 66.2/66.3: flat, bordered surfaces come from the design system, not ad-hoc values.
        assertTrue(sections.contains("SistaCard(") && sections.contains("FeatureTile("))
        assertFalse("No hardcoded colours", Regex("""Color\(0x""").containsMatchIn(home + sections))
    }

    @Test
    fun testFase67_TokenParity() {
        // Assert design token values match Figma specs
        assertEquals("Slate 50 hex parity", Color(0xFFF8FAFC), Slate50)
        assertEquals("Slate 200 hex parity", Color(0xFFE2E8F0), Slate200)
        assertEquals("Slate 900 hex parity", Color(0xFF0F172A), Slate900)
        assertEquals("Emerald 600 hex parity", Color(0xFF107047), Emerald600)
    }
}
