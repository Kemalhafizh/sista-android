package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.text.style.TextOverflow
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.Avatar
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FeatureTile
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.GreetingHeader
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.OnResume
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.component.greetingFor
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.feature.parent.R
import com.sultanagung1.sista.core.ui.R as CoreUiR
import com.sultanagung1.sista.data.model.ChildVsClassComparison
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.data.model.WeeklyDigest
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen
import java.util.Calendar

/** A parent shortcut. Shown only when the account may open [route]. */
@Immutable
data class ParentShortcut(@StringRes val title: Int, val icon: ImageVector, val route: String)

/** Shortcuts for the chosen child; routes carry the child so every screen shows the same one. */
fun parentShortcuts(childUuid: String?): List<ParentShortcut> = listOfNotNull(
    childUuid?.let { ParentShortcut(R.string.shortcut_child_detail, Icons.Outlined.Person, Screen.ChildDetail.createRoute(it)) },
    ParentShortcut(R.string.shortcut_activity, Icons.Outlined.Timeline, Screen.ChildActivityFeed.createRoute(childUuid)),
    ParentShortcut(R.string.stat_bills, Icons.Outlined.AccountBalanceWallet, Screen.Billing.route),
    ParentShortcut(R.string.shortcut_rules, Icons.Outlined.Gavel, Screen.Discipline.createRoute(childUuid)),
    ParentShortcut(R.string.shortcut_message_teacher, Icons.AutoMirrored.Outlined.Chat, Screen.ConversationList.route),
    ParentShortcut(R.string.shortcut_progress, Icons.Outlined.Insights, Screen.ChildProgress.createRoute(childUuid)),
)

/**
 * The parent's Beranda — the same layout as every role's home: who is signed
 * in, the chosen child, what the school recorded this week, recent events,
 * how the child compares with the class, and the shortcuts this account may use.
 */
