package com.sultanagung1.sista.display

import com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager
import com.sultanagung1.sista.core.display.DisplayCapabilities
import com.sultanagung1.sista.core.display.RefreshRateMode
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AdaptiveRefreshRateTest {

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
        val file = File("src/main/java/com/sultanagung1/sista/core/display/AdaptiveRefreshRateManager.kt")
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
        val file = File("src/main/java/com/sultanagung1/sista/MainActivity.kt")
        assertTrue("MainActivity.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must call applyToActivity", content.contains("AdaptiveRefreshRateManager.applyToActivity(this)"))
        assertTrue("Must observe stored mode flow", content.contains("getStoredModeFlow(sessionManager)"))
    }

    @Test
    fun testPulsingAnimationsMigratedToGraphicsLayerForJankElimination() {
        val headerFile = File("src/main/java/com/sultanagung1/sista/ui/common/SulaoneExecutiveHeader.kt")
        assertTrue(headerFile.exists())
        val headerContent = headerFile.readText()
        assertTrue(
            "ExecutiveHeader LiveDot must use graphicsLayer to avoid recomposition jank",
            headerContent.contains("graphicsLayer { this.alpha = alpha }")
        )

        val parentFile = File("src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt")
        assertTrue(parentFile.exists())
        val parentContent = parentFile.readText()
        assertTrue(
            "Parent dashboard gate presence must use graphicsLayer",
            parentContent.contains("graphicsLayer { this.alpha = pulseAlpha }")
        )

        val loginFile = File("src/main/java/com/sultanagung1/sista/ui/auth/LoginScreen.kt")
        assertTrue(loginFile.exists())
        val loginContent = loginFile.readText()
        assertTrue(
            "Login screen aura must use graphicsLayer",
            loginContent.contains("graphicsLayer { this.alpha = auraGlow }")
        )
    }

    @Test
    fun testLazyColumnsHaveStableKeysAndContentTypes() {
        val parentFile = File("src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt")
        assertTrue(parentFile.readText().contains("key = { it.id }"))

        val adminFile = File("src/main/java/com/sultanagung1/sista/ui/admin/AdminDashboardScreen.kt")
        val adminContent = adminFile.readText()
        assertTrue(adminContent.contains("contentType = { \"alert\" }"))
        assertTrue(adminContent.contains("contentType = { \"approval\" }"))

        val teacherFile = File("src/main/java/com/sultanagung1/sista/ui/teacher/TeacherDashboardScreen.kt")
        val teacherContent = teacherFile.readText()
        assertTrue(teacherContent.contains("contentType = { \"schedule\" }"))
        assertTrue(teacherContent.contains("contentType = { \"journal\" }"))

        val homeFile = File("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt")
        val homeContent = homeFile.readText()
        assertTrue(homeContent.contains("key = \"hero_section\""))
        assertTrue(homeContent.contains("key = \"prayer_widget\""))
        assertTrue(homeContent.contains("key = \"quick_actions\""))
        assertTrue(homeContent.contains("key = \"schedule_preview\""))
    }

    @Test
    fun testProfileScreenExposesRefreshRateSetting() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/profile/ProfileScreen.kt")
        assertTrue(file.exists())

        val content = file.readText()
        assertTrue("Must have Refresh Rate menu item", content.contains("Laju Penyegaran Layar (Refresh Rate)"))
        assertTrue("Must have showRefreshRateDialog state", content.contains("showRefreshRateDialog"))
        assertTrue("Must detect capabilities in ProfileScreen", content.contains("AdaptiveRefreshRateManager.detectCapabilities"))
    }
}
