package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class LoadingTier {
    Hidden,
    Spinner,
    Skeleton
}

object LoadingTierTiming {
    fun tierFor(elapsedMs: Long): LoadingTier = when {
        elapsedMs < 300 -> LoadingTier.Hidden
        elapsedMs < 1000 -> LoadingTier.Spinner
        else -> LoadingTier.Skeleton
    }
}

@Composable
fun SulaoneTieredLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    skeletonContent: @Composable () -> Unit
) {
    if (isLoading) {
        Box(modifier = modifier) {
            skeletonContent()
        }
    }
}
