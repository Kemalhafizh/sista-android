package com.sultanagung1.sista.core.accessibility

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

// ============================================================================
// 1. Heading & Hierarchy Semantics (WCAG 2.4.6 & 1.3.1)
// ============================================================================

/**
 * Marks this composable as an accessible heading for TalkBack navigation.
 * Screen readers can jump between headings quickly.
 */
fun Modifier.talkBackHeading(label: String): Modifier = this.semantics {
    heading()
    contentDescription = label
}

/**
 * Universal Sulaone heading modifier with optional heading level and custom content description.
 */
fun Modifier.sulaoneHeading(title: String? = null, isHeading: Boolean = true): Modifier = this.semantics {
    if (isHeading) {
        heading()
    }
    if (!title.isNullOrBlank()) {
        contentDescription = title
    }
}

// ============================================================================
// 2. Interactive Actions & Badges (WCAG 4.1.2 Name, Role, Value)
// ============================================================================

/**
 * Legacy TalkBack action modifier retained for backwards compatibility.
 */
fun Modifier.talkBackAction(label: String, state: String? = null): Modifier = this.semantics {
    contentDescription = label
    if (state != null) {
        stateDescription = state
    }
}

/**
 * Accessible badge semantics for color-coded status badges ("LUNAS", "HADIR", etc.).
 * Ensures TalkBack announces both what the badge is and its current evaluated state.
 */
fun Modifier.sulaoneBadgeSemantics(
    label: String,
    state: String? = null,
    role: Role = Role.Button
): Modifier = this.semantics(mergeDescendants = true) {
    this.role = role
    contentDescription = label
    if (state != null) {
        stateDescription = state
    }
}

/**
 * Sets explicit state description for dynamic components (e.g. "Tuntas KKTP", "Hadir Tepat Waktu").
 */
fun Modifier.sulaoneStateDescription(state: String): Modifier = this.semantics {
    stateDescription = state
}

/**
 * Adds custom TalkBack accessibility actions (e.g., "Bayar Tagihan Sekarang", "Hubungi Wali Kelas").
 */
fun Modifier.sulaoneCustomAction(label: String, onAction: () -> Boolean): Modifier = this.semantics {
    customActions = listOf(CustomAccessibilityAction(label, onAction))
}

// ============================================================================
// 3. Touch Target Compliance (WCAG 2.2 AA SC 2.5.8 & SC 2.5.5)
// ============================================================================

/**
 * Guarantees minimum interactive touch target of at least 48x48 dp.
 * Combines Material 3 minimum interactive component size with explicit default min dimensions.
 */
fun Modifier.sulaoneInteractiveTouchTarget(minSize: Dp = 48.dp): Modifier = this
    .minimumInteractiveComponentSize()
    .defaultMinSize(minWidth = minSize, minHeight = minSize)

// ============================================================================
// 4. Canvas Chart & Media Semantics (WCAG 1.1.1 Non-text Content)
// ============================================================================

/**
 * Enriches custom Canvas charts with descriptive TalkBack text and Role.Image.
 */
fun Modifier.sulaoneChartSemantics(
    title: String,
    summary: String,
    role: Role = Role.Image
): Modifier = this.semantics(mergeDescendants = true) {
    this.role = role
    contentDescription = "$title: $summary"
}

/**
 * Clears accessibility semantics for purely decorative visual elements,
 * preventing screen readers from announcing redundant clutter.
 */
fun Modifier.sulaoneDecorative(): Modifier = this.clearAndSetSemantics { }

/**
 * Marks this composable as a live region that announces dynamic updates politely or aggressively.
 */
fun Modifier.sulaoneLiveRegion(politeness: LiveRegionMode = LiveRegionMode.Polite): Modifier = this.semantics {
    liveRegion = politeness
}

// ============================================================================
// 5. WCAG 2.2 Color Contrast Utilities & Relative Luminance
// ============================================================================

object AccessibilityContrastUtils {

