package com.sultanagung1.sista.core.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

/** One top-level destination of the app shell. */
@Immutable
data class NavEntry(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    /** Unread count; shown as a dot above 0, capped at "99+". */
    val badge: Int = 0,
)

/**
 * Bottom navigation for phones. The same four places for every account;
 * what differs between roles is what is inside them.
 */
@Composable
fun SistaNavigationBar(
    entries: List<NavEntry>,
    selectedKey: String?,
    onSelect: (NavEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = SistaTheme.colors.surfaceContainer,
        contentColor = SistaTheme.colors.onSurfaceVariant,
        tonalElevation = 0.dp,
    ) {
        entries.forEach { entry ->
            val selected = entry.key == selectedKey
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(entry) },
                icon = { EntryIcon(entry, selected) },
                label = {
                    Text(entry.label, style = SistaTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SistaTheme.colors.onSecondaryContainer,
                    selectedTextColor = SistaTheme.colors.onSurface,
                    indicatorColor = SistaTheme.colors.secondaryContainer,
                    unselectedIconColor = SistaTheme.colors.onSurfaceVariant,
                    unselectedTextColor = SistaTheme.colors.onSurfaceVariant,
                ),
            )
        }
    }
}

/** Side navigation for tablets and unfolded foldables. Same entries as the bar. */
@Composable
fun SistaNavigationRail(
    entries: List<NavEntry>,
    selectedKey: String?,
    onSelect: (NavEntry) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = SistaTheme.colors.surfaceContainer,
        header = header,
    ) {
        Spacer(Modifier.height(Spacing.sm))
        entries.forEach { entry ->
            val selected = entry.key == selectedKey
            NavigationRailItem(
                selected = selected,
                onClick = { if (!selected) onSelect(entry) },
                icon = { EntryIcon(entry, selected) },
                label = { Text(entry.label, style = SistaTheme.typography.labelMedium, maxLines = 1) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = SistaTheme.colors.onSecondaryContainer,
                    selectedTextColor = SistaTheme.colors.onSurface,
                    indicatorColor = SistaTheme.colors.secondaryContainer,
                    unselectedIconColor = SistaTheme.colors.onSurfaceVariant,
                    unselectedTextColor = SistaTheme.colors.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun EntryIcon(entry: NavEntry, selected: Boolean) {
    val icon = if (selected) entry.selectedIcon else entry.icon
    if (entry.badge > 0) {
        val text = if (entry.badge > 99) "99+" else entry.badge.toString()
        val unreadLabel = stringResource(R.string.core_unread_count, text)
        BadgedBox(
            badge = {
                Badge(Modifier.semantics { contentDescription = unreadLabel }) { Text(text) }
            },
        ) { Icon(icon, contentDescription = null) }
    } else {
        Icon(icon, contentDescription = null)
    }
}