@Composable
fun ParentDashboardScreen(
    viewModel: ParentViewModel,
    onNavigateToChildDetail: (String) -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onNavigateToActivityFeed: ((String?) -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    OnResume(viewModel::loadUnreadNotifications)
    val capabilities = LocalCapabilityState.current

    var refreshRequested by remember { mutableStateOf(false) }
    val busy = uiState.isLoading || uiState.isLoadingChildDetail || uiState.isLoadingExperience
    LaunchedEffect(busy) { if (!busy) refreshRequested = false }

    val now = remember(uiState.selectedChild, uiState.isLoading) { DateUtils.nowCalendar() }
    ParentHomeContent(
        greeting = greetingFor(now.get(Calendar.HOUR_OF_DAY)),
        state = uiState,
        nowMillis = now.timeInMillis,
        shortcuts = parentShortcuts(uiState.selectedChild?.uuid).filter { capabilities.canOpen(it.route) },
        refreshing = refreshRequested && busy,
        onRefresh = {
            refreshRequested = true
            viewModel.refresh()
        },
        onRetry = viewModel::loadDashboard,
        onSelectChild = viewModel::selectChild,
        onOpenChild = { onNavigateToChildDetail(it.uuid) },
        onOpenBilling = onNavigateToBilling,
        onOpenFeed = { onNavigateToActivityFeed?.invoke(uiState.selectedChild?.uuid) },
        onOpenRoute = onNavigateRoute,
    )
}

/** The parent home without a ViewModel, for previews and screenshots. */
@Composable
fun ParentHomeContent(
    greeting: String,
    state: ParentUiState,
    nowMillis: Long,
    shortcuts: List<ParentShortcut>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSelectChild: (ParentChildItem) -> Unit,
    onOpenChild: (ParentChildItem) -> Unit,
    onOpenBilling: () -> Unit,
    onOpenFeed: () -> Unit,
    onOpenRoute: (String) -> Unit,
) {
    val child = state.selectedChild
    ShellTheme {
        Surface(color = SistaTheme.colors.background) {
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("parent_home_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    item(key = "header") {
                        GreetingHeader(
                            greeting = greeting,
                            name = state.parentName.ifBlank { stringResource(R.string.parent_default_name) },
                            details = listOf(
                                when (state.children.size) {
                                    0 -> stringResource(R.string.parent_role_none)
                                    1 -> stringResource(R.string.parent_role_one, state.children.first().name)
                                    else -> stringResource(R.string.parent_role_many, state.children.size)
                                },
                            ),
                            unreadCount = state.unreadNotifications,
                            onOpenNotifications = { onOpenRoute(Screen.NotificationCenter.route) },
                        )
                    }
                    when {
                        state.isLoading && state.children.isEmpty() -> item(key = "loading") { SkeletonList(rows = 4) }
                        state.errorMessage != null && state.children.isEmpty() -> item(key = "error") {
                            ErrorState(
                                title = stringResource(R.string.parent_children_error),
                                body = state.errorMessage,
                                onRetry = onRetry,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                        state.children.isEmpty() -> item(key = "no_children") {
                            EmptyState(
                                title = stringResource(R.string.parent_no_children),
                                body = stringResource(R.string.parent_no_children_body),
                                icon = Icons.Outlined.FamilyRestroom,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    if (state.children.size > 1 && child != null) {
                        item(key = "children") {
                            FilterChipRow(
                                options = state.children,
                                selected = child,
                                onSelect = onSelectChild,
                                label = { it.name.substringBefore(' ') + (it.classroom?.let { c -> " · $c" } ?: "") },
                            )
                        }
                    }
                    state.liveGateStatus?.let { gate ->
                        item(key = "gate") {
                            InlineBanner(
                                title = stringResource(R.string.parent_gate_title, gate.studentName, gate.gate),
                                message = stringResource(R.string.parent_gate_body, gate.status, gate.checkInTime),
                                tone = StatusTone.Success,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    if (child != null) {
                        if (state.childErrorMessage != null) {
                            item(key = "child_error") {
                                InlineBanner(
                                    message = stringResource(R.string.parent_partial_error, state.childErrorMessage),
                                    tone = StatusTone.Warning,
                                    actionLabel = stringResource(CoreUiR.string.core_reload),
                                    onAction = onRefresh,
                                    modifier = Modifier.padding(horizontal = Spacing.screen),
                                )
                            }
                        }
                        item(key = "child") { ChildCard(child, state, onClick = { onOpenChild(child) }) }
                        item(key = "stats") { ChildStats(state, onOpenBilling) }
                        item(key = "digest") { WeekDigest(state.weeklyDigest, state.isLoadingExperience) }
                        item(key = "feed", contentType = "feed") { RecentActivity(state, nowMillis, onOpenFeed) }
                        if (state.classComparison.isNotEmpty()) {
                            item(key = "comparison") { ClassComparison(state.classComparison) }
                        }
                        if (shortcuts.isNotEmpty()) {
                            item(key = "shortcuts") { Shortcuts(shortcuts, onOpenRoute) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildCard(child: ParentChildItem, state: ParentUiState, onClick: () -> Unit) {
    val homeroom = state.selectedChildSummary?.student?.homeroomTeacher?.takeIf { it.isNotBlank() }
    SistaCard(modifier = Modifier.padding(horizontal = Spacing.screen).fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = child.name)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(child.name, style = SistaTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(
                        child.classroom?.takeIf { it.isNotBlank() && it != "N/A" }?.let { stringResource(R.string.parent_detail_class, it) },
                        child.nis?.let { stringResource(R.string.parent_detail_nis, it) },
                    ).joinToString(" · ") { "\u2068$it\u2069" },
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
                if (homeroom != null) {
                    Text(stringResource(R.string.parent_homeroom, homeroom), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                }
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = stringResource(R.string.parent_view_detail), tint = SistaTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChildStats(state: ParentUiState, onOpenBilling: () -> Unit) {
    val stats = state.selectedChildSummary?.statistics
    // "–" until the summary arrives or when it failed: never a 0 that looks like a fact.
    val unpaid = stats?.unpaidBillingsCount
    val points = stats?.totalBkPoints
    val locale = parentLocale()
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile(
                label = stringResource(R.string.stat_attendance),
                value = stats?.attendanceRate?.let { "${decimal(it, locale)}%" } ?: "–",
                supporting = stringResource(if (stats != null && stats.attendanceRate == null) R.string.stat_attendance_none else R.string.stat_this_year),
                icon = Icons.Outlined.EventAvailable,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatTile(
                label = stringResource(R.string.stat_average),
                value = stats?.averageGrade?.let { decimal(it, locale) } ?: "–",
                supporting = stringResource(if (stats != null && stats.averageGrade == null) R.string.stat_average_none else R.string.stat_all_subjects),
                icon = Icons.Outlined.Grade,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile(
                label = stringResource(R.string.stat_points),
                value = points?.toString() ?: "–",
                supporting = stringResource(
                    when {
                        points == null -> R.string.stat_no_data
                        points == 0 -> R.string.stat_no_record
                        else -> R.string.stat_from_rules
                    },
                ),
                icon = Icons.Outlined.Gavel,
                tone = if ((points ?: 0) > 0) StatusTone.Warning else StatusTone.Neutral,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatTile(
                label = stringResource(R.string.stat_bills),
                value = when {
                    unpaid == null -> "–"
                    unpaid == 0 -> stringResource(R.string.stat_paid)
                    else -> "$unpaid"
                },
                supporting = stringResource(
                    when {
                        unpaid == null -> R.string.stat_no_data
                        unpaid == 0 -> R.string.stat_no_bills
                        else -> R.string.stat_unpaid
                    },
                ),
                icon = Icons.Outlined.AccountBalanceWallet,
                tone = if ((unpaid ?: 0) > 0) StatusTone.Danger else StatusTone.Neutral,
                onClick = onOpenBilling,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun WeekDigest(digest: WeeklyDigest?, loading: Boolean) {
    val locale = parentLocale()
    Column(Modifier.padding(horizontal = Spacing.screen)) {
        SectionHeader(stringResource(R.string.digest_title))
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row {
                    DigestNumber(stringResource(R.string.digest_present), digest?.attendancePercentage?.let { "${decimal(it, locale)}%" }, Modifier.weight(1f))
                    DigestNumber(stringResource(R.string.digest_new_grades), digest?.averageGrade?.let { decimal(it, locale) }, Modifier.weight(1f))
                    DigestNumber(stringResource(R.string.digest_ibadah), digest?.ibadahScore?.toString(), Modifier.weight(1f))
                }
                HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                val highlights = digest?.highlights.orEmpty()
                when {
                    loading && digest == null -> Text(stringResource(CoreUiR.string.core_loading), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                    highlights.isEmpty() -> Text(
                        stringResource(R.string.digest_empty),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                    else -> Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        highlights.forEach { Text("•  $it", style = SistaTheme.typography.bodyMedium) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DigestNumber(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value ?: "–", style = SistaTheme.typography.titleLarge)
        Text(label, style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun RecentActivity(state: ParentUiState, nowMillis: Long, onOpenFeed: () -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.screen)) {
        SectionHeader(
            stringResource(R.string.recent_title),
            actionLabel = if (state.activityFeed.size > 3) stringResource(R.string.recent_all) else null,
            onAction = onOpenFeed,
        )
        when {
            state.isLoadingExperience && state.activityFeed.isEmpty() -> SkeletonList(rows = 3)
            state.activityFeed.isEmpty() -> Text(
                stringResource(R.string.recent_empty),
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
            )
            else -> SistaCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    state.activityFeed.take(3).forEachIndexed { index, event ->
                        if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                        ActivityRow(event, nowMillis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassComparison(rows: List<ChildVsClassComparison>) {
    val locale = parentLocale()
    Column(Modifier.padding(horizontal = Spacing.screen)) {
        SectionHeader(stringResource(R.string.comparison_title))
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                rows.forEachIndexed { index, row ->
                    if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                    Row(Modifier.padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(row.subject, style = SistaTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                stringResource(R.string.comparison_class, decimal(row.classAverage, locale)),
                                style = SistaTheme.typography.bodySmall,
                                color = SistaTheme.colors.onSurfaceVariant,
                            )
                        }
                        Text(decimal(row.childScore, locale), style = SistaTheme.typography.titleMedium)
                        row.classAverage?.let { average ->
                            val diff = row.childScore - average
                            Spacer(Modifier.width(Spacing.sm))
                            StatusPill(
                                // Isolated left-to-right so the sign stays in front in Arabic.
                                "\u2066" + (if (diff >= 0) "+" else "−") + decimal(kotlin.math.abs(diff), locale) + "\u2069",
                                if (diff >= 0) StatusTone.Success else StatusTone.Warning,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Shortcuts(shortcuts: List<ParentShortcut>, onOpenRoute: (String) -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(stringResource(R.string.parent_quick_access), actionLabel = stringResource(R.string.parent_all_services), onAction = { onOpenRoute(Screen.ServicesHub.route) })
        shortcuts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                row.forEach { item -> FeatureTile(stringResource(item.title), item.icon, onClick = { onOpenRoute(item.route) }, modifier = Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