    /**
     * Calculates the relative luminance of a color according to WCAG 2.2 specifications.
     * Normalized channels [0..1] linearized with gamma 2.4.
     */
    fun calculateLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.04045) {
                c / 12.92
            } else {
                ((c + 0.055) / 1.055).pow(2.4)
            }
        }
        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Calculates the contrast ratio between foreground and background colors: (L1 + 0.05) / (L2 + 0.05).
     * Returns a value between 1.0 (no contrast) and 21.0 (black/white).
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Verifies if contrast meets WCAG 2.2 Level AA:
     * - Normal text (< 18pt or < 14pt bold): minimum 4.5:1
     * - Large text (>= 18pt or >= 14pt bold): minimum 3.0:1
     */
    fun isWcagAaCompliant(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        val threshold = if (isLargeText) 3.0 else 4.5
        return ratio >= threshold
    }

    /**
     * Verifies if contrast meets WCAG 2.2 Level AAA:
     * - Normal text: minimum 7.0:1
     * - Large text: minimum 4.5:1
     */
    fun isWcagAaaCompliant(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        val threshold = if (isLargeText) 4.5 else 7.0
        return ratio >= threshold
    }
}

// ============================================================================
// 6. Accessibility Text Formatters for Screen Readers
// ============================================================================

object AccessibilityFormatters {

    fun attendanceState(status: String): String {
        return when (status.trim().lowercase()) {
            "hadir", "hadir di sekolah", "hadir tepat waktu" -> "Status kehadiran: Hadir tepat waktu di sekolah"
            "terlambat" -> "Status kehadiran: Terlambat hadir"
            "sakit" -> "Status kehadiran: Izin sakit dengan surat"
            "izin" -> "Status kehadiran: Izin keperluan keluarga"
            "alpha", "alpa" -> "Status kehadiran: Alpa, tanpa keterangan"
            else -> "Status kehadiran: $status"
        }
    }

    fun billingState(isPaid: Boolean, amount: String? = null): String {
        val statusText = if (isPaid) "Lunas dan sah" else "Belum dibayar"
        return if (amount.isNullOrBlank()) {
            "Status pembayaran: $statusText"
        } else {
            "Status pembayaran: $statusText, total $amount"
        }
    }

    fun kktpState(isPassed: Boolean, score: Float, threshold: Float = 75f): String {
        val passLabel = if (isPassed) "Tuntas KKTP" else "Perlu Remedial"
        return "$passLabel, nilai $score dari kriteria ketuntasan $threshold"
    }

    fun radarChartSummary(title: String, axes: List<Pair<String, Float>>): String {
        if (axes.isEmpty()) return "$title: Tidak ada data kompetensi."
        val pointsDesc = axes.joinToString(", ") { "${it.first}: ${it.second.roundToInt()} poin" }
        val avg = axes.map { it.second }.average().toFloat().roundToInt()
        return "$title. Rata-rata kompetensi $avg poin dari 100. Rincian: $pointsDesc."
    }

    fun donutChartSummary(title: String, segments: List<Pair<String, Float>>): String {
        if (segments.isEmpty()) return "$title: Tidak ada data segmen."
        val total = segments.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
        val segDesc = segments.joinToString(", ") { (label, value) ->
            val pct = ((value / total) * 100f).roundToInt()
            "$label $pct persen"
        }
        return "$title. Distribusi data: $segDesc."
    }

    fun lineChartSummary(title: String, points: List<Float>): String {
        if (points.isEmpty()) return "$title: Grafik tidak memiliki titik data."
        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 0f
        val firstVal = points.first()
        val lastVal = points.last()
        val trend = when {
            lastVal > firstVal -> "Tren meningkat"
            lastVal < firstVal -> "Tren menurun"
            else -> "Tren stabil"
        }
        return "$title. $trend dari $firstVal ke $lastVal. Nilai terendah $minVal, nilai tertinggi $maxVal."
    }

    fun streakHeatmapSummary(totalDays: Int, activeDaysCount: Int): String {
        val pct = if (totalDays > 0) ((activeDaysCount.toFloat() / totalDays) * 100f).roundToInt() else 0
        return "Kalender Keaktifan: $activeDaysCount dari $totalDays hari aktif ($pct persen keaktifan)."
    }
}
