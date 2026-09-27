package com.sultanagung1.sista.decomposition

import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.ui.attendance.QrDisplayState
import com.sultanagung1.sista.ui.finance.BillingUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * FASE 76.2 repair: every source path here pointed at the pre-FASE-73
 * single-module layout inside :app, which no longer exists — Home and the
 * design system moved to feature/home and core/designsystem. The TopAppBar
 * scan still "passed" on paper by scanning app/src/main/java, but that folder
 * now holds only ~20 app-level files (no screens), so it also tripped its own
 * ">= 50 files" sanity check. Paths now resolve to the real modules.
 */
class ScreenDecompositionTest {

    /** Unit tests run with the :app module dir as working dir; sibling modules are one level up. */
    private fun source(relativePath: String): File {
        val candidates = listOf(File("../$relativePath"), File(relativePath))
        return candidates.firstOrNull { it.exists() } ?: candidates.first()
    }

    private val homeDir = "feature/home/src/main/java/com/sultanagung1/sista/ui/home"
    private val designSystemDir = "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem"

    @Test
    fun testHomeScreenDecompositionAndLineCountReduction() {
        // Verify HomeScreen.kt line count dropped dramatically (under 250 lines, from 1253 lines)
        val homeScreenFile = source("$homeDir/HomeScreen.kt")
        assertTrue("HomeScreen.kt must exist (resolved to ${homeScreenFile.absolutePath})", homeScreenFile.exists())

        val lines = homeScreenFile.readLines()
        assertTrue(
            "HomeScreen.kt must be under 250 lines after decomposition (was 1253 lines, currently ${lines.size} lines)",
            lines.size <= 250
        )

        // Verify all 6 section files exist and are populated
        val sectionsDir = source("$homeDir/sections")
        assertTrue("sections directory must exist", sectionsDir.exists() && sectionsDir.isDirectory)

        val expectedSections = listOf(
            "HomeHeroSection.kt",
            "HomePrayerWidget.kt",
            "HomeSmartSuggestions.kt",
            "HomeQuickActions.kt",
            "HomeSchedulePreview.kt",
            "HomeModuleCarousel.kt"
        )

        for (sectionName in expectedSections) {
            val sectionFile = File(sectionsDir, sectionName)
            assertTrue("Section $sectionName must exist", sectionFile.exists())
            val content = sectionFile.readText()
            assertTrue("Section $sectionName must have package com.sultanagung1.sista.ui.home.sections",
                content.contains("package com.sultanagung1.sista.ui.home.sections"))
            assertTrue("Section $sectionName must be populated (> 500 chars)", content.length > 500)
        }
    }

    @Test
    fun testBillingUiStatePattern() {
        // Test sealed interface subtypes and properties
        val loadingState: BillingUiState = BillingUiState.Loading
        assertNotNull(loadingState)

        val sampleInvoices = listOf(
            BillingInvoice(1L, "SPP", "September 2026", 750000.0, "Rp 750.000", "10 Sep 2026", "UNPAID", 750000.0),
            BillingInvoice(2L, "SPP", "Agustus 2026", 750000.0, "Rp 750.000", "10 Agu 2026", "PAID", 0.0)
        )

        val contentState = BillingUiState.Content(
            invoices = sampleInvoices,
            totalUnpaid = 750000.0,
            totalPaid = 750000.0,
            selectedFilter = "SEMUA"
        )

        assertEquals(2, contentState.invoices.size)
        assertEquals(750000.0, contentState.totalUnpaid, 0.01)
        assertEquals(750000.0, contentState.totalPaid, 0.01)
        assertEquals("SEMUA", contentState.selectedFilter)

        val filteredUnpaid = contentState.copy(selectedFilter = "UNPAID")
        assertEquals("UNPAID", filteredUnpaid.selectedFilter)

        val errorState = BillingUiState.Error("Koneksi gagal")
        assertEquals("Koneksi gagal", errorState.message)
    }

    @Test
    fun testDynamicQrDisplayStatePattern() {
        val loadingState: QrDisplayState = QrDisplayState.Loading
        assertNotNull(loadingState)

        val contentState = QrDisplayState.Content("SULA-TOTP-TOKEN-9821", com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness.FRESH)
        assertEquals("SULA-TOTP-TOKEN-9821", contentState.qrToken)

        val errorState = QrDisplayState.Error("Server presensi sedang sibuk")
        assertEquals("Server presensi sedang sibuk", errorState.message)
    }

