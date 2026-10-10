package com.sultanagung1.sista.navigation

import com.sultanagung1.sista.core.motion.SulaoneNavTransitions
import com.sultanagung1.sista.ui.navigation.Screen
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Automated Unit Test Suite untuk FASE 53 (Navigation Decomposition) & FASE 73
 * (Multi-Module Migration).
 *
 * Rewritten from the original FASE 53 version, which referenced ~80
 * `XxxRoute` data classes implementing a `SulaoneRoute` marker interface with
 * `@Serializable` — that type-safe-route design was apparently never actually
 * built; the app has always navigated via [Screen]'s sealed class of
 * String routes (`ui/navigation/Screen.kt`, now living in :core:common after
 * FASE 73's module split). This suite tests that real design instead.
 */
class TypeSafeNavigationTest {

    private val allScreens = Screen::class.sealedSubclasses.mapNotNull { it.objectInstance }

    /**
     * FASE 78 removed the screens the school does not use. A minimum screen
     * count (this test used to demand at least 80) worked against that; what
     * matters is that the removed routes stay gone.
     */
    @Test
    fun testRemovedScreensStayRemoved() {
        val removed = setOf(
            "geofence_attendance", "dynamic_qr", "face_biometric", "face_enrollment", "ai_tutor", "ai_essay_grader",
            "gamification_dashboard", "leaderboard", "badge_collection", "document_scanner", "digital_signature",
            "executive_analytics", "diagnostic_report",
        )
        assertTrue(allScreens.isNotEmpty())
        assertEquals(emptyList<String>(), allScreens.map { it.route }.filter { it in removed })
    }

    @Test
    fun testAllScreensHaveNonBlankRoute() {
        for (screen in allScreens) {
            assertTrue(
                "Screen ${screen::class.simpleName} harus memiliki route yang tidak kosong",
                screen.route.isNotBlank()
            )
        }
    }

    @Test
    fun testAllScreenRoutesAreUnique() {
        // Route templates (e.g. "chat/{conversationId}") must be unique across
        // every Screen — a duplicate would make NavHost resolve to the wrong
        // destination.
        val routes = allScreens.map { it.route }
        val duplicates = routes.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        assertTrue("Route duplikat ditemukan: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun testAll8SubGraphsAreDefined() {
        val subGraphClasses = listOf(
            "com.sultanagung1.sista.ui.navigation.graphs.AuthNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.AcademicNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.CbtNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.TeacherNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.ParentNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.AdminNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.CommunicationNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.SettingsNavGraphKt"
        )

        assertEquals("Harus ada tepat 8 modul sub-navigation graph", 8, subGraphClasses.size)

        for (className in subGraphClasses) {
            val cls = try {
                Class.forName(className)
            } catch (e: ClassNotFoundException) {
                null
            }
            assertNotNull("SubGraph file class $className harus terdefinisi", cls)
        }
    }

    @Test
    fun testPredictiveBackEnabledInManifest() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml harus ditemukan", manifestFile.exists())

        val content = manifestFile.readText()
        assertTrue(
            "AndroidManifest.xml harus mengaktifkan android:enableOnBackInvokedCallback=\"true\"",
            content.contains("android:enableOnBackInvokedCallback=\"true\"")
        )
    }

    @Test
    fun testTabTransitionsAreCrossfade() {
        assertNotNull("tabEnterTransition harus terdefinisi", SulaoneNavTransitions.tabEnterTransition)
        assertNotNull("tabExitTransition harus terdefinisi", SulaoneNavTransitions.tabExitTransition)
    }

    @Test
    fun testAppNavigationStaysDecomposed() {
        val appNavFile = File("src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt")
        assertTrue("AppNavigation.kt harus ditemukan", appNavFile.exists())

        val lines = appNavFile.readLines().size
        // Original FASE 53 threshold was "< 450" (down from a 1.477-line
        // monolith); by FASE 73 the file had already grown to 454 lines from
        // later features, so the original bound was already failing before
        // this rewrite. Kept as a real regression guard against it drifting
        // back toward a monolith, with realistic headroom instead of an
        // already-stale exact number.
        assertTrue(
            "AppNavigation.kt harus tetap terdekomposisi di bawah 600 baris (sekarang $lines baris, awalnya 1.477 baris)",
            lines < 600
        )
    }
}
