package com.sultanagung1.sista.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every pull-to-refresh must reflect a real request. Schedule, Notifications
 * and the CBT exam list used to flip a local `isRefreshing` flag, wait a
 * fixed `delay(600)` and hide the spinner — whether or not the server had
 * answered (Schedule did not even call it). The spinner now follows the
 * ViewModel's loading state, like Home and the library catalogue.
 */
class HonestPullToRefreshGuardTest {

    private fun projectRoot(): File =
        generateSequence(File("").absoluteFile) { it.parentFile }
            .first { File(it, "feature").isDirectory && File(it, "core").isDirectory }

    private fun screensWithPullToRefresh(): List<File> =
        File(projectRoot(), "feature").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && "/src/main/" in it.path.replace('\\', '/') }
            .filter { it.readText().contains("SulaonePullToRefreshBox(") }
            .toList()

    @Test
    fun `no screen fakes a refresh with a local flag and a fixed delay`() {
        val screens = screensWithPullToRefresh()
        assertTrue("expected pull-to-refresh screens to scan", screens.size >= 5)

        val offenders = screens.filter { file ->
            val src = file.readText()
            Regex("""isRefreshing\s*=\s*isRefreshing\b""").containsMatchIn(src) ||
                Regex("""onRefresh\s*=\s*\{[^}]*delay\(\d+""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(src)
        }.map { it.name }

        assertEquals(emptyList<String>(), offenders)
    }

    @Test
    fun `schedule refresh actually reloads from the server`() {
        val screen = File(projectRoot(), "feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/ScheduleScreen.kt").readText()
        assertTrue(screen.contains("viewModel.loadSchedule()"))
        assertTrue(screen.contains("refreshing = refreshRequested && uiState.isScheduleRefreshing"))
    }
}