    @Test
    fun testDesignSystemNewComponentsExist() {
        val dsDir = source(designSystemDir)
        assertTrue("Design system dir must exist", dsDir.exists())

        val newComponents = listOf(
            "SulaoneTextField.kt",
            "SulaoneDropdown.kt",
            "SulaoneModalBottomSheet.kt", // FASE 70.2: renamed from SulaoneBottomSheet.kt
            "SulaoneDatePicker.kt",
            "SulaoneSegmentedFilter.kt" // FASE 70.2: renamed from SulaoneSegmentedButton.kt (which had zero real callers)
        )

        for (comp in newComponents) {
            val file = File(dsDir, comp)
            assertTrue("Design system component $comp must exist", file.exists())
            val text = file.readText()
            assertTrue("Component $comp must contain @Composable", text.contains("@Composable"))
            assertTrue("Component $comp must be in com.sultanagung1.sista.core.designsystem",
                text.contains("package com.sultanagung1.sista.core.designsystem"))
        }
    }

    @Test
    fun testNoRawTopAppBarInAppCodebase() {
        // Traverse the main sources of every module (app, core/*, feature/*) —
        // after FASE 73 the screens live in feature/*, not app/src/main/java.
        val repoRoot = listOf(File(".."), File(".")).first { File(it, "settings.gradle").exists() }
        val sourceRoots = listOf(File(repoRoot, "app/src/main/java")) +
            listOf("core", "feature").flatMap { group ->
                File(repoRoot, group).listFiles().orEmpty()
                    .map { File(it, "src/main/java") }
                    .filter { it.isDirectory }
            }
        val ktFiles = sourceRoots.flatMap { root -> root.walkTopDown().filter { it.extension == "kt" }.toList() }
        assertTrue("Should have at least 50 kt files across modules (found ${ktFiles.size})", ktFiles.size >= 50)

        for (file in ktFiles) {
            // SulaoneTopBar.kt itself defines TopBar, but screens should not call raw TopAppBar(
            val lines = file.readLines()
            for ((idx, line) in lines.withIndex()) {
                val trimmed = line.trim()
                assertFalse(
                    "File ${file.name} line ${idx + 1} uses raw TopAppBar(: '$trimmed'. Must use SulaoneTopBar instead.",
                    trimmed.startsWith("TopAppBar(")
                )
            }
        }
    }

    @Test
    fun testHomeScreenSectionExports() {
        val sectionsDir = source("$homeDir/sections")
        val heroFile = File(sectionsDir, "HomeHeroSection.kt")
        assertTrue(heroFile.readText().contains("HomeHeroSection"))

        val prayerFile = File(sectionsDir, "HomePrayerWidget.kt")
        assertTrue(prayerFile.readText().contains("HomePrayerWidget"))
        assertTrue(prayerFile.readText().contains("ModernPrayerBadge"))

        val suggestionsFile = File(sectionsDir, "HomeSmartSuggestions.kt")
        assertTrue(suggestionsFile.readText().contains("HomeStreakBanner"))
        assertTrue(suggestionsFile.readText().contains("HomeContextualSection"))

        val quickActionsFile = File(sectionsDir, "HomeQuickActions.kt")
        assertTrue(quickActionsFile.readText().contains("HomeMinimalQuickActions"))
        assertTrue(quickActionsFile.readText().contains("ModernQuickActionPill"))
        // FASE 76.2: dead code with a fake "Radius 250m" label (real geofence is 100m) was removed
        assertFalse(quickActionsFile.readText().contains("HomeBentoGrid"))
        assertFalse(quickActionsFile.readText().contains("HomeQuickServicesGrid"))

        val scheduleFile = File(sectionsDir, "HomeSchedulePreview.kt")
        assertTrue(scheduleFile.readText().contains("HomeSchedulePreview"))
        assertTrue(scheduleFile.readText().contains("ModernScheduleCard"))

        val moduleFile = File(sectionsDir, "HomeModuleCarousel.kt")
        assertTrue(moduleFile.readText().contains("HomeModuleCarousel"))
    }
}
