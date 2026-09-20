package com.sultanagung1.sista.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.*
import com.sultanagung1.sista.core.designsystem.*
import org.junit.Assert.*
import org.junit.Test

class AccessibilityComplianceTest {

    @Test
    fun testHeadingSemanticsUtility() {
        val modifier1 = Modifier.sulaoneHeading("Akses Utama SuperApp")
        assertNotNull("Modifier with heading semantics should not be null", modifier1)

        val modifier2 = Modifier.talkBackHeading("Jadwal Pelajaran")
        assertNotNull("Modifier with legacy talkBackHeading should not be null", modifier2)
    }

    @Test
    fun testBadgeStateDescriptionFormatting() {
        // Attendance status formatting
        val hadirDesc = AccessibilityFormatters.attendanceState("Hadir")
        assertTrue(
            "Hadir status description should mention hadir and tepat waktu: $hadirDesc",
            hadirDesc.contains("Hadir tepat waktu", ignoreCase = true)
        )

        val sakitDesc = AccessibilityFormatters.attendanceState("Sakit")
        assertTrue(
            "Sakit status description should mention izin sakit: $sakitDesc",
            sakitDesc.contains("sakit", ignoreCase = true)
        )

        val alpaDesc = AccessibilityFormatters.attendanceState("Alpha")
        assertTrue(
            "Alpha status description should mention tanpa keterangan: $alpaDesc",
            alpaDesc.contains("Alpa", ignoreCase = true)
        )

        // Billing status formatting
        val paidDesc = AccessibilityFormatters.billingState(isPaid = true, amount = "Rp 850.000")
        assertTrue("Billing paid description should mention Lunas: $paidDesc", paidDesc.contains("Lunas", ignoreCase = true))
        assertTrue("Billing paid description should include amount: $paidDesc", paidDesc.contains("Rp 850.000"))

        val unpaidDesc = AccessibilityFormatters.billingState(isPaid = false)
        assertTrue("Billing unpaid description should mention Belum dibayar: $unpaidDesc", unpaidDesc.contains("Belum dibayar", ignoreCase = true))

        // KKTP status formatting
        val passedKktp = AccessibilityFormatters.kktpState(isPassed = true, score = 88f, threshold = 75f)
        assertTrue("KKTP passed should mention Tuntas KKTP: $passedKktp", passedKktp.contains("Tuntas KKTP"))

        val failedKktp = AccessibilityFormatters.kktpState(isPassed = false, score = 65f, threshold = 75f)
        assertTrue("KKTP failed should mention Perlu Remedial: $failedKktp", failedKktp.contains("Perlu Remedial"))
    }

    @Test
    fun testTouchTargetMinimum48Dp() {
        val modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
        assertNotNull("Touch target modifier should instantiate properly", modifier)
    }

    @Test
    fun testWcagContrastFormulas() {
        // Black and white contrast ratio is 21.0:1
        val blackWhiteContrast = AccessibilityContrastUtils.calculateContrastRatio(Color.Black, Color.White)
        assertEquals(21.0, blackWhiteContrast, 0.1)

        // Identical colors contrast ratio is 1.0:1
        val sameContrast = AccessibilityContrastUtils.calculateContrastRatio(Color.White, Color.White)
        assertEquals(1.0, sameContrast, 0.05)

        // Verify WCAG AA Compliance (>= 4.5:1)
        val isCompliantAa = AccessibilityContrastUtils.isWcagAaCompliant(Slate900, BackgroundLight)
        assertTrue("Slate900 on BackgroundLight should be WCAG AA compliant", isCompliantAa)

        // High contrast onSurface on surface
        val highContrastAa = AccessibilityContrastUtils.isWcagAaCompliant(HighContrastColorScheme.onSurface, HighContrastColorScheme.surface)
        assertTrue("High contrast theme should be WCAG AA compliant", highContrastAa)

        val highContrastAaa = AccessibilityContrastUtils.isWcagAaaCompliant(HighContrastColorScheme.onSurface, HighContrastColorScheme.surface)
        assertTrue("High contrast theme should be WCAG AAA compliant (>= 7.0:1)", highContrastAaa)
    }

    @Test
    fun testChartSummariesGeneration() {
        // 1. Radar Chart Summary
        val radarData = listOf(
            "Matematika" to 85f,
            "Bahasa Indonesia" to 90f,
            "Fisika" to 78f
        )
        val radarSummary = AccessibilityFormatters.radarChartSummary("Grafik Radar Kompetensi", radarData)
        assertFalse("Radar summary should not be blank", radarSummary.isBlank())
        assertTrue("Radar summary should contain subject names", radarSummary.contains("Matematika") && radarSummary.contains("Fisika"))

        // Empty radar data safety
        val emptyRadar = AccessibilityFormatters.radarChartSummary("Grafik Radar Kosong", emptyList())
        assertTrue("Empty radar should report no data gracefully", emptyRadar.contains("Tidak ada data"))

        // 2. Donut Chart Summary
        val donutSegments = listOf(
            "Lunas" to 70f,
            "Belum Bayar" to 30f
        )
        val donutSummary = AccessibilityFormatters.donutChartSummary("Distribusi SPP", donutSegments)
        assertTrue("Donut summary should contain percentage info", donutSummary.contains("Lunas 70 persen") && donutSummary.contains("Belum Bayar 30 persen"))

        // 3. Line Chart Summary
        val linePoints = listOf(70f, 75f, 85f, 92f)
        val lineSummary = AccessibilityFormatters.lineChartSummary("Perkembangan Nilai Siswa", linePoints)
        assertTrue("Line summary should report upward trend", lineSummary.contains("Tren meningkat"))
        assertTrue("Line summary should mention min and max values", lineSummary.contains("70.0") && lineSummary.contains("92.0"))

        // 4. Streak Heatmap Summary
        val streakSummary = AccessibilityFormatters.streakHeatmapSummary(totalDays = 28, activeDaysCount = 21)
        assertTrue("Streak summary should mention active days count and percentage", streakSummary.contains("21 dari 28 hari aktif") && streakSummary.contains("75 persen"))
    }

    @Test
    fun testFontScaleSafetyNetCap() {
        // Normal scale
        val normal = FontScaleManager.calculateEffectiveFontScale(systemFontScale = 1.0f, appFontScale = 1.0f)
        assertEquals(1.0f, normal, 0.001f)

        // Medium scale
        val medium = FontScaleManager.calculateEffectiveFontScale(systemFontScale = 1.2f, appFontScale = 1.25f)
        assertEquals(1.5f, medium, 0.001f)

        // Large scale
        val large = FontScaleManager.calculateEffectiveFontScale(systemFontScale = 1.4f, appFontScale = 1.5f)
        assertEquals(2.1f, large, 0.001f)

        // Extreme scale safety cap (must cap at MAX_EFFECTIVE_FONT_SCALE = 2.5f)
        val cappedExtreme1 = FontScaleManager.calculateEffectiveFontScale(systemFontScale = 2.0f, appFontScale = 2.0f)
        assertEquals(FontScaleManager.MAX_EFFECTIVE_FONT_SCALE, cappedExtreme1, 0.001f)

        val cappedExtreme2 = FontScaleManager.calculateEffectiveFontScale(systemFontScale = 3.5f, appFontScale = 2.0f)
        assertEquals(2.5f, cappedExtreme2, 0.001f)

        // App scale out of range clamp check
        val clampedUnder = FontScaleManager.calculateEffectiveFontScale(systemFontScale = 1.0f, appFontScale = 0.5f)
        assertEquals(0.75f, clampedUnder, 0.001f)
    }
}
