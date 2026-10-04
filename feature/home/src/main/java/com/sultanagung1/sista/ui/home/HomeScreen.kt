package com.sultanagung1.sista.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.sync.SyncManager
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.feature.home.R
import com.sultanagung1.sista.core.ui.R as CoreUiR
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.ui.common.SyncStatusHeader
import com.sultanagung1.sista.ui.home.sections.HomeHeader
import com.sultanagung1.sista.ui.home.sections.HomeQuickAccess
import com.sultanagung1.sista.ui.home.sections.HomeTodayCard
import com.sultanagung1.sista.ui.home.sections.QuickItem
import com.sultanagung1.sista.ui.home.sections.STUDENT_QUICK_ITEMS
import com.sultanagung1.sista.core.ui.component.greetingFor
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * The student's Beranda: who is signed in, a class running right now,
 * today's lessons and the shortcuts this account may use. Everything shown
 * comes from the server; shortcuts the account does not have never appear.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateRoute: (String) -> Unit,
    syncManager: SyncManager? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val capabilities = LocalCapabilityState.current
    val coroutineScope = rememberCoroutineScope()

    // FASE 77.5.3: poll for a running class only while Home is on screen.
    LifecycleStartStopEffect(
        onStart = viewModel::startActiveClassPolling,
        onStop = viewModel::stopActiveClassPolling,
    )

    // Pull-to-refresh follows the real reload and stops when the server answers.
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) refreshRequested = false }

    val now = remember(uiState.todaySchedules, uiState.isLoading) { DateUtils.nowCalendar() }
    val record = viewModel::recordFeatureUse

    HomeContent(
        greeting = greetingFor(now.get(Calendar.HOUR_OF_DAY)),
        name = uiState.userName.orEmpty(),
        classroom = uiState.studentClass,
        identifier = uiState.userIdentifier,
        unreadCount = uiState.unreadNotificationsCount,
        activeClass = uiState.activeClassSession,
        todaySchedules = uiState.todaySchedules,
        nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        quickItems = STUDENT_QUICK_ITEMS.filter { capabilities.canOpen(it.route) },
        usage = uiState.featureUsage,
        canScanClassQr = capabilities.canOpen(Screen.StudentSessionQrScan.route),
        refreshing = refreshRequested && uiState.isLoading,
        onRefresh = {
            refreshRequested = true
            coroutineScope.launch { syncManager?.performFullSync() }
            viewModel.loadHomeData()
        },
        onOpenRoute = onNavigateRoute,
        onOpenQuickItem = { item ->
            record(item.key)
            onNavigateRoute(item.route)
        },
        onResetUsage = viewModel::resetFeatureUsage,
        syncHeader = syncManager?.let { manager -> { SyncStatusHeader(syncManager = manager) } },
    )
}

/** Beranda without state of its own, so it can be previewed and screenshot-tested. */
@Composable
fun HomeContent(
    greeting: String,
    name: String,
    classroom: String?,
    identifier: String?,
    unreadCount: Int,
    activeClass: ActiveClassSessionDto?,
    todaySchedules: List<ScheduleItem>,
    nowMinutes: Int,
    isLoading: Boolean,
    errorMessage: String?,
    quickItems: List<QuickItem>,
    usage: Map<String, Int>,
    canScanClassQr: Boolean,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenRoute: (String) -> Unit,
    onOpenQuickItem: (QuickItem) -> Unit,
    onResetUsage: () -> Unit,
    syncHeader: (@Composable () -> Unit)? = null,
) {
    ShellTheme {
        Surface(color = SistaTheme.colors.background) {
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                // FASE 74.1: landing marker for E2E drivers.
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_screen_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    if (syncHeader != null) item(key = "sync_header") { syncHeader() }
                    item(key = "header") {
                        HomeHeader(
                            greeting = greeting,
                            name = name,
                            classroom = classroom,
                            identifier = identifier,
                            unreadCount = unreadCount,
                            onOpenNotifications = { onOpenRoute(Screen.NotificationCenter.route) },
                        )
                    }
                    if (activeClass != null) {
                        item(key = "active_class") { ActiveClassBanner(activeClass, canScanClassQr) { onOpenRoute(Screen.StudentSessionQrScan.route) } }
                    }
                    if (errorMessage != null && todaySchedules.isNotEmpty()) {
                        item(key = "stale") {
                            InlineBanner(
                                message = stringResource(R.string.home_stale_data, errorMessage),
                                tone = StatusTone.Warning,
                                actionLabel = stringResource(CoreUiR.string.core_reload),
                                onAction = onRefresh,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    item(key = "today") {
                        HomeTodayCard(
                            todaySchedules = todaySchedules,
                            nowMinutes = nowMinutes,
                            isLoading = isLoading,
                            loadError = errorMessage,
                            onRetry = onRefresh,
                            onOpenSchedule = { onOpenRoute(Screen.Schedule.route) },
                        )
                    }
                    item(key = "quick_actions") {
                        HomeQuickAccess(
                            available = quickItems,
                            usage = usage,
                            onOpen = onOpenQuickItem,
                            onOpenAllServices = { onOpenRoute(Screen.ServicesHub.route) },
                            onResetUsage = onResetUsage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveClassBanner(session: ActiveClassSessionDto, canScan: Boolean, onScan: () -> Unit) {
    val subject = session.subjectName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.home_class_fallback)
    val title = stringResource(R.string.home_class_ongoing, subject)
    if (session.alreadyCheckedIn) {
        InlineBanner(
            title = title,
            message = stringResource(R.string.home_class_checked_in),
            tone = StatusTone.Success,
            modifier = Modifier.padding(horizontal = Spacing.screen),
        )
    } else {
        InlineBanner(
            title = title,
            message = stringResource(R.string.home_class_scan_hint),
            tone = StatusTone.Info,
            actionLabel = if (canScan) stringResource(R.string.home_scan_qr) else null,
            onAction = if (canScan) onScan else null,
            modifier = Modifier
                .padding(horizontal = Spacing.screen)
                .testTag("active_class_banner"),
        )
    }
}
