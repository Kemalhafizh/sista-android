package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Translucent "glass" card. Note [blurRadius] is accepted for API
 * compatibility only — stock Compose has no backdrop blur (Modifier.blur
 * blurs a composable's OWN content, not what's behind it), so this is a
 * tinted translucent surface, not a real frosted-glass blur.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp, // Preserved for API backward compatibility
    tintColor: Color = MaterialTheme.colorScheme.surface,
    tintAlpha: Float = 0.82f,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    elevation: Dp = 3.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = tintColor.copy(alpha = tintAlpha),
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = elevation,
        content = content
    )
}

/**
 * FASE 76.2 — true once the item with [itemKey] has fully scrolled out of
 * the viewport (and the list has laid out at least once, so the very first
 * frame — before any layout pass — never reports "scrolled off").
 */
@Composable
fun rememberIsItemScrolledOff(listState: LazyListState, itemKey: Any): State<Boolean> =
    remember(listState, itemKey) {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            visible.isNotEmpty() && visible.none { it.key == itemKey }
        }
    }

/**
 * FASE 76.2 — the one glassmorphism pattern the 2026 trend research found
 * genuinely durable: a sticky top bar that appears once a screen's big
 * header has scrolled away, with the content continuing to scroll visibly
 * underneath it. Overlay it at the top of the same Box as the LazyColumn and
 * drive [visible] with [rememberIsItemScrolledOff] on the header item.
 *
 * Honest limitation: this is a translucent tinted panel with a hairline
 * edge, NOT a real backdrop blur — stock Compose cannot blur what's behind
 * a composable, and minSdk here is 26 while RenderEffect needs 31. Real
 * backdrop blur would need a third-party library (e.g. Haze), which is a
 * dependency decision this change deliberately does not make.
 *
 * Tapping it scrolls back to the top when [onClick] is provided (the
 * familiar tap-the-title-bar gesture).
 */
@Composable
fun SulaoneGlassTopBar(
    visible: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically { fullHeight -> -fullHeight } + fadeIn(),
        exit = slideOutVertically { fullHeight -> -fullHeight } + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            onClickLabel = "Gulir ke atas",
                            role = Role.Button,
                            onClick = onClick
                        )
                    } else {
                        Modifier
                    }
                ),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            shadowElevation = 0.dp
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
