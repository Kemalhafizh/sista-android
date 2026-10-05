package com.sultanagung1.sista.overhaul

import com.sultanagung1.sista.core.feature.FeatureFlagKey
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MinimalistUxOverhaulTest {

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

    /**
     * The module directory (EnterpriseCatalogScreen, ModuleFavoritesScreen,
     * SsoWebViewScreen) decided access with a role list in the screen, repeated
     * the Services tab and listed marketing labels. It was removed; the Web3
     * "Paspor Digital" entry this test used to guard went with it.
     */
    @Test
    fun testModuleDirectoryStaysRemoved() {
        val root = listOf(File("."), File("..")).first { File(it, "settings.gradle").exists() || File(it, "settings.gradle.kts").exists() }
        val portal = File(root, "feature/home/src/main/java/com/sultanagung1/sista/ui/portal")
        for (name in listOf("EnterpriseCatalogScreen.kt", "ModuleFavoritesScreen.kt", "SsoWebViewScreen.kt", "ModuleCatalogViewModel.kt")) {
            assertFalse("$name must stay deleted", File(portal, name).exists())
        }
        val screens = File(root, "core/common/src/main/java/com/sultanagung1/sista/ui/navigation/Screen.kt").readText()
        for (route in listOf("enterprise_catalog", "module_favorites", "sso_webview")) {
            assertFalse("Route $route must not come back", screens.contains("\"$route\""))
        }
        val models = File(root, "core/model/src/main/java/com/sultanagung1/sista/data/model/PortalModels.kt").readText()
        assertFalse("The static module catalog must not come back", models.contains("EnterpriseModuleCatalog"))
        assertFalse("Web3 passport gimmick must not come back", models.contains("Paspor Digital Web3"))
    }

    @Test
    fun testBlockchainFeatureFlagDisabledByDefault() {
        assertFalse(
            "BLOCKCHAIN_PASSPORT_ENABLED must default to false",
            FeatureFlagKey.BLOCKCHAIN_PASSPORT_ENABLED.defaultEnabled
        )
    }

    @Test
    fun testHomeShortcutsFollowTheAccountsFeatures() {
        val quick = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/sections/HomeQuickAccess.kt").readText()
        assertTrue("Shortcuts are a short list", quick.contains("QUICK_ACCESS_LIMIT = 6"))
        assertTrue("Everything else lives in Layanan", quick.contains("R.string.home_all_services"))

        val home = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt").readText()
        // Only shortcuts this account may open, from the server's capability list.
        assertTrue(home.contains("STUDENT_QUICK_ITEMS.filter { capabilities.canOpen(it.route) }"))
        assertTrue(home.contains("Screen.ServicesHub.route"))
        // The old all-services sheet (every tile for every account) is gone.
        assertFalse(home.contains("HomeServicesBottomSheet"))
    }

    @Test
    fun testBottomNavConsolidatedTo4Tabs() {
        val appNavFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt")
        assertTrue(appNavFile.exists())
        val content = appNavFile.readText()
        assertTrue("Must contain FASE 60.3 comment", content.contains("FASE 60.3: Consolidated 4-Tab System"))
        // One tab list for every account, built from the server's capability
        // list — no branch per role any more.
        assertTrue(content.contains("ShellTabs.entries(homeRoute, capabilityState, tabLabels)"))
        assertFalse(content.contains("UserRoles.groupOf("))

        val tabs = findSourceFile("src/main/java/com/sultanagung1/sista/ui/navigation/ShellTabs.kt").readText()
        assertEquals("At most four tabs", 4, tabs.split("add(NavEntry(").size - 1)
    }
}
