package com.sultanagung1.sista.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Colours live in one place (theme/Color.kt). Components and screens use
 * MaterialTheme / SistaTheme roles, which is what keeps light and dark mode
 * correct — the old design system ended up with ~2,000 raw colour references
 * and hand-rolled `isDark` checks.
 */
class DesignSystemGuardTest {

    private val sources = File("src/main/java").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    @Test
    fun `no hardcoded colours outside the theme`() {
        val offenders = sources
            .filter { it.name != "Color.kt" }
            .filter { Regex("""Color\(0x|Color\.(White|Black|Red|Green|Blue|Gray)\b""").containsMatchIn(it.readText()) }
            .map { it.name }
        assertEquals(emptyList<String>(), offenders)
    }

    @Test
    fun `components never decide dark mode themselves`() {
        val offenders = sources.filter { it.readText().contains("isSystemInDarkTheme()") && it.name != "Theme.kt" }.map { it.name }
        assertEquals(emptyList<String>(), offenders)
    }
}
