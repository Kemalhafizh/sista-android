package com.sultanagung1.sista.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

/** Nothing to show — says why, and what to do next when there is something to do. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MessageState(icon, title, body, actionLabel, onAction, ButtonVariant.Secondary, modifier)
}

/** Loading failed. Always offers a retry; the message says what went wrong in plain language. */
@Composable
fun ErrorState(
    title: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: ImageVector = Icons.Outlined.CloudOff,
) {
    MessageState(icon, title, body, "Coba lagi", onRetry, ButtonVariant.Primary, modifier)
}

@Composable
private fun MessageState(
    icon: ImageVector,
    title: String,
    body: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    actionVariant: ButtonVariant,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SistaTheme.colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = SistaTheme.colors.onSurfaceVariant, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = SistaTheme.typography.titleMedium, color = SistaTheme.colors.onSurface, textAlign = TextAlign.Center)
        if (body != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(body, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            SistaButton(
                actionLabel,
                onAction,
                variant = actionVariant,
                leadingIcon = if (actionVariant == ButtonVariant.Primary) Icons.Outlined.Refresh else null,
            )
        }
    }
}

/**
 * Placeholder block that pulses while content loads. Static in previews and
 * screenshot tests so images are deterministic.
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 14.dp,
) {
    val alpha = if (LocalInspectionMode.current) 1f else {
        val t = rememberInfiniteTransition(label = "skeleton")
        val a by t.animateFloat(0.55f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "skeletonAlpha")
        a
    }
    Box(
        modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .alpha(alpha)
            .clip(SistaTheme.shapes.extraSmall)
            .background(SistaTheme.colors.surfaceContainerHighest),
    )
}

/** Skeleton shaped like [SistaListItem] rows, the most common loading layout. */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    rows: Int = 5,
) {
    Column(
        modifier.semantics { contentDescription = "Memuat" },
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        repeat(rows) {
            Row(
                Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBlock(width = 40.dp, height = 40.dp)
                Spacer(Modifier.width(Spacing.lg))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.7f))
                    SkeletonBlock(Modifier.fillMaxWidth(0.45f), height = 12.dp)
                }
            }
        }
    }
}
