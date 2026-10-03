package com.sultanagung1.sista.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.data.model.NotificationItem
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.canOpen
import java.time.LocalDate

/**
 * This account's notifications, newest first and grouped by day. Opening one
 * marks it read and goes where it points, when this account may open that.
 */
@Composable
fun NotificationCenterScreen(
    viewModel: NotificationViewModel,
    onNavigateDeepLink: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val capabilities = LocalCapabilityState.current
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) refreshRequested = false }

    NotificationCenterContent(
        state = state,
        today = remember(state.notifications) { LocalDate.now(SCHOOL_ZONE) },
        refreshing = refreshRequested && state.isLoading,
        canOpen = { route -> capabilities.canOpen(route) },
        onRefresh = {
            refreshRequested = true
            viewModel.load()
        },
        onRetry = viewModel::load,
        onFilter = viewModel::setFilter,
        onOpen = { item ->
            viewModel.markRead(item.id)
            item.deepLinkRoute?.takeIf { capabilities.canOpen(it) }?.let(onNavigateDeepLink)
        },
        onMarkRead = viewModel::markRead,
        onMarkAllRead = viewModel::markAllRead,
        onDelete = viewModel::delete,
        onDismissMessage = viewModel::dismissActionMessage,
        onNavigateBack = onNavigateBack,
        onOpenSettings = onNavigateToSettings,
    )
}

/** The notification center without a ViewModel, for previews and screenshots. */
@Composable
fun NotificationCenterContent(
    state: NotificationUiState,
    today: LocalDate,
    refreshing: Boolean,
    canOpen: (String) -> Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onFilter: (NotificationFilter) -> Unit,
    onOpen: (NotificationItem) -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDelete: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    onOpenSettings: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbar.showSnackbar(it)
            onDismissMessage()
        }
    }
    val notifications = state.notifications
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SistaTopBar(
                    title = "Notifikasi",
                    scrollBehavior = scrollBehavior,
                    subtitle = if (state.unreadCount > 0) "${state.unreadCount} belum dibaca" else null,
                    onBack = onNavigateBack,
                    actions = {
                        if (state.unreadCount > 0) {
                            IconButton(onClick = onMarkAllRead) { Icon(Icons.Outlined.DoneAll, contentDescription = "Tandai semua sudah dibaca") }
                        }
                        IconButton(onClick = onOpenSettings) { Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan notifikasi") }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("notification_center_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    when {
                        notifications == null && state.errorMessage != null -> item(key = "error") {
                            ErrorState(title = "Notifikasi belum bisa dimuat", body = state.errorMessage, onRetry = onRetry)
                        }
                        notifications == null -> item(key = "loading") { SkeletonList(rows = 5) }
                        notifications.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = "Belum ada notifikasi",
                                body = "Kabar presensi, tagihan, dan layanan sekolah muncul di sini.",
                                icon = Icons.Outlined.NotificationsNone,
                            )
                        }
                        else -> {
                            item(key = "filter") {
                                FilterChipRow(
                                    options = NotificationFilter.values().toList(),
                                    selected = state.filter,
                                    onSelect = onFilter,
                                    label = { if (it == NotificationFilter.Unread && state.unreadCount > 0) "${it.label} (${state.unreadCount})" else it.label },
                                    contentPadding = PaddingValues(vertical = Spacing.sm),
                                )
                            }
                            val visible = state.visible
                            if (visible.isEmpty()) {
                                item(key = "all_read") {
                                    EmptyState(title = "Semua sudah dibaca", icon = Icons.Outlined.DoneAll)
                                }
                            }
                            groupByDay(visible, today) { parseInstant(it.timestamp) }.forEach { (day, items) ->
                                item(key = "day_$day") {
                                    Text(
                                        day,
                                        style = SistaTheme.typography.labelLarge,
                                        color = SistaTheme.colors.onSurfaceVariant,
                                        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs),
                                    )
                                }
                                items(items, key = { it.id }) { item ->
                                    NotificationRow(
                                        item = item,
                                        opens = item.deepLinkRoute?.let(canOpen) == true,
                                        onOpen = { onOpen(item) },
                                        onMarkRead = { onMarkRead(item.id) },
                                        onDelete = { onDelete(item.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    opens: Boolean,
    onOpen: () -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
) {
    val channel = channelOf(item.channel)
    var menu by remember { mutableStateOf(false) }
    SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconBadge(icon = channel.icon(), tone = channel.tone())
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (!item.isRead) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SistaTheme.colors.primary),
                        )
                    }
                    Text(
                        item.title,
                        style = SistaTheme.typography.titleSmall,
                        fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (item.body.isNotBlank()) {
                    Text(
                        item.body,
                        style = SistaTheme.typography.bodyMedium,
                        color = SistaTheme.colors.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    listOfNotNull(parseInstant(item.timestamp)?.let(::clock), channel.label, if (opens) "Ketuk untuk membuka" else null)
                        .joinToString(" · "),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { menu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Pilihan untuk ${item.title}")
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (!item.isRead) {
                        DropdownMenuItem(text = { Text("Tandai sudah dibaca") }, onClick = { menu = false; onMarkRead() })
                    }
                    DropdownMenuItem(text = { Text("Hapus") }, onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}
