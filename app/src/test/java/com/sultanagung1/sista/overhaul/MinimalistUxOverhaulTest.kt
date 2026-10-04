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

    @Test
    fun testWeb3GimmickPurgedFromCatalog() {
        val catalogFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/portal/EnterpriseCatalogScreen.kt")
        val portalModelsFile = findSourceFile("src/main/java/com/sultanagung1/sista/data/model/PortalModels.kt")
        assertTrue("EnterpriseCatalogScreen.kt must exist", catalogFile.exists())
        assertTrue("PortalModels.kt must exist", portalModelsFile.exists())

        val content = catalogFile.readText() + "\n" + portalModelsFile.readText()
        assertFalse(
            "Web3 Blockchain passport gimmick must be purged from catalog",
            content.contains("Paspor Digital Web3 (DID)")
        )
        assertFalse(
            "Category AI & Web3 should be renamed to AI Edukasi",
            content.contains("\"AI & Web3\"")
        )
        assertTrue(
            "Must have Kecerdasan Buatan (AI Edukasi) category",
            content.contains("Kecerdasan Buatan (AI Edukasi)")
        )
        assertTrue(
            "Must have E-Ijazah & Transkrip Resmi replacement",
            content.contains("E-Ijazah & Transkrip Resmi")
        )
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
        assertTrue("Everything else lives in Layanan", quick.contains("\"Semua layanan\""))

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
