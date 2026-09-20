package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Breakpoint standar Material 3 untuk klasifikasi ukuran layar (FASE 55.1).
 */
object AdaptiveBreakpoints {
    const val COMPACT_MAX_WIDTH_DP = 600
    const val MEDIUM_MAX_WIDTH_DP = 840
}

/**
 * Tipe navigasi adaptif berdasarkan ukuran layar perangkat.
 */
enum class AdaptiveNavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}

/**
 * Helper untuk mengonversi nilai lebar dp ke canonical WindowWidthSizeClass.
 */
fun calculateWindowWidthSizeClass(widthDp: Int): WindowWidthSizeClass {
    return when {
        widthDp < AdaptiveBreakpoints.COMPACT_MAX_WIDTH_DP -> WindowWidthSizeClass.Compact
        widthDp < AdaptiveBreakpoints.MEDIUM_MAX_WIDTH_DP -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}

/**
 * Mapping WindowWidthSizeClass ke AdaptiveNavigationType.
 */
fun WindowWidthSizeClass.toAdaptiveNavigationType(): AdaptiveNavigationType {
    return when (this) {
        WindowWidthSizeClass.Compact -> AdaptiveNavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.Medium -> AdaptiveNavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.Expanded -> AdaptiveNavigationType.PERMANENT_NAVIGATION_DRAWER
        else -> AdaptiveNavigationType.BOTTOM_NAVIGATION
    }
}

/**
 * CompositionLocal untuk menyediakan WindowWidthSizeClass di seluruh pohon Composable.
 */
val LocalWindowWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }

/**
 * Composable helper untuk menghitung dan mengingat WindowWidthSizeClass terkini.
 */
@Composable
fun rememberCurrentWindowWidthSizeClass(): WindowWidthSizeClass {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        calculateWindowWidthSizeClass(configuration.screenWidthDp)
    }
}

/**
 * Scaffold Adaptif yang menyesuaikan komponen navigasi utama berdasarkan WindowWidthSizeClass (FASE 55.3).
 * - Compact: Menampilkan bottom navigation bar di bawah layar.
 * - Medium: Menampilkan navigation rail di sisi kiri (leading side).
 * - Expanded: Menampilkan permanent navigation drawer / expanded rail di sisi kiri.
 */
@Composable
fun AdaptiveScaffold(
    windowWidthClass: WindowWidthSizeClass = rememberCurrentWindowWidthSizeClass(),
    navigationBar: @Composable () -> Unit = {},
    navigationRail: @Composable () -> Unit = {},
    navigationDrawer: @Composable () -> Unit = navigationRail,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalWindowWidthSizeClass provides windowWidthClass) {
        when (windowWidthClass.toAdaptiveNavigationType()) {
            AdaptiveNavigationType.BOTTOM_NAVIGATION -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    Box(modifier = Modifier.fillMaxWidth()) {
                        navigationBar()
                    }
                }
            }
            AdaptiveNavigationType.NAVIGATION_RAIL -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    navigationRail()
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        content()
                    }
                }
            }
            AdaptiveNavigationType.PERMANENT_NAVIGATION_DRAWER -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    navigationDrawer()
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Wrapper ListDetailPaneScaffold Canonical Sulaone (FASE 55.1).
 * Mengatur tampilan dual-pane (Master-Detail side-by-side) pada tablet / foldable (Medium & Expanded),
 * dan single-pane dengan transisi navigasi halus pada smartphone (Compact).
 */
@Composable
fun <T : Any> SulaoneListDetailPaneScaffold(
    selectedItem: T?,
    onSelectItem: (T?) -> Unit,
    modifier: Modifier = Modifier,
    listPaneWidth: Dp = 380.dp,
    listPane: @Composable (isDualPane: Boolean) -> Unit,
    detailPane: @Composable (item: T?, isDualPane: Boolean) -> Unit,
    emptyDetailPlaceholder: @Composable () -> Unit = {}
) {
    val windowWidthClass = LocalWindowWidthSizeClass.current
    val isDualPane = windowWidthClass != WindowWidthSizeClass.Compact

    if (isDualPane) {
        // Mode Dual-Pane (Tablet / Foldable / Desktop)
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Pane Kiri (Master List)
            Box(
                modifier = Modifier
                    .width(listPaneWidth)
                    .fillMaxHeight()
            ) {
                listPane(true)
            }

            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            // Pane Kanan (Detail Content)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (selectedItem != null) {
                    detailPane(selectedItem, true)
                } else {
                    emptyDetailPlaceholder()
                }
            }
        }
    } else {
        // Mode Single-Pane (Smartphone Compact)
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedItem == null) {
                listPane(false)
            } else {
                detailPane(selectedItem, false)
            }
        }
    }
}
