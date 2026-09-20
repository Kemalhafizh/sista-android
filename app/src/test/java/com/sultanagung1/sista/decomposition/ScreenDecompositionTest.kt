package com.sultanagung1.sista.decomposition

import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.ui.attendance.QrDisplayState
import com.sultanagung1.sista.ui.finance.BillingUiState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ScreenDecompositionTest {

    @Test
    fun testHomeScreenDecompositionAndLineCountReduction() {
        // Verify HomeScreen.kt line count dropped dramatically (under 250 lines, from 1253 lines)
        val homeScreenFile = File("src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt")
        assertTrue("HomeScreen.kt must exist", homeScreenFile.exists())

        val lines = homeScreenFile.readLines()
        assertTrue(
            "HomeScreen.kt must be under 250 lines after decomposition (was 1253 lines, currently ${lines.size} lines)",
            lines.size <= 250
        )

        // Verify all 6 section files exist and are populated
        val sectionsDir = File("src/main/java/com/sultanagung1/sista/ui/home/sections")
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
            BillingInvoice(1, "SPP September 2026", 750000, "Rp 750.000", "10 Sep 2026", "UNPAID", "BSI", "8821900699112"),
            BillingInvoice(2, "SPP Agustus 2026", 750000, "Rp 750.000", "10 Agu 2026", "PAID", "BSI", "8821900699112")
        )

        val contentState = BillingUiState.Content(
            invoices = sampleInvoices,
            totalUnpaid = 750000L,
            totalPaid = 750000L,
            selectedFilter = "SEMUA"
        )

        assertEquals(2, contentState.invoices.size)
        assertEquals(750000L, contentState.totalUnpaid)
        assertEquals(750000L, contentState.totalPaid)
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

        val contentState = QrDisplayState.Content("SULA-TOTP-TOKEN-9821")
        assertEquals("SULA-TOTP-TOKEN-9821", contentState.qrPayload)

        val errorState = QrDisplayState.Error("Server presensi sedang sibuk")
        assertEquals("Server presensi sedang sibuk", errorState.message)
    }

    @Test
    fun testDesignSystemNewComponentsExist() {
        val dsDir = File("src/main/java/com/sultanagung1/sista/core/designsystem")
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
        // Traverse all kotlin files in app/src/main/java
        val mainDir = File("src/main/java")
        val ktFiles = mainDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("Should have at least 50 kt files", ktFiles.size >= 50)

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
        val sectionsDir = File("src/main/java/com/sultanagung1/sista/ui/home/sections")
        val heroFile = File(sectionsDir, "HomeHeroSection.kt")
        assertTrue(heroFile.readText().contains("HomeHeroSection"))

        val prayerFile = File(sectionsDir, "HomePrayerWidget.kt")
        assertTrue(prayerFile.readText().contains("HomePrayerWidget"))
        assertTrue(prayerFile.readText().contains("ModernPrayerBadge"))

        val suggestionsFile = File(sectionsDir, "HomeSmartSuggestions.kt")
        assertTrue(suggestionsFile.readText().contains("HomeStreakBanner"))
        assertTrue(suggestionsFile.readText().contains("HomeContextualSection"))

        val quickActionsFile = File(sectionsDir, "HomeQuickActions.kt")
        assertTrue(quickActionsFile.readText().contains("HomeBentoGrid"))
        assertTrue(quickActionsFile.readText().contains("HomeQuickServicesGrid"))
        assertTrue(quickActionsFile.readText().contains("ModernQuickActionPill"))

        val scheduleFile = File(sectionsDir, "HomeSchedulePreview.kt")
        assertTrue(scheduleFile.readText().contains("HomeSchedulePreview"))
        assertTrue(scheduleFile.readText().contains("ModernScheduleCard"))

        val moduleFile = File(sectionsDir, "HomeModuleCarousel.kt")
        assertTrue(moduleFile.readText().contains("HomeModuleCarousel"))
    }
}
