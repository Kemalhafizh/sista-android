package com.sultanagung1.sista.ui

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit test verifying compliance of CBT Screens (CbtExamListScreen & CbtTokenEntryScreen)
 * overhaul with SISTA Modern Design System: Off-white canvas, 0dp elevation, 0.5dp border,
 * Emerald600 accents, and 48dp touch targets.
 */
class CbtUiOverhaulTest {

    private fun findSourceFile(relativePath: String): File {
        val candidates = listOf(
            File(relativePath),
            File("app/$relativePath"),
            File("../$relativePath")
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException("Cannot locate $relativePath in any candidate paths")
    }

    @Test
    fun testCbtExamListScreenDesignCompliance() {
        val file = findSourceFile("src/main/java/com/sultanagung1/sista/ui/cbt/CbtExamListScreen.kt")
        assertTrue("CbtExamListScreen.kt must exist", file.exists())

        val content = file.readText()

        // 1. Off-white background
        assertTrue(
            "CbtExamListScreen must use Slate50 background token",
            content.contains("Slate50")
        )

        // 2. Flat elevation
        assertTrue(
            "CbtExamListScreen cards must use 0.dp elevation",
            content.contains("cardElevation(0.dp)")
        )

        // 3. Hairline borders 0.5dp
        assertTrue(
            "CbtExamListScreen must apply 0.5.dp border with Slate200",
            content.contains("0.5.dp") && content.contains("Slate200")
        )

        // 4. Accent Emerald600 for key indicators and buttons
        assertTrue(
            "CbtExamListScreen must use Emerald600 accent",
            content.contains("Emerald600")
        )

        // 5. 48dp WCAG touch targets
        assertTrue(
            "CbtExamListScreen must adhere to 48dp touch targets",
            content.contains("sulaoneInteractiveTouchTarget(48.dp)")
        )

        // 6. No hardcoded hex colors
        val hardcodedRegex = Regex("""Color\s*\(\s*0x[0-9a-fA-F]{6,8}\s*\)""")
        assertFalse(
            "CbtExamListScreen must not contain hardcoded Color(0x...)",
            hardcodedRegex.containsMatchIn(content)
        )
    }

    @Test
    fun testCbtTokenEntryScreenDesignCompliance() {
        val file = findSourceFile("src/main/java/com/sultanagung1/sista/ui/cbt/CbtTokenEntryScreen.kt")
        assertTrue("CbtTokenEntryScreen.kt must exist", file.exists())

        val content = file.readText()

        // 1. Off-white background
        assertTrue(
            "CbtTokenEntryScreen must use Slate50 background token",
            content.contains("Slate50")
        )

        // 2. Flat elevation
        assertTrue(
            "CbtTokenEntryScreen cards must use 0.dp elevation",
            content.contains("cardElevation(0.dp)")
        )

        // 3. Hairline borders 0.5dp
        assertTrue(
            "CbtTokenEntryScreen must apply 0.5.dp border with Slate200",
            content.contains("0.5.dp") && content.contains("Slate200")
        )

        // 4. Accent Emerald600 for key indicators and buttons
        assertTrue(
            "CbtTokenEntryScreen must use Emerald600 accent",
            content.contains("Emerald600")
        )

        // 5. 48dp WCAG touch targets
        assertTrue(
            "CbtTokenEntryScreen must adhere to 48dp touch targets",
            content.contains("sulaoneInteractiveTouchTarget(48.dp)")
        )

        // 6. No hardcoded hex colors
        val hardcodedRegex = Regex("""Color\s*\(\s*0x[0-9a-fA-F]{6,8}\s*\)""")
        assertFalse(
            "CbtTokenEntryScreen must not contain hardcoded Color(0x...)",
            hardcodedRegex.containsMatchIn(content)
        )
    }
}
