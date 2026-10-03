package com.sultanagung1.sista.ui.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.AnnouncementItem
import java.time.LocalDate
import java.time.ZoneId

/** Announcements meant for this account, pinned first, with what is unread or awaits confirmation. */
@Composable
fun AnnouncementFeedScreen(
    viewModel: AnnouncementFeedViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val state by viewModel.uiState.collectAsState()
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) refreshRequested = false }

    AnnouncementFeedContent(
        state = state,
        today = remember(state.announcements) { LocalDate.now(ZoneId.of("Asia/Jakarta")) },
        refreshing = refreshRequested && state.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.load()
        },
        onRetry = viewModel::load,
        onCategory = viewModel::selectCategory,
        onSearch = viewModel::search,
        onOpen = { id ->
            viewModel.markOpened(id)
            onNavigateToDetail(id)
        },
        onDismissBanner = viewModel::dismissBanner,
        onNavigateBack = onNavigateBack,
    )
}

/** The announcement list without a ViewModel, for previews and screenshots. */
@Composable
fun AnnouncementFeedContent(
    state: AnnouncementFeedUiState,
    today: LocalDate,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCategory: (AnnouncementCategory) -> Unit,
    onSearch: (String) -> Unit,
    onOpen: (String) -> Unit,
    onDismissBanner: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val all = state.announcements
    val unread = all.orEmpty().count(::isUnread)
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SistaTopBar(
                    title = "Pengumuman",
                    scrollBehavior = scrollBehavior,
                    subtitle = if (unread > 0) "$unread belum dibaca" else null,
                    onBack = onNavigateBack,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("announcement_feed_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    state.liveBanner?.let { message ->
                        item(key = "live") {
                            InlineBanner(
                                message = message,
                                tone = if (state.liveIsEmergency) StatusTone.Danger else StatusTone.Info,
                                title = if (state.liveIsEmergency) "Siaran darurat" else null,
                                onDismiss = onDismissBanner,
                            )
                        }
                    }
                    item(key = "search") {
                        SistaTextField(
                            value = state.query,
                            onValueChange = onSearch,
                            label = "Cari pengumuman",
                            leadingIcon = Icons.Outlined.Search,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item(key = "categories") {
                        FilterChipRow(
                            options = AnnouncementCategory.values().toList(),
                            selected = state.category,
                            onSelect = onCategory,
                            label = { it.label },
                            contentPadding = PaddingValues(0.dp),
                        )
                    }
                    val visible = state.visible
                    when {
                        all == null && state.errorMessage != null -> item(key = "error") {
                            ErrorState(title = "Pengumuman belum bisa dimuat", body = state.errorMessage, onRetry = onRetry)
                        }
                        all == null -> item(key = "loading") { SkeletonList(rows = 4) }
                        all.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = if (state.category == AnnouncementCategory.All) "Belum ada pengumuman" else "Belum ada pengumuman ${state.category.label.lowercase()}",
                                body = "Pengumuman sekolah untuk Anda muncul di sini.",
                                icon = Icons.Outlined.Campaign,
                            )
                        }
                        visible.isEmpty() -> item(key = "no_match") {
                            EmptyState(title = "Tidak ada yang cocok dengan \"${state.query.trim()}\"", icon = Icons.Outlined.SearchOff)
                        }
                        else -> items(visible, key = { it.id }) { item ->
                            AnnouncementCard(item, today, onClick = { onOpen(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(item: AnnouncementItem, today: LocalDate, onClick: () -> Unit) {
    val category = AnnouncementCategory.of(item.category)
    val unread = isUnread(item)
    SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                IconBadge(icon = category.icon(), tone = if (isUrgent(item)) StatusTone.Danger else category.tone())
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        if (unread) {
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
                            fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (item.isPinned) {
                            Icon(Icons.Outlined.PushPin, contentDescription = "Disematkan", tint = SistaTheme.colors.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        listOfNotNull(item.author.takeIf { it.isNotBlank() }, publishedLabel(item, today).takeIf { it.isNotBlank() }).joinToString(" · "),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            }
            if (item.summary.isNotBlank()) {
                Text(item.summary, style = SistaTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            val pills = buildList {
                if (isUrgent(item)) add("Penting" to StatusTone.Danger)
                if (needsAcknowledgement(item)) add("Perlu konfirmasi" to StatusTone.Warning)
                add(category.label to StatusTone.Neutral)
                item.audience?.takeIf { it.isNotBlank() && it != "Semua" }?.let { add(it to StatusTone.Neutral) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                pills.forEach { (text, tone) -> StatusPill(text = text, tone = tone) }
            }
        }
    }
}
