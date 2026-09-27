package com.sultanagung1.sista.ui

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit test verifying compliance of BillingScreen overhaul
 * with SISTA Modern Design System: Off-white canvas, 0dp elevation, 0.5dp border,
 * Emerald600 accents, and 48dp touch targets.
 */
class BillingUiOverhaulTest {

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
    fun testBillingScreenDesignCompliance() {
        val file = findSourceFile("feature/finance/src/main/java/com/sultanagung1/sista/ui/finance/BillingScreen.kt")
        assertTrue("BillingScreen.kt must exist", file.exists())

        val content = file.readText()

        // 1. Off-white background
        assertTrue(
            "BillingScreen must use Slate50 background token",
            content.contains("Slate50")
        )

        // 2. Flat elevation
        assertTrue(
            "BillingScreen cards must use 0.dp elevation",
            content.contains("cardElevation(0.dp)")
        )

        // 3. Hairline borders 0.5dp
        assertTrue(
            "BillingScreen must apply 0.5.dp border with Slate200",
            content.contains("0.5.dp") && content.contains("Slate200")
        )

        // 4. Accent Emerald600 for key indicators and buttons
        assertTrue(
            "BillingScreen must use Emerald600 accent",
            content.contains("Emerald600")
        )

        // 5. 48dp WCAG touch targets
        assertTrue(
            "BillingScreen must adhere to 48dp touch targets",
            content.contains("sulaoneInteractiveTouchTarget(48.dp)")
        )

        // 6. No hardcoded hex colors
        val hardcodedRegex = Regex("""Color\s*\(\s*0x[0-9a-fA-F]{6,8}\s*\)""")
        assertFalse(
            "BillingScreen must not contain hardcoded Color(0x...)",
            hardcodedRegex.containsMatchIn(content)
        )
    }
}
