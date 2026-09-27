package com.sultanagung1.sista.ui

import com.sultanagung1.sista.core.designsystem.LoadingTier
import com.sultanagung1.sista.core.designsystem.LoadingTierTiming
import com.sultanagung1.sista.ui.finance.formatRupiahCompact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * FASE 76 Gelombang 3: bento on BillingScreen, see-through headers on long
 * lists (76.2) and tiered, content-shaped loading (76.5).
 */
class Fase76Wave3Test {

    private fun source(relativePath: String): String {
        val candidates = listOf(File("../$relativePath"), File(relativePath))
        val file = candidates.firstOrNull { it.exists() } ?: error("$relativePath not found")
        return file.readText()
    }

    /** Code only — comments that explain what was removed don't count. */
    private fun code(relativePath: String): String =
        source(relativePath).replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")
            .lines().joinToString("\n") { it.substringBefore("//") }

    private val billing = "feature/finance/src/main/java/com/sultanagung1/sista/ui/finance/BillingScreen.kt"
    private val grades = "feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/GradesScreen.kt"
    private val announcements = "feature/academic/src/main/java/com/sultanagung1/sista/ui/announcements/AnnouncementFeedScreen.kt"
    private val conversations = "feature/chat/src/main/java/com/sultanagung1/sista/ui/chat/ConversationListScreen.kt"
    private val library = "feature/academic/src/main/java/com/sultanagung1/sista/ui/library/LibraryCatalogScreen.kt"
    private val libraryVm = "feature/academic/src/main/java/com/sultanagung1/sista/ui/library/LibraryViewModel.kt"
    private val components = "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneComponents.kt"

    // ── 76.5 tier timing ───────────────────────────────────────────────

    @Test
    fun loadingTiersFollowTheResearchThresholds() {
        assertEquals(LoadingTier.Hidden, LoadingTierTiming.tierFor(0))
        assertEquals(LoadingTier.Hidden, LoadingTierTiming.tierFor(299))
        assertEquals(LoadingTier.Spinner, LoadingTierTiming.tierFor(300))
        assertEquals(LoadingTier.Spinner, LoadingTierTiming.tierFor(999))
        assertEquals(LoadingTier.Skeleton, LoadingTierTiming.tierFor(1_000))
        assertEquals(LoadingTier.Skeleton, LoadingTierTiming.tierFor(8_000))
    }

    @Test
    fun priorityScreensShowContentShapedSkeletonsWhileLoading() {
        assertTrue(code(grades).contains("SulaoneTieredLoading(isLoading = true) { GradesSkeleton() }"))
        assertTrue(code(announcements).contains("AnnouncementSkeleton(borderColor = borderColor)"))
        assertTrue(code(library).contains("BookListSkeleton()"))
    }

    @Test
    fun emptyStatesAreNotClaimedWhileLoadingOrAfterAnError() {
        val feed = code(announcements)
        // Loading is handled before the "no announcements" branch, and errors are shown.
        assertTrue(feed.indexOf("filteredList.isEmpty() && uiState.isLoading") < feed.indexOf("title = \"Tidak Ada Pengumuman\""))
        assertTrue(feed.contains("SulaoneErrorBanner("))

        val lib = code(library)
        assertTrue(lib.indexOf("uiState.myLoans.isEmpty() && uiState.isLoading") < lib.indexOf("Belum Ada Buku yang Dipinjam"))
    }

    @Test
    fun libraryPullToRefreshReallyReloads() {
        val lib = code(library)
        assertFalse("No fixed-duration fake refresh", lib.contains("delay(600)"))
        assertTrue(lib.contains("viewModel.refresh()"))
        assertTrue(lib.contains("isRefreshing = refreshRequested && uiState.isLoading"))
        // A successful retry must clear the old error.
        assertTrue(code(libraryVm).contains("it.copy(isLoading = true, errorMessage = null)"))
    }

    // ── 76.2 bento on Billing ────────────────────────────────────────────

    @Test
    fun billingSummaryIsABentoWithoutInventedLabels() {
        val bill = code(billing)
        assertTrue(bill.contains("BentoPair("))
        assertTrue(bill.contains("ModernBentoCard("))
        // Printed for every student regardless of data.
        assertFalse(bill.contains("T.A. 2026/2027"))
        assertFalse(bill.contains("Aktif Belajar"))
    }

    @Test
    fun compactRupiahFitsHalfWidthTiles() {
        assertEquals("Rp 750", formatRupiahCompact(750.0))
        assertEquals("Rp 750 rb", formatRupiahCompact(750_000.0))
        assertEquals("Rp 12,5 jt", formatRupiahCompact(12_500_000.0))
        assertEquals("Rp 3 jt", formatRupiahCompact(3_000_000.0))
        assertEquals("Rp 1,2 M", formatRupiahCompact(1_200_000_000.0))
        assertEquals("Rp 0", formatRupiahCompact(0.0))
    }

    // ── 76.2 see-through headers ─────────────────────────────────────────

    @Test
    fun longListsScrollUnderASeeThroughTopBar() {
        for (screen in listOf(grades, announcements, conversations)) {
            val c = code(screen)
            assertTrue("$screen: translucent bar", c.contains("translucent = true"))
            assertTrue("$screen: hairline once scrolled", c.contains("showDivider = listScrolled"))
            assertTrue("$screen: top inset goes into contentPadding", c.contains("paddingValues.calculateTopPadding()"))
            assertFalse("$screen: list must not be padded below the bar", c.contains(".padding(paddingValues)"))
        }
    }

    @Test
    fun topBarKeepsItsOldLookByDefault() {
        val c = code(components)
        assertTrue(c.contains("translucent: Boolean = false"))
        assertTrue(c.contains("showDivider: Boolean = false"))
    }
}
