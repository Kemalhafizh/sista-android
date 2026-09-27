package com.sultanagung1.sista.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.LocalAppStrings
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.ui.common.SyncStatusHeader
import com.sultanagung1.sista.ui.home.sections.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * FASE 60.1: Contextual Smart Hub (Elegant Minimalism).
 * Screen decomposition with Progressive Disclosure via HomeServicesBottomSheet.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGeofence: () -> Unit,
    onNavigateToDynamicQr: () -> Unit,
    onNavigateToCbt: () -> Unit,
    onNavigateToAiTutor: () -> Unit,
    onNavigateToMutabaah: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToAnnouncements: () -> Unit = {},
    onNavigateRoute: ((String) -> Unit)? = null,
    syncManager: com.sultanagung1.sista.core.sync.SyncManager? = null,
    onNavigateToGamification: (() -> Unit)? = null,
    onNavigateToClassSessionScan: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val haptics = rememberHapticFeedbackHelper()

    val isDark = MaterialTheme.colorScheme.surface.isDark()

    val timeGreeting = remember {
        val hour = DateUtils.nowCalendar().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 3..10 -> "Selamat Pagi ☀️"
            in 11..14 -> "Selamat Siang 🌤️"
            in 15..17 -> "Selamat Sore 🌅"
            else -> "Selamat Malam 🌙"
        }
    }

    // Pull-to-refresh follows the real reload (it used to spin a fixed 750 ms
    // and never reloaded this screen's own data).
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) refreshRequested = false }
    var showServicesBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val heroScrolledOff by rememberIsItemScrolledOff(listState, "hero_section")

    // FASE 77.5.3: poll for a running class only while Home is on screen.
    LifecycleStartStopEffect(
        onStart = viewModel::startActiveClassPolling,
        onStop = viewModel::stopActiveClassPolling
    )

    SulaonePullToRefreshBox(
        isRefreshing = refreshRequested && uiState.isLoading,
        onRefresh = {
            refreshRequested = true
            coroutineScope.launch { syncManager?.performFullSync() }
            viewModel.loadHomeData()
        },
        // FASE 74.1: landing marker so an E2E driver can assert it reached
        // HomeScreen after login, instead of guessing off transient text
        // like the time-of-day greeting.
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_root")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
        ) {
            // 0. Sync Status Header (Offline/Syncing)
            if (syncManager != null) {
                item(key = "sync_header", contentType = "sync") {
                    SyncStatusHeader(syncManager = syncManager)
                }
            }

            // 1. Minimalist Glassmorphic Hero Header (Sapaan, Avatar, WCAG Actions)
            item(key = "hero_section", contentType = "hero") {
                HomeHeroSection(
                    uiState = uiState,
                    isDark = isDark,
                    timeGreeting = timeGreeting,
                    onNavigateToDynamicQr = onNavigateToDynamicQr,
                    onNavigateToAnnouncements = onNavigateToAnnouncements,
                    onNavigateRoute = onNavigateRoute
                )
            }

            // 1.4. A class of this student is running: scan the teacher's QR (FASE 77.5.3)
            uiState.activeClassSession?.let { active ->
                item(key = "active_class_session", contentType = "active_class") {
                    HomeActiveClassBanner(
                        session = active,
                        onScan = onNavigateToClassSessionScan,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // 1.5. Next-Class Countdown (FASE 71.1, server-time corrected)
            if (uiState.todaySchedules.isNotEmpty()) {
                item(key = "next_class_countdown", contentType = "countdown") {
                    HomeNextClassCountdown(todaySchedules = uiState.todaySchedules)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // 2. Compact Prayer Context Card (Hijri Date & 5-Prayer Bar)
            item(key = "prayer_widget", contentType = "prayer") {
                HomePrayerWidget(
                    prayerSchedule = uiState.prayerSchedule
                )
            }

            // 3. Focused 5-Pill Quick Actions (4 Core + 1 "Semua Layanan")
            item(key = "quick_actions", contentType = "actions") {
                HomeMinimalQuickActions(
                    isDark = isDark,
                    onNavigateToGeofence = onNavigateToGeofence,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onNavigateToCbt = onNavigateToCbt,
                    onNavigateToBilling = onNavigateToBilling,
                    onOpenAllServices = { showServicesBottomSheet = true },
                    usage = uiState.featureUsage,
                    onActionUsed = viewModel::recordFeatureUse
                )
            }

            // 4. Contextual Amalan Yaumiyah & Smart Suggestions
            uiState.contextualPayload?.let { ctx ->
                item(key = "contextual_section", contentType = "contextual") {
                    HomeContextualSection(
                        contextualPayload = ctx,
                        onNavigateToGamification = onNavigateToGamification,
                        onNavigateToMutabaah = onNavigateToMutabaah,
                        onNavigateRoute = onNavigateRoute
                    )
                }
            } ?: item(key = "streak_banner", contentType = "streak") {
                HomeStreakBanner(
                    onNavigateToMutabaah = onNavigateToMutabaah
                )
            }

            // 5. Today's Class Schedule Timeline
            item(key = "schedule_preview", contentType = "schedule") {
                HomeSchedulePreview(
                    todaySchedules = uiState.todaySchedules,
                    onNavigateToSchedule = onNavigateToSchedule,
                    isLoading = uiState.isLoading,
                    loadError = uiState.errorMessage,
                    onRetry = viewModel::loadHomeData
                )
            }

            // Bottom space for breathing room above navigation bar
            item(key = "bottom_spacer", contentType = "spacer") {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // FASE 76.2: sticky glass bar once the hero greeting scrolls away.
        // Only visible when scrolled down, so it never competes with the
        // pull-to-refresh indicator (which only appears at the very top).
        SulaoneGlassTopBar(
            visible = heroScrolledOff,
            title = uiState.userName,
            subtitle = timeGreeting,
            onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } }
        )
    }

    // Progressive Disclosure: Applet Modal BottomSheet for All Services
    HomeServicesBottomSheet(
        isVisible = showServicesBottomSheet,
        onDismiss = { showServicesBottomSheet = false },
        onNavigateRoute = onNavigateRoute,
        usage = uiState.featureUsage,
        onServiceUsed = viewModel::recordFeatureUse,
        onResetUsage = viewModel::resetFeatureUsage
    )
}
