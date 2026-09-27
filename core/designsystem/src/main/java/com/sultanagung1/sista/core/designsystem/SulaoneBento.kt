package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.tokens.SulaoneRadiusTokens

/*
 * FASE 76.2 — Bento primitives that are safe to use INSIDE a LazyColumn.
 *
 * Every dashboard in this app is a LazyColumn. The pre-existing
 * ResponsiveBentoGrid (ResponsiveGrid.kt) is a LazyVerticalGrid, and nesting
 * a vertically-scrolling lazy layout inside another one crashes at measure
 * time ("measured with an infinity maximum height constraints") — which is
 * why it has zero call sites. On phones it also collapses to one column,
 * which isn't a bento layout at all.
 *
 * These primitives are plain Row/Column compositions sized with
 * IntrinsicSize.Min, so tiles in the same row always end up the same height
 * instead of the ragged heights a bare Row gives when one tile has an extra
 * subtitle line. The slot lambdas receive the Modifier the slot must apply
 * (weight + fillMaxHeight), so any tile composable that accepts a modifier
 * (SulaoneMetricCard, ModernBentoCard, SulaoneBentoHeroTile) works as-is.
 *
 * Constraint: never put a SubcomposeLayout (BoxWithConstraints, LazyRow,
 * any Lazy* list) inside a slot — SubcomposeLayout does not support the
 * intrinsic measurement IntrinsicSize.Min relies on and will throw.
 */

/**
 * Asymmetric bento row: one hero tile on the start side spanning the full
 * row height, two stacked tiles on the end side. Put the single most
 * important, time-sensitive metric in [hero].
 */
@Composable
fun BentoHeroSplit(
    modifier: Modifier = Modifier,
    spacing: Dp = 10.dp,
    heroWeight: Float = 1.15f,
    hero: @Composable (Modifier) -> Unit,
    top: @Composable (Modifier) -> Unit,
    bottom: @Composable (Modifier) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        hero(Modifier.weight(heroWeight).fillMaxHeight())
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            top(Modifier.weight(1f).fillMaxWidth())
            bottom(Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/** Two equal-width tiles forced to equal height. */
@Composable
fun BentoPair(
    modifier: Modifier = Modifier,
    spacing: Dp = 10.dp,
    start: @Composable (Modifier) -> Unit,
    end: @Composable (Modifier) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        start(Modifier.weight(1f).fillMaxHeight())
        end(Modifier.weight(1f).fillMaxHeight())
    }
}

/**
 * The hero tile of a [BentoHeroSplit]. Its value is the one place on a
 * dashboard that uses the M3 Expressive emphasized weight — per the M3
 * spec, emphasis only reads as emphasis when it's rare, so the smaller
 * surrounding SulaoneMetricCards deliberately stay at baseline weight.
 *
 * Keep [value] short (a count, or a percentage rounded to one decimal) and
 * put the unit in [unit]: the hero is roughly half the screen width, and a
 * long value (e.g. a Rupiah amount) would ellipsize at large font scales.
 */
@Composable
fun SulaoneBentoHeroTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    unit: String? = null,
    caption: String? = null,
    accent: Color = Emerald700,
    badgeText: String? = null,
    onClick: (() -> Unit)? = null
) {
    ModernBentoCard(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = buildString {
                append(label).append(": ").append(value)
                if (unit != null) append(" ").append(unit)
                if (caption != null) append(". ").append(caption)
            }
        },
        backgroundColor = accent.copy(alpha = 0.08f),
        borderColor = accent.copy(alpha = 0.28f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(SulaoneRadiusTokens.md))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (!badgeText.isNullOrBlank()) {
                    SulaoneBadge(
                        text = badgeText,
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent
                    )
                }
            }

            // Guarantees breathing room even when the row is only as tall as
            // this tile's own content (SpaceBetween alone would collapse to 0).
            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayMedium.emphasized(),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .alignByBaseline()
                            .weight(1f, fill = false)
                    )
                    if (!unit.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!caption.isNullOrBlank()) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
