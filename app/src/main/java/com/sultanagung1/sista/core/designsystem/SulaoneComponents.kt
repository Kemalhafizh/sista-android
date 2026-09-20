package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable

@Composable
fun SulaoneTopBar(
    title: String,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateBack != null) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}

@Composable
fun SulaoneCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.isDark()
    val resolvedBg = if (isDark && (backgroundColor == Emerald50 || backgroundColor == Slate50 || backgroundColor == Color.White || backgroundColor == Color(0xFFEBFBF3) || backgroundColor == Color(0xFFF8FAFC))) {
        MaterialTheme.colorScheme.surface
    } else {
        backgroundColor
    }
    val resolvedBorder = if (isDark && (borderColor == Emerald200 || borderColor == Slate300 || borderColor == Slate200)) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    } else {
        borderColor
    }

    val cardModifier = if (onClick != null) {
        modifier
            .shadow(elevation, shape)
            .clip(shape)
            .border(1.dp, resolvedBorder, shape)
            .background(resolvedBg)
            .clickable { onClick() }
            .padding(16.dp)
    } else {
        modifier
            .shadow(elevation, shape)
            .clip(shape)
            .border(1.dp, resolvedBorder, shape)
            .background(resolvedBg)
            .padding(16.dp)
    }

    Column(modifier = cardModifier) {
        content()
    }
}

@Composable
fun SulaoneGradientCard(
    modifier: Modifier = Modifier,
    brush: Brush = Brush.linearGradient(listOf(Emerald800, Emerald600)),
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(brush)
            .clickable { onClick() }
            .padding(20.dp)
    } else {
        modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(brush)
            .padding(20.dp)
    }

    Column(modifier = cardModifier) {
        content()
    }
}

@Composable
fun SulaoneButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = Emerald700,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth(),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SulaoneBadge(
    text: String,
    containerColor: Color = Emerald100,
    contentColor: Color = Emerald800,
    modifier: Modifier = Modifier,
    stateDescription: String? = null
) {
    val stateDesc = stateDescription ?: "Status: $text"
    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                this.stateDescription = stateDesc
            }
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = contentColor
        )
    }
}

@Composable
fun SulaoneEmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Inbox,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SulaoneErrorBanner(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AccentRose.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = AccentRose,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = AccentRose,
                modifier = Modifier.weight(1f)
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onRetry) {
                    Text(
                        text = "Coba Lagi",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentRose,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBentoCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
    elevation: Dp = 0.dp,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptics = com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper()
    val isDark = MaterialTheme.colorScheme.surface.isDark()
    val resolvedBg = if (isDark && (backgroundColor == Emerald50 || backgroundColor == Slate50 || backgroundColor == Color.White || backgroundColor == Color(0xFFEBFBF3) || backgroundColor == Color(0xFFEFF6FF) || backgroundColor == Color(0xFFF5F3FF) || backgroundColor == Color(0xFFF8FAFC))) {
        MaterialTheme.colorScheme.surface
    } else {
        backgroundColor
    }
    val resolvedBorder = if (isDark && (borderColor == Emerald200 || borderColor == Slate300 || borderColor == Slate200 || borderColor == Color(0xFFBFDBFE) || borderColor == Color(0xFFDDD6FE))) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    } else {
        borderColor
    }

    Box(
        modifier = modifier
            .then(
                if (glowColor != null) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = glowColor,
                        spotColor = glowColor
                    )
                } else if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape
                    )
                } else Modifier
            )
            .clip(shape)
            .background(resolvedBg)
            .border(width = 1.dp, color = resolvedBorder, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        haptics.tapLight()
                        onClick()
                    }
                } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun ShimmerSkeleton(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(durationMillis = 1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "shimmer_trans"
    )

    val isDark = MaterialTheme.colorScheme.surface.isDark()
    val baseColor = if (isDark) Slate800 else Slate200
    val highlightColor = if (isDark) Slate700 else Slate100

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = androidx.compose.ui.geometry.Offset(translateAnim - 500f, translateAnim - 500f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun LiveStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AccentGreen
) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alphaAnim))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun AnimatedCounterText(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    prefix: String = "",
    suffix: String = ""
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "counter_anim"
    )
    Text(
        text = "$prefix$animatedValue$suffix",
        style = style,
        color = color,
        modifier = modifier
    )
}

@Composable
fun IslamicArchCard(
    modifier: Modifier = Modifier,
    brush: Brush = Brush.verticalGradient(listOf(Emerald900, Emerald800)),
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    goldBorder: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()
    val borderStroke = if (goldBorder) {
        androidx.compose.foundation.BorderStroke(1.dp, IslamicArchBorder)
    } else null

    Card(
        modifier = modifier
            .shadow(6.dp, shape, ambientColor = EmeraldGlow, spotColor = EmeraldGlow)
            .then(
                if (onClick != null) {
                    Modifier.springPressable {
                        haptics.tapLight()
                        onClick()
                    }
                } else Modifier
            ),
        shape = shape,
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SulaoneGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    Box(
        modifier = modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        haptics.tapLight()
                        onClick()
                    }
                } else Modifier
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun BentoSkeletonLoader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShimmerSkeleton(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(16.dp)
            )
            ShimmerSkeleton(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
        ShimmerSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}

