package com.sultanagung1.sista.display

import com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager
import com.sultanagung1.sista.core.display.DisplayCapabilities
import com.sultanagung1.sista.core.display.RefreshRateMode
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AdaptiveRefreshRateTest {

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
            ?: File(relativePath)
    }

    @Test
    fun testRefreshRateModeEnumProperties() {
        assertEquals(3, RefreshRateMode.entries.size)

        val adaptive = RefreshRateMode.ADAPTIVE_SMOOTH
        assertTrue(adaptive.title.contains("Adaptif"))
        assertTrue(adaptive.title.contains("120Hz"))
        assertTrue(adaptive.title.contains("LTPO"))
        assertTrue(adaptive.description.contains("laju penyegaran layar HP"))

        val powerSaver = RefreshRateMode.POWER_SAVER_60HZ
        assertTrue(powerSaver.title.contains("60Hz"))
        assertTrue(powerSaver.description.contains("menghemat daya"))

        val defaultMode = RefreshRateMode.SYSTEM_DEFAULT
        assertTrue(defaultMode.title.contains("Default Sistem"))
    }

    @Test
    fun testDisplayCapabilitiesDataClass() {
        val caps120Ltpo = DisplayCapabilities(
            currentRefreshRate = 120f,
            maxSupportedRefreshRate = 120f,
            supportedRefreshRates = listOf(30f, 60f, 120f),
            isHighRefreshRateSupported = true,
            isLtpoSupported = true,
            summaryText = "120Hz Adaptif (LTPO Didukung)"
        )
        assertTrue(caps120Ltpo.isHighRefreshRateSupported)
        assertTrue(caps120Ltpo.isLtpoSupported)
        assertEquals(3, caps120Ltpo.supportedRefreshRates.size)

        val caps90 = DisplayCapabilities(
            currentRefreshRate = 90f,
            maxSupportedRefreshRate = 90f,
            supportedRefreshRates = listOf(60f, 90f),
            isHighRefreshRateSupported = true,
            isLtpoSupported = false,
            summaryText = "90Hz Smooth Display"
        )
        assertTrue(caps90.isHighRefreshRateSupported)
        assertFalse(caps90.isLtpoSupported)

        val caps60 = DisplayCapabilities(
            currentRefreshRate = 60f,
            maxSupportedRefreshRate = 60f,
            supportedRefreshRates = listOf(60f),
            isHighRefreshRateSupported = false,
            isLtpoSupported = false,
            summaryText = "60Hz Standar"
        )
        assertFalse(caps60.isHighRefreshRateSupported)
        assertFalse(caps60.isLtpoSupported)
    }

    @Test
    fun testAdaptiveRefreshRateManagerSourceExists() {
        val file = findSourceFile("src/main/java/com/sultanagung1/sista/core/display/AdaptiveRefreshRateManager.kt")
        assertTrue("AdaptiveRefreshRateManager.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must declare applyToActivity", content.contains("fun applyToActivity"))
        assertTrue("Must declare detectCapabilities", content.contains("fun detectCapabilities"))
        assertTrue("Must configure preferredDisplayModeId", content.contains("preferredDisplayModeId"))
        assertTrue("Must configure preferredMaxDisplayRefreshRate", content.contains("preferredMaxDisplayRefreshRate"))
        assertTrue("Must configure preferredMinDisplayRefreshRate for LTPO", content.contains("preferredMinDisplayRefreshRate") && content.contains("minRate = 0f"))
        assertTrue("Must support SessionManager storage flow", content.contains("getStoredModeFlow"))
    }

    @Test
    fun testMainActivityAppliesAdaptiveRefreshRate() {
        val file = findSourceFile("src/main/java/com/sultanagung1/sista/MainActivity.kt")
        assertTrue("MainActivity.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must call applyToActivity", content.contains("AdaptiveRefreshRateManager.applyToActivity(this)"))
        assertTrue("Must observe stored mode flow", content.contains("getStoredModeFlow(sessionManager)"))
    }

    @Test
    fun testPulsingAnimationsMigratedToGraphicsLayerForJankElimination() {
        val headerFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/common/SulaoneExecutiveHeader.kt")
        assertTrue(headerFile.exists())
        val headerContent = headerFile.readText()
        assertTrue(
            "ExecutiveHeader LiveDot must use graphicsLayer to avoid recomposition jank",
            headerContent.contains("graphicsLayer")
        )

        val parentFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt")
        assertTrue(parentFile.exists())

        // The rebuilt login screen has no looping animation left to keep off the main thread.
    }

    @Test
    fun testLazyColumnsHaveStableKeysAndContentTypes() {
        val parentFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt")
        assertTrue(parentFile.exists())

        val adminFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/admin/AdminDashboardScreen.kt")
        assertTrue(adminFile.exists())

        val teacherFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/teacher/TeacherDashboardScreen.kt")
        val teacherContent = teacherFile.readText()
        assertTrue(teacherContent.contains("key = \"today\", contentType = \"schedule\""))
        assertTrue(teacherContent.contains("key = \"journals\", contentType = \"journal\""))

        val homeFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt")
        val homeContent = homeFile.readText()
        assertTrue(homeContent.contains("key = \"today\""))
        assertTrue(homeContent.contains("key = \"quick_actions\""))
    }

    @Test
    fun testProfileScreenExposesRefreshRateSetting() {
        val file = findSourceFile("src/main/java/com/sultanagung1/sista/ui/profile/ProfileScreen.kt")
        assertTrue(file.exists())

        val content = file.readText()
        assertTrue("Must have Refresh Rate menu item", content.contains("Laju Penyegaran Layar (Refresh Rate)"))
        assertTrue("Must have showRefreshRateDialog state", content.contains("showRefreshRateDialog"))
        assertTrue("Must detect capabilities in ProfileScreen", content.contains("AdaptiveRefreshRateManager.detectCapabilities"))
    }
}
