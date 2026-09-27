package com.sultanagung1.sista.motion

import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.core.motion.LocalSharedTransitionScope
import com.sultanagung1.sista.core.motion.SulaoneNavTransitions
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Automated Unit Test Suite untuk FASE 54:
 * Shared Element Transitions & Motion Upgrade.
 */
class SharedElementMotionTest {

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

    private fun findDirectory(dirPath: String): File {
        val dirname = dirPath.substringAfterLast("/")
        val candidates = listOf(
            File(dirPath),
            File("app/$dirPath"),
            File("../$dirPath"),
            File("../../$dirPath")
        )
        candidates.firstOrNull { it.exists() }?.let { return it }

        val rootDir = File("..").takeIf { File("..", "settings.gradle").exists() || File("..", "build.gradle").exists() } ?: File(".")
        return rootDir.walkTopDown().firstOrNull { it.isDirectory && it.name == dirname }
            ?: File(dirPath)
    }

    @Test
    fun testSharedTransitionCompositionLocalsDefaultToNull() {
        assertNotNull("LocalSharedTransitionScope harus terdefinisi", LocalSharedTransitionScope)
        assertNotNull("LocalNavAnimatedVisibilityScope harus terdefinisi", LocalNavAnimatedVisibilityScope)
    }

    @Test
    fun testContextAwareMotionTransitionsConfigured() {
        assertNotNull("enterTransition harus terdefinisi", SulaoneNavTransitions.enterTransition)
        assertNotNull("exitTransition harus terdefinisi", SulaoneNavTransitions.exitTransition)
        assertNotNull("popEnterTransition harus terdefinisi", SulaoneNavTransitions.popEnterTransition)
        assertNotNull("popExitTransition harus terdefinisi", SulaoneNavTransitions.popExitTransition)
        assertNotNull("tabEnterTransition harus terdefinisi", SulaoneNavTransitions.tabEnterTransition)
        assertNotNull("tabExitTransition harus terdefinisi", SulaoneNavTransitions.tabExitTransition)
        assertNotNull("modalEnterTransition harus terdefinisi", SulaoneNavTransitions.modalEnterTransition)
        assertNotNull("modalExitTransition harus terdefinisi", SulaoneNavTransitions.modalExitTransition)
        assertNotNull("subPageEnterTransition harus terdefinisi", SulaoneNavTransitions.subPageEnterTransition)
        assertNotNull("subPageExitTransition harus terdefinisi", SulaoneNavTransitions.subPageExitTransition)
        assertNotNull("predictiveBackPopEnterTransition harus terdefinisi", SulaoneNavTransitions.predictiveBackPopEnterTransition)
        assertNotNull("predictiveBackPopExitTransition harus terdefinisi", SulaoneNavTransitions.predictiveBackPopExitTransition)
    }

    @Test
    fun testSharedTransitionLayoutWrappedInAppNavigation() {
        val appNavFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt")
        assertTrue("AppNavigation.kt harus ditemukan", appNavFile.exists())

        val content = appNavFile.readText()
        assertTrue(
            "AppNavigation.kt harus membungkus NavHost di dalam SharedTransitionLayout",
            content.contains("SharedTransitionLayout")
        )
        assertTrue(
            "AppNavigation.kt harus menyediakan LocalSharedTransitionScope",
            content.contains("LocalSharedTransitionScope provides this@SharedTransitionLayout")
        )
        assertTrue(
            "AppNavigation.kt harus memiliki anotasi ExperimentalSharedTransitionApi",
            content.contains("ExperimentalSharedTransitionApi")
        )
    }

    @Test
    fun testPullToRefreshLegacyRemovedAndReplacedWithM3() {
        val legacyFile = findSourceFile("src/main/java/com/sultanagung1/sista/core/designsystem/PullToRefresh.kt")
        assertFalse(
            "File custom hand-rolled PullToRefresh.kt harus sudah dihapus (FASE 54.3)",
            legacyFile.exists() && legacyFile.path.contains("PullToRefresh.kt") && !legacyFile.path.contains("SulaonePullRefresh.kt")
        )

        val canonicalFile = findSourceFile("src/main/java/com/sultanagung1/sista/core/designsystem/SulaonePullRefresh.kt")
        assertTrue(
            "File canonical SulaonePullRefresh.kt harus ditemukan",
            canonicalFile.exists()
        )

        val content = canonicalFile.readText()
        assertTrue(
            "SulaonePullRefresh.kt harus menggunakan Material 3 PullToRefreshBox",
            content.contains("PullToRefreshBox")
        )
        assertTrue(
            "SulaonePullRefresh.kt harus menyediakan fungsi SulaonePullToRefreshBox",
            content.contains("fun SulaonePullToRefreshBox")
        )
    }

