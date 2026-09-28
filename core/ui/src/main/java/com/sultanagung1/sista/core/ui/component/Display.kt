package com.sultanagung1.sista.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors

/** Small status label: "Hadir", "Terlambat", "Lunas", "Belum dinilai"… */
@Composable
fun StatusPill(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = tone.colors()
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.container)
            .padding(horizontal = 10.dp, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.onContainer, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(text, style = SistaTheme.typography.labelMedium, color = colors.onContainer, maxLines = 1)
    }
}

/** Title of a content section, with an optional action ("Lihat semua"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = SistaTheme.typography.titleMedium,
            color = SistaTheme.colors.onSurface,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (actionLabel != null && onAction != null) {
            SistaButton(actionLabel, onAction, variant = ButtonVariant.Text)
        }
    }
}

/**
 * One row of a list: leading visual, headline, supporting text, trailing
 * content. The whole row is the touch target when [onClick] is set.
 */
@Composable
fun SistaListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    overline: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(Spacing.lg))
        }
        Column(Modifier.weight(1f)) {
            if (overline != null) {
                Text(overline, style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant, maxLines = 1)
            }
            Text(
                headline,
                style = SistaTheme.typography.bodyLarge,
                color = SistaTheme.colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting != null) {
                Text(
                    supporting,
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
    }
}

/** Icon inside a tinted rounded square — the leading visual of list rows and tiles. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Brand,
    size: Dp = 40.dp,
) {
    val colors = tone.colors()
    Box(
        modifier = modifier
            .size(size)
            .clip(SistaTheme.shapes.medium)
            .background(colors.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onContainer, modifier = Modifier.size(size * 0.5f))
    }
}

/** Initials in a circle, colour picked from the name so it stays stable. */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val tones = listOf(StatusTone.Brand, StatusTone.Info, StatusTone.Warning, StatusTone.Success)
    val colors = tones[(name.hashCode() and Int.MAX_VALUE) % tones.size].colors()
    val initials = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.container),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = colors.onContainer, style = SistaTheme.typography.labelLarge.copy(fontSize = (size.value * 0.36f).sp))
    }
}

/** A number that matters at a glance: attendance %, unpaid bills, today's lessons. */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    icon: ImageVector? = null,
    tone: StatusTone = StatusTone.Neutral,
    onClick: (() -> Unit)? = null,
) {
    SistaCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconBadge(icon, tone = if (tone == StatusTone.Neutral) StatusTone.Brand else tone, size = 32.dp)
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(label, style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.size(Spacing.sm))
        Text(
            value,
            style = SistaTheme.typography.headlineSmall,
            color = if (tone == StatusTone.Neutral) SistaTheme.colors.onSurface else tone.colors().content,
            maxLines = 1,
        )
        if (supporting != null) {
            Text(supporting, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant, maxLines = 2)
        }
    }
}

/** Inline message inside a screen (not a toast): offline data, deadline, success. */
@Composable
fun InlineBanner(
    message: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = tone.colors()
    val icon = when (tone) {
        StatusTone.Success -> Icons.Outlined.CheckCircle
        StatusTone.Warning -> Icons.Outlined.WarningAmber
        StatusTone.Danger -> Icons.Outlined.ErrorOutline
        else -> Icons.Outlined.Info
    }
    Surface(modifier = modifier.fillMaxWidth(), shape = SistaTheme.shapes.medium, color = colors.container, contentColor = colors.onContainer) {
        Row(Modifier.padding(start = Spacing.lg, end = Spacing.xs, top = Spacing.md, bottom = Spacing.md)) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(20.dp))
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f).padding(end = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                if (title != null) Text(title, style = SistaTheme.typography.titleSmall)
                Text(message, style = SistaTheme.typography.bodyMedium)
                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        modifier = Modifier.heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.onContainer),
                    ) {
                        Text(actionLabel, style = SistaTheme.typography.labelLarge)
                    }
                }
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Tutup")
                }
            }
        }
    }
}
