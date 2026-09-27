package com.sultanagung1.sista.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The "Uji Push Notifikasi" simulator is a developer tool. Notification Center
 * is a bottom-nav tab for every student, parent and teacher, so the button and
 * its dialog must only render in debug builds.
 */
class NotificationDebugToolsGuardTest {

    private fun source(relativePath: String): String {
        val candidates = listOf(File("../$relativePath"), File(relativePath))
        val file = candidates.firstOrNull { it.exists() } ?: error("$relativePath not found")
        return file.readText()
    }

    private val screen = "feature/profile/src/main/java/com/sultanagung1/sista/ui/notifications/NotificationCenterScreen.kt"
    private val navGraph = "app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/CommunicationNavGraph.kt"

    @Test
    fun testDispatcherIsHiddenUnlessDebugToolsAreEnabled() {
        val content = source(screen)

        assertTrue(
            "showDebugTools must default to false so release callers never see the simulator",
            content.contains("showDebugTools: Boolean = false")
        )

        val fabBlock = content.substringAfter("floatingActionButton = {").substringBefore("snackbarHost =")
        assertTrue(
            "The test-push FAB must be wrapped in if (showDebugTools)",
            fabBlock.contains("if (showDebugTools)") &&
                fabBlock.indexOf("if (showDebugTools)") < fabBlock.indexOf("ExtendedFloatingActionButton")
        )
        assertTrue(
            "The test-push dialog must also be gated on showDebugTools",
            content.contains("if (showDebugTools && showTestDispatchDialog)")
        )
    }

    @Test
    fun navGraphEnablesDebugToolsOnlyForDebugBuilds() {
        val content = source(navGraph)
        assertTrue(
            "CommunicationNavGraph must pass BuildConfig.DEBUG to NotificationCenterScreen",
            content.contains("showDebugTools = BuildConfig.DEBUG")
        )
        assertFalse(
            "Debug tools must never be force-enabled",
            content.contains("showDebugTools = true")
        )
    }
}