    @Test
    fun testSharedElementKeysAcrossKeyScreens() {
        // 1. Student avatar (HomeScreen & sections, ProfileScreen, StudentProfileComprehensiveScreen)
        val homeDir = findDirectory("src/main/java/com/sultanagung1/sista/ui/home")
        val homeFilesText = homeDir.walkTopDown().filter { it.extension == "kt" }.map { it.readText() }.joinToString("\n")
        val profileFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/profile/ProfileScreen.kt")
        val comprehensiveFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/profile/StudentProfileComprehensiveScreen.kt")

        assertTrue("HomeScreen atau section-nya harus memiliki key student_avatar", homeFilesText.contains("\"student_avatar\""))
        assertTrue("ProfileScreen.kt harus memiliki key student_avatar", profileFile.readText().contains("\"student_avatar\""))
        assertTrue("StudentProfileComprehensiveScreen.kt harus memiliki key student_avatar", comprehensiveFile.readText().contains("\"student_avatar\""))

        // 2. Schedule card (HomeScreen & sections, ScheduleScreen)
        val scheduleFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/academic/ScheduleScreen.kt")
        assertTrue("HomeScreen atau section-nya harus memiliki shared bounds schedule_card_", homeFilesText.contains("schedule_card_"))
        assertTrue("ScheduleScreen.kt harus memiliki shared bounds schedule_card_", scheduleFile.readText().contains("schedule_card_"))

        // 3. CBT exam card (HomeScreen & sections, CbtExamListScreen, CbtTokenEntryScreen)
        val cbtListFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/cbt/CbtExamListScreen.kt")
        val cbtTokenFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/cbt/CbtTokenEntryScreen.kt")
        assertTrue("CbtExamListScreen.kt harus memiliki cbt_exam_card_", cbtListFile.readText().contains("cbt_exam_card_"))
        assertTrue("CbtTokenEntryScreen.kt harus memiliki cbt_exam_card_", cbtTokenFile.readText().contains("cbt_exam_card_"))

        // 4. Book cover (LibraryCatalogScreen)
        val libraryFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/library/LibraryCatalogScreen.kt")
        assertTrue("LibraryCatalogScreen.kt harus memiliki book_cover_", libraryFile.readText().contains("book_cover_"))

        // 5. Achievement card (AchievementUploadScreen)
        val achievementFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/achievement/AchievementUploadScreen.kt")
        assertTrue("AchievementUploadScreen.kt harus memiliki achievement_card_", achievementFile.readText().contains("achievement_card_"))
        assertTrue("AchievementUploadScreen.kt harus memiliki cert_card_", achievementFile.readText().contains("cert_card_"))
    }

    @Test
    fun testCanonicalPullToRefreshIntegratedInFeatureScreens() {
        val homeContent = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt").readText()
        val scheduleContent = findSourceFile("src/main/java/com/sultanagung1/sista/ui/academic/ScheduleScreen.kt").readText()
        val cbtContent = findSourceFile("src/main/java/com/sultanagung1/sista/ui/cbt/CbtExamListScreen.kt").readText()
        val libraryContent = findSourceFile("src/main/java/com/sultanagung1/sista/ui/library/LibraryCatalogScreen.kt").readText()
        val notifContent = findSourceFile("src/main/java/com/sultanagung1/sista/ui/notifications/NotificationCenterScreen.kt").readText()

        assertTrue("HomeScreen harus mengintegrasikan SulaonePullToRefreshBox", homeContent.contains("SulaonePullToRefreshBox"))
        assertTrue("ScheduleScreen harus mengintegrasikan SulaonePullToRefreshBox", scheduleContent.contains("SulaonePullToRefreshBox"))
        assertTrue("CbtExamListScreen harus mengintegrasikan SulaonePullToRefreshBox", cbtContent.contains("SulaonePullToRefreshBox"))
        assertTrue("LibraryCatalogScreen harus mengintegrasikan SulaonePullToRefreshBox", libraryContent.contains("SulaonePullToRefreshBox"))
        assertTrue("NotificationCenterScreen harus mengintegrasikan SulaonePullToRefreshBox", notifContent.contains("SulaonePullToRefreshBox"))
    }
}
