package com.sultanagung1.sista.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every destination must go through FeatureGate (guardedComposable), so a
 * screen can only be shown to an account whose server-given features open it
 * — whichever way it is reached. These checks keep a new destination from
 * slipping in with a plain `composable(...)`, and keep the old local role
 * checks from coming back.
 */
class NavigationGuardTest {

    private fun projectRoot(): File =
        generateSequence(File("").absoluteFile) { it.parentFile }
            .first { File(it, "feature").isDirectory && File(it, "core").isDirectory }

    private val kotlinSources: List<File> by lazy {
        listOf("app", "feature", "core").flatMap { dir ->
            File(projectRoot(), dir).walkTopDown()
                .filter { it.isFile && it.extension == "kt" && "/src/main/" in it.path }
                .toList()
        }
    }

    @Test
    fun `only the gate itself declares raw composable destinations`() {
        val offenders = kotlinSources
            .filter { it.name != "FeatureGate.kt" }
            .filter { file ->
                val text = file.readText()
                "import androidx.navigation.compose.composable" in text ||
                    Regex("""(?<![\w.])composable\(\s*(route\s*=\s*)?Screen\.""").containsMatchIn(text)
            }
            .map { it.name }
        assertEquals(emptyList<String>(), offenders)
    }

    @Test
    fun `nav graphs declare destinations through the gate`() {
        val graphs = File(projectRoot(), "app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs")
            .listFiles { f -> f.extension == "kt" }!!.toList()
        assertTrue(graphs.isNotEmpty())
        graphs.forEach { graph ->
            assertTrue("${graph.name} declares no guarded destination", "guardedComposable(" in graph.readText())
        }
    }

    @Test
    fun `no screen decides access from a local role string`() {
        val offenders = kotlinSources.filter { "RoleGuardedScreen(" in it.readText() }.map { it.name }
        assertEquals(emptyList<String>(), offenders)
    }
}
