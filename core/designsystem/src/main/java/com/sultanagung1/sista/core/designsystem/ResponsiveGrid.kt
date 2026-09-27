package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Konfigurasi jumlah kolom responsif berdasarkan WindowWidthSizeClass (FASE 55.2).
 */
data class ResponsiveGridColumns(
    val compact: Int = 1,
    val medium: Int = 2,
    val expanded: Int = 3
) {
    companion object {
        val SingleToTriple = ResponsiveGridColumns(compact = 1, medium = 2, expanded = 3)
        val CardsTwoToFour = ResponsiveGridColumns(compact = 2, medium = 3, expanded = 4)
        val BentoDashboard = ResponsiveGridColumns(compact = 1, medium = 2, expanded = 4)
    }
}

/**
 * Menghitung jumlah kolom optimal berdasarkan lebar dp layar.
 */
fun calculateResponsiveColumns(
    widthDp: Int,
    columns: ResponsiveGridColumns = ResponsiveGridColumns.SingleToTriple
): Int {
    return when {
        widthDp < AdaptiveBreakpoints.COMPACT_MAX_WIDTH_DP -> columns.compact
        widthDp < AdaptiveBreakpoints.MEDIUM_MAX_WIDTH_DP -> columns.medium
        else -> columns.expanded
    }
}

/**
 * ResponsiveGrid yang secara otomatis menyesuaikan jumlah kolom berdasarkan lebar kontainer
 * atau WindowWidthSizeClass perangkat (FASE 55.2).
 */
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    minColumnWidth: Dp = 160.dp,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp,
    content: LazyGridScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val columnCount = (maxWidth / minColumnWidth).toInt().coerceAtLeast(1)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
    }
}

/**
 * Bento Grid responsif yang secara spesifik dirancang untuk kartu dashboard & widget analitik.
 * - Compact (Smartphone): 1 kolom vertikal
 * - Medium (Tablet Portrait / Foldable): 2 kolom
 * - Expanded (Tablet Landscape / Desktop): 3 atau 4 kolom
 */
@Composable
fun ResponsiveBentoGrid(
    modifier: Modifier = Modifier,
    gridColumns: ResponsiveGridColumns = ResponsiveGridColumns.BentoDashboard,
    horizontalSpacing: Dp = 14.dp,
    verticalSpacing: Dp = 14.dp,
    content: LazyGridScope.() -> Unit
) {
    val windowWidthClass = LocalWindowWidthSizeClass.current
    val calculatedColumns = when (windowWidthClass) {
        WindowWidthSizeClass.Compact -> gridColumns.compact
        WindowWidthSizeClass.Medium -> gridColumns.medium
        WindowWidthSizeClass.Expanded -> gridColumns.expanded
        else -> gridColumns.compact
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(calculatedColumns),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier = modifier,
        content = content
    )
}
