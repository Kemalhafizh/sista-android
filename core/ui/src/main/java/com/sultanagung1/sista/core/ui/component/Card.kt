package com.sultanagung1.sista.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

enum class CardVariant { Filled, Outlined, Highlighted }

/**
 * Content container. Flat by design (no drop shadows): hierarchy comes from
 * surface tone, not elevation, so it stays clean in dark mode too.
 */
@Composable
fun SistaCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(contentPadding), content = content)
    }
    val shape = SistaTheme.shapes.large
    when (variant) {
        CardVariant.Outlined -> {
            val colors = CardDefaults.outlinedCardColors(containerColor = SistaTheme.colors.surface)
            val border = BorderStroke(1.dp, SistaTheme.colors.outlineVariant)
            if (onClick != null) OutlinedCard(onClick, modifier, shape = shape, colors = colors, border = border, content = body)
            else OutlinedCard(modifier, shape = shape, colors = colors, border = border, content = body)
        }
        else -> {
            val colors = CardDefaults.cardColors(
                containerColor = if (variant == CardVariant.Highlighted) SistaTheme.colors.primaryContainer
                else SistaTheme.colors.surfaceContainerLow,
                contentColor = if (variant == CardVariant.Highlighted) SistaTheme.colors.onPrimaryContainer
                else SistaTheme.colors.onSurface,
            )
            if (onClick != null) Card(onClick, modifier, shape = shape, colors = colors, content = body)
            else Card(modifier, shape = shape, colors = colors, content = body)
        }
    }
}

