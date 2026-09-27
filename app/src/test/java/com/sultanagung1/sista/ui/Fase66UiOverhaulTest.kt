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
    fun testFase66_1_DarkGradientsEliminatedAndOffWhiteBackground() {
        val homeScreenFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt")
        assertTrue("HomeScreen.kt must exist", homeScreenFile.exists())

        val content = homeScreenFile.readText()

        // 66.1: Pemusnahan Variabel Gradasi Gelap
        assertFalse(
            "HomeScreen.kt must NOT declare or use headerGradient",
            content.contains("headerGradient")
        )
        assertFalse(
            "HomeScreen.kt must NOT declare or use streakGradient",
            content.contains("streakGradient")
        )

        // 66.1: Perbaikan Background to off-white Slate 50
        assertTrue(
            "HomeScreen.kt LazyColumn background must use Slate50 token",
            content.contains("Slate50")
        )
    }

    @Test
    fun testFase66_2_TransparentHeaderRestructuring() {
        val heroFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/sections/HomeHeroSection.kt")
        assertTrue("HomeHeroSection.kt must exist", heroFile.exists())

        val content = heroFile.readText()

        // 66.2: Hapus penerimaan parameter gradient dari definisi fungsi
        assertFalse(
            "HomeHeroSection function signature must NOT accept headerGradient",
            content.contains("headerGradient: Brush")
        )

        // 66.2: High-contrast Slate 900 for greetings in light mode
        assertTrue(
            "HomeHeroSection.kt must use Slate900 token for high contrast greetings",
            content.contains("Slate900")
        )

        // 66.2: Airy horizontal = 16.dp, vertical = 24.dp
        assertTrue(
            "HomeHeroSection.kt must use padding(horizontal = 16.dp, vertical = 24.dp)",
            content.contains("horizontal = 16.dp, vertical = 24.dp")
        )
    }

    @Test
    fun testFase66_3_MinimalQuickActionsAndCardBorders() {
        val quickActionsFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/sections/HomeQuickActions.kt")
        assertTrue("HomeQuickActions.kt must exist", quickActionsFile.exists())

        val content = quickActionsFile.readText()

        // 66.3: Semua tombol Card mematikan elevasi (0.dp)
        assertTrue(
            "HomeQuickActions.kt must disable elevation via cardElevation(0.dp)",
            content.contains("cardElevation(0.dp)")
        )

        // 66.3: Border tipis 0.5.dp Slate 200
        assertTrue(
            "HomeQuickActions.kt must apply BorderStroke(0.5.dp, ... Slate200)",
            content.contains("0.5.dp") && content.contains("Slate200")
        )

        // 66.3: Warna aksen Emerald600 untuk ikon vektor 24dp dan latar putih polos
        assertTrue(
            "HomeQuickActions.kt must use Emerald600 for central action icons",
            content.contains("Emerald600")
        )
        assertTrue(
            "HomeQuickActions.kt must use Color.White for action pills background",
            content.contains("Color.White")
        )
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
