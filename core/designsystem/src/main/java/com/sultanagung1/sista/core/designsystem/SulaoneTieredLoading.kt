package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class LoadingTier {
    Hidden,
    Spinner,
    Skeleton
}

object LoadingTierTiming {
    /** Before this, show nothing: most cached/fast responses land here and a flash of UI is noise. */
    const val SPINNER_AFTER_MS = 300L

    /** From here on, the content-shaped skeleton: the wait is long enough to need a layout preview. */
    const val SKELETON_AFTER_MS = 1_000L

    fun tierFor(elapsedMs: Long): LoadingTier = when {
        elapsedMs < SPINNER_AFTER_MS -> LoadingTier.Hidden
        elapsedMs < SKELETON_AFTER_MS -> LoadingTier.Spinner
        else -> LoadingTier.Skeleton
    }
}

/**
 * FASE 76.5 tiered first-load indicator: nothing for the first 300 ms, a small
 * spinner until 1 s, then [skeletonContent] (which should mirror the real
 * layout of the screen).
 *
 * Correction 2026-09-27: the first version of this composable rendered
 * [skeletonContent] immediately and never consulted [LoadingTierTiming], so the
 * documented 0-300 ms / 300 ms-1 s tiers did not exist at runtime. The clock
 * starts when [isLoading] becomes true, and restarts on the next load.
 *
 * Only for a *first* load (nothing to show yet). When refreshing a list that
 * already has data, keep the data on screen instead of calling this.
 */
@Composable
fun SulaoneTieredLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    skeletonContent: @Composable () -> Unit
) {
    if (!isLoading) return

    var tier by remember { mutableStateOf(LoadingTier.Hidden) }
    LaunchedEffect(Unit) {
        tier = LoadingTierTiming.tierFor(0)
        delay(LoadingTierTiming.SPINNER_AFTER_MS)
        tier = LoadingTierTiming.tierFor(LoadingTierTiming.SPINNER_AFTER_MS)
        delay(LoadingTierTiming.SKELETON_AFTER_MS - LoadingTierTiming.SPINNER_AFTER_MS)
        tier = LoadingTierTiming.tierFor(LoadingTierTiming.SKELETON_AFTER_MS)
    }

    when (tier) {
        // Nothing to draw, but keep the node so TalkBack users still hear
        // that something is loading.
        LoadingTier.Hidden -> Box(modifier = modifier.semantics { contentDescription = "Memuat" })
        LoadingTier.Spinner -> Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .semantics { contentDescription = "Memuat" },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LoadingTier.Skeleton -> Box(modifier = modifier.semantics { contentDescription = "Memuat" }) {
            skeletonContent()
        }
    }
}
