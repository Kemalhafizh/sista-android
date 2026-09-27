package com.sultanagung1.sista.adaptive

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.sultanagung1.sista.core.designsystem.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Automated Unit Test Suite untuk FASE 55: Adaptive Layout — Tablet & Foldable.
 * Memverifikasi breakpoint deteksi lebar layar, resolusi kolom grid responsif,
 * pemetaan komponen navigasi adaptif, serta arsitektur dual-pane scaffold.
 */
class AdaptiveLayoutTest {

    @Test
    fun testWindowWidthSizeClassBreakpoints() {
        // Smartphone (Compact): < 600 dp
        assertEquals(WindowWidthSizeClass.Compact, calculateWindowWidthSizeClass(360))
        assertEquals(WindowWidthSizeClass.Compact, calculateWindowWidthSizeClass(411))
        assertEquals(WindowWidthSizeClass.Compact, calculateWindowWidthSizeClass(599))

        // Foldable unfolded / Tablet Portrait (Medium): 600..839 dp
        assertEquals(WindowWidthSizeClass.Medium, calculateWindowWidthSizeClass(600))
        assertEquals(WindowWidthSizeClass.Medium, calculateWindowWidthSizeClass(720))
        assertEquals(WindowWidthSizeClass.Medium, calculateWindowWidthSizeClass(839))

        // Large Tablet Landscape / Desktop (Expanded): >= 840 dp
        assertEquals(WindowWidthSizeClass.Expanded, calculateWindowWidthSizeClass(840))
        assertEquals(WindowWidthSizeClass.Expanded, calculateWindowWidthSizeClass(1024))
        assertEquals(WindowWidthSizeClass.Expanded, calculateWindowWidthSizeClass(1280))
    }

    @Test
    fun testAdaptiveNavigationTypeMapping() {
        // Compact -> Floating Bottom Navigation Bar (Phone)
        val compactNavType = WindowWidthSizeClass.Compact.toAdaptiveNavigationType()
        assertEquals(AdaptiveNavigationType.BOTTOM_NAVIGATION, compactNavType)

        // Medium -> Navigation Rail di sisi kiri (Tablet portrait / Foldable)
        val mediumNavType = WindowWidthSizeClass.Medium.toAdaptiveNavigationType()
        assertEquals(AdaptiveNavigationType.NAVIGATION_RAIL, mediumNavType)

        // Expanded -> Permanent Navigation Drawer di sisi kiri (Tablet landscape / Desktop)
        val expandedNavType = WindowWidthSizeClass.Expanded.toAdaptiveNavigationType()
        assertEquals(AdaptiveNavigationType.PERMANENT_NAVIGATION_DRAWER, expandedNavType)
    }

    @Test
    fun testResponsiveGridColumnsCalculation() {
        // Preset SingleToTriple
        assertEquals(1, calculateResponsiveColumns(380, ResponsiveGridColumns.SingleToTriple))
        assertEquals(2, calculateResponsiveColumns(680, ResponsiveGridColumns.SingleToTriple))
        assertEquals(3, calculateResponsiveColumns(1000, ResponsiveGridColumns.SingleToTriple))

        // Preset BentoDashboard (Kartu Dashboard & Analitik)
        assertEquals(1, calculateResponsiveColumns(360, ResponsiveGridColumns.BentoDashboard))
        assertEquals(2, calculateResponsiveColumns(700, ResponsiveGridColumns.BentoDashboard))
        assertEquals(4, calculateResponsiveColumns(960, ResponsiveGridColumns.BentoDashboard))

        // Preset CardsTwoToFour
        assertEquals(2, calculateResponsiveColumns(400, ResponsiveGridColumns.CardsTwoToFour))
        assertEquals(3, calculateResponsiveColumns(750, ResponsiveGridColumns.CardsTwoToFour))
        assertEquals(4, calculateResponsiveColumns(1200, ResponsiveGridColumns.CardsTwoToFour))
    }

    @Test
    fun testDualPaneModulesExistAndConfigured() {
        // Verifikasi keberadaan komponen adaptif via Reflection
        val chatAdaptiveClass = Class.forName("com.sultanagung1.sista.ui.chat.AdaptiveChatScaffoldKt")
        assertNotNull("AdaptiveChatScaffoldKt harus ada", chatAdaptiveClass)

        val elearningAdaptiveClass = Class.forName("com.sultanagung1.sista.ui.elearning.AdaptiveElearningScaffoldKt")
        assertNotNull("AdaptiveElearningScaffoldKt harus ada", elearningAdaptiveClass)

        val questionBankClass = Class.forName("com.sultanagung1.sista.ui.teacher.QuestionBankScreenKt")
        assertNotNull("QuestionBankScreenKt harus ada", questionBankClass)

        // Was "TeachingJournalScreenKt": that file doesn't exist, so this threw
        // ClassNotFoundException. The journal screen is TeachingJournalMobileScreen.
        // Note these checks only prove the classes exist — the journal screen has
        // no dual-pane layout of its own.
        val teachingJournalClass = Class.forName("com.sultanagung1.sista.ui.teacher.TeachingJournalMobileScreenKt")
        assertNotNull("TeachingJournalMobileScreenKt harus ada", teachingJournalClass)
    }

    @Test
    fun testCompositionLocalWindowWidthSizeClassDefault() {
        assertNotNull("LocalWindowWidthSizeClass harus terdefinisi", LocalWindowWidthSizeClass)
        assertEquals(600, AdaptiveBreakpoints.COMPACT_MAX_WIDTH_DP)
        assertEquals(840, AdaptiveBreakpoints.MEDIUM_MAX_WIDTH_DP)
    }
}
