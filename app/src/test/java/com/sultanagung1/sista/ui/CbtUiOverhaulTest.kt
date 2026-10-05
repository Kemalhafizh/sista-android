package com.sultanagung1.sista.ui

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * The student CBT screens (list, token, room) are built on :core:ui like the
 * rest of the app, and keep the exam honest: no device-side unlock, no clock
 * or exam details the server didn't send, and every entry goes through the token.
 *
 * Replaces the earlier check that pinned the old Sulaone styling (Slate50,
 * 0.5dp borders, Emerald600) to these files.
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

    private val cbtDir = "feature/cbt/src/main/java/com/sultanagung1/sista/ui/cbt"
    private val screens = listOf("CbtExamListScreen.kt", "CbtTokenEntryScreen.kt", "CbtExamRoomScreen.kt")

    @Test
    fun screensUseTheSharedDesignSystem() {
        val hardcodedColor = Regex("""Color\s*\(\s*0x[0-9a-fA-F]{6,8}\s*\)""")
        screens.forEach { name ->
            val content = findSourceFile("$cbtDir/$name").readText()
            assertTrue("$name must use ShellTheme", content.contains("ShellTheme"))
            assertTrue("$name must use SistaTopBar", content.contains("SistaTopBar("))
            assertFalse("$name must not use SulaoneTopBar", content.contains("SulaoneTopBar("))
            assertFalse("$name must not hardcode colours", hardcodedColor.containsMatchIn(content))
        }
    }

    @Test
    fun noDeviceSideUnlockOrSupervisorPin() {
        val engine = findSourceFile("feature/cbt/src/main/java/com/sultanagung1/sista/core/security/CbtAntiCheatEngine.kt").readText()
        val room = findSourceFile("$cbtDir/CbtExamRoomScreen.kt").readText()
        listOf(engine, room).forEach { content ->
            assertFalse(content.contains("supervisorPin", ignoreCase = true))
            assertFalse(content.contains("unlockExam", ignoreCase = true))
        }
    }

    @Test
    fun noFabricatedExamClockOrDetails() {
        val vm = findSourceFile("$cbtDir/CbtViewModel.kt").readText()
        val room = findSourceFile("$cbtDir/CbtExamRoomScreen.kt").readText()
        val nav = findSourceFile("app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/CbtNavGraph.kt").readText()
        assertFalse("no fixed 90-minute clock", vm.contains("5400") || room.contains("5400"))
        assertFalse("no invented exam fallback", nav.contains("Ujian CBT Sultan Agung") || nav.contains("Mata Pelajaran"))
    }

    @Test
    fun everyExamEntryGoesThroughTheToken() {
        val sources = listOf(
            "core/common/src/main/java/com/sultanagung1/sista/core/notification/NotificationRouter.kt",
            "core/common/src/main/java/com/sultanagung1/sista/core/deeplink/DeepLinkRouter.kt",
            "app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/SettingsNavGraph.kt",
        )
        sources.forEach { path ->
            assertFalse("$path must open the token screen, not the room", findSourceFile(path).readText().contains("CbtRoom.createRoute"))
        }
    }
}
