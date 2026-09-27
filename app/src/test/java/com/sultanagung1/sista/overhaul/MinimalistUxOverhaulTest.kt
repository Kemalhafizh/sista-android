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
    fun testHomeServicesBottomSheetExists() {
        val sheetFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/sections/HomeServicesBottomSheet.kt")
        assertTrue("HomeServicesBottomSheet.kt must exist", sheetFile.exists())

        val content = sheetFile.readText()
        assertTrue(content.contains("fun HomeServicesBottomSheet"))
        assertTrue("Must have Akademik category", content.contains("Akademik & Ujian"))
        assertTrue("Must have Keuangan category", content.contains("Keuangan & Presensi"))
        assertTrue("Must have Kesiswaan category", content.contains("Kesiswaan & Pembinaan"))
        assertTrue("Must have Bimbingan category", content.contains("Bimbingan & Layanan"))
    }

    @Test
    fun testHomeMinimalQuickActionsDefined() {
        val quickActionsFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/sections/HomeQuickActions.kt")
        assertTrue(quickActionsFile.exists())

        val content = quickActionsFile.readText()
        assertTrue("Must define HomeMinimalQuickActions", content.contains("fun HomeMinimalQuickActions"))
        assertTrue(content.contains("\"Presensi\""))
        assertTrue(content.contains("\"Jadwal\""))
        assertTrue(content.contains("\"Ujian CBT\""))
        assertTrue(content.contains("\"SPP\""))
        assertTrue(content.contains("\"Semua\""))
    }

    @Test
    fun testHomeScreenHasAppletSheetTrigger() {
        val homeFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt")
        assertTrue(homeFile.exists())

        val content = homeFile.readText()
        assertTrue("Must trigger HomeServicesBottomSheet", content.contains("HomeServicesBottomSheet"))
        assertTrue("Must use HomeMinimalQuickActions", content.contains("HomeMinimalQuickActions"))
        assertFalse("Must not include HomeModuleCarousel in main feed", content.contains("HomeModuleCarousel("))
    }

    @Test
    fun testBottomNavConsolidatedTo4Tabs() {
        val appNavFile = findSourceFile("src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt")
        assertTrue(appNavFile.exists())

        val content = appNavFile.readText()
        assertTrue("Must contain FASE 60.3 comment", content.contains("FASE 60.3: Consolidated 4-Tab System"))

        val navItemsSection = content.substringAfter("val bottomNavItems = when {").substringBefore("val tabRoutes")
        val totalNavItems = navItemsSection.split("BottomNavItem(").size - 1
        assertEquals("Total nav items across 4 roles must be 16 (4 tabs per role)", 16, totalNavItems)
    }
}
