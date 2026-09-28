package com.sultanagung1.sista.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sultanagung1.sista.core.accessibility.FontScaleManager
import com.sultanagung1.sista.core.accessibility.LanguageManager
import com.sultanagung1.sista.core.accessibility.LocalAppStrings
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.document.DownloadManager
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.lite.LiteModeManager
import com.sultanagung1.sista.core.motion.LocalSharedTransitionScope
import com.sultanagung1.sista.core.motion.SulaoneNavTransitions
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.notification.NotificationChannelManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.sync.AppLifecycleSyncObserver
import com.sultanagung1.sista.core.sync.NetworkConnectivityObserver
import com.sultanagung1.sista.core.sync.OfflineActionQueue
import com.sultanagung1.sista.core.sync.SyncManager
import com.sultanagung1.sista.core.time.ServerTimeProvider
import com.sultanagung1.sista.core.update.InAppUpdateManager
import com.sultanagung1.sista.core.websocket.WebSocketSessionViewModel
import com.sultanagung1.sista.data.local.SulaoneLocalStore
import com.sultanagung1.sista.ui.auth.LoginViewModel
import com.sultanagung1.sista.ui.navigation.graphs.*
import com.sultanagung1.sista.ui.shell.ServicesHubScreen
import com.sultanagung1.sista.ui.shell.ShellTheme
import com.sultanagung1.sista.core.ui.component.SistaNavigationBar
import com.sultanagung1.sista.core.ui.component.SistaNavigationRail
import com.sultanagung1.sista.data.model.CapabilityState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

/**
 * Master NavHost & Adaptive Navigation Bar (FASE 53.2).
 * Didekomposisi menjadi 8 sub-graphs modular:
 * 1. AuthNavGraph
 * 2. AcademicNavGraph
 * 3. CbtNavGraph
 * 4. TeacherNavGraph
 * 5. ParentNavGraph
 * 6. AdminNavGraph
 * 7. CommunicationNavGraph
 * 8. SettingsNavGraph
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    apiClient: ApiClient? = null,
    sessionManager: SessionManager,
    languageManager: LanguageManager,
    fontScaleManager: FontScaleManager,
    themeManager: ThemeManager,
    initialDeepLinkRoute: String? = null
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn by sessionManager.isLoggedInFlow.collectAsState(initial = false)
    // Only for screens that still read a role for wording; what an account may
    // open comes from the server's capability list below, never from this.
    val userRole = sessionManager.userRoleFlow.collectAsState(initial = null).value.orEmpty()
    val isSensitiveProtectionEnabled by sessionManager.isSensitiveProtectionEnabledFlow.collectAsState(initial = true)

    // What this account may use, from GET me/capabilities (server = truth).
    val capabilitiesViewModel: CapabilitiesViewModel = hiltViewModel()
    val capabilityState by capabilitiesViewModel.state.collectAsState()
    val homeRoute = (capabilityState as? CapabilityState.Ready)
        ?.let { FeatureCatalog.homeRouteFor(it.capabilities) }
        ?: Screen.ServicesHub.route
    val coroutineScope = rememberCoroutineScope()

    val navigateToRoleHome: () -> Unit = {
        navController.navigate(homeRoute) {
            popUpTo(0) { inclusive = false }
            launchSingleTop = true
        }
    }

    // Signed in: fetch the account's features first, then open its home.
    val onSignedIn: () -> Unit = {
        coroutineScope.launch {
            val state = capabilitiesViewModel.refreshNow()
            val target = (state as? CapabilityState.Ready)
                ?.let { FeatureCatalog.homeRouteFor(it.capabilities) }
                ?: Screen.ServicesHub.route
            navController.navigate(target) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Opens/closes the shared Reverb WebSocket connection as isLoggedIn changes
    // (FASE 71.3/71.4 real-time infra) — obtained via Hilt so it shares the same
    // ReverbWebSocketManager singleton that feature ViewModels listen to.
    val webSocketSessionViewModel: WebSocketSessionViewModel = hiltViewModel()
    val activeEmergencyAlert by webSocketSessionViewModel.emergencyAlert.collectAsState()
    activeEmergencyAlert?.let { alert ->
        com.sultanagung1.sista.ui.common.EmergencyAlertDialog(
            alert = alert,
            onDismiss = { webSocketSessionViewModel.dismissEmergencyAlert() }
        )
    }

    // Real-Time & Offline Sync Services
    val effectiveApiClient = remember(apiClient, context) { apiClient ?: ApiClient(context) }
    val localStore = remember { SulaoneLocalStore.getInstance(context) }
    val actionQueue = remember { OfflineActionQueue(localStore, effectiveApiClient) }
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val syncManager = remember { SyncManager(context, effectiveApiClient, localStore, actionQueue, connectivityObserver) }
    // FASE 71 anti-tamper clock: synced on app start and every resume-from-background
    // (onCatchUp hook below), so schedule "sedang berlangsung" status, countdowns, and
    // attendance windows never trust a device clock the user could have changed.
    val serverTimeProvider = remember { ServerTimeProvider(context, effectiveApiClient) }
    val lifecycleSyncObserver = remember {
        AppLifecycleSyncObserver(syncManager, onCatchUp = { serverTimeProvider.sync() })
    }

    LaunchedEffect(Unit) {
        serverTimeProvider.syncAsync()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val capabilityObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) capabilitiesViewModel.onForeground()
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleSyncObserver)
        lifecycleOwner.lifecycle.addObserver(capabilityObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleSyncObserver)
            lifecycleOwner.lifecycle.removeObserver(capabilityObserver)
        }
    }

    // Telemetry: Automatically record screen navigation breadcrumbs
    DisposableEffect(navController) {
        val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: "unknown"
            com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.logBreadcrumb(
                category = com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.NAVIGATION,
                message = "Navigated to route: $route",
                level = com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.INFO,
                data = mapOf("route" to route)
            )
            com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.setCustomKey("current_screen", route)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val notificationChannelManager = remember { NotificationChannelManager(context) }
    LaunchedEffect(Unit) {
        notificationChannelManager.createNotificationChannels()
        syncManager.performFullSync()
    }

    LaunchedEffect(initialDeepLinkRoute, isLoggedIn) {
        if (isLoggedIn && !initialDeepLinkRoute.isNullOrEmpty()) {
            navController.navigate(initialDeepLinkRoute) {
                launchSingleTop = true
            }
        }
    }

    val audioRecorderManager = remember { AudioRecorderManager(context) }
    val downloadManager = remember { DownloadManager(context) }
    val inAppUpdateManager = remember { InAppUpdateManager(context, com.sultanagung1.sista.core.network.ApiClient(context)) }
    val liteModeManager = remember { LiteModeManager(context) }

    // The same places for every account (FASE 60.3: Consolidated 4-Tab System):
    // Beranda opens the home of whatever the account may use, Layanan lists
    // every feature it has. Roles differ in content, never in the shell.
    val bottomNavItems = remember(homeRoute, capabilityState, strings) {
        ShellTabs.entries(homeRoute, capabilityState, strings)
    }

    val tabRoutes = remember(bottomNavItems) { bottomNavItems.map { it.key }.toSet() }
    val windowWidthClass = rememberCurrentWindowWidthSizeClass()
    val navType = windowWidthClass.toAdaptiveNavigationType()
    val isTabRoute = currentRoute in tabRoutes
    val showBottomBar = isTabRoute && navType == AdaptiveNavigationType.BOTTOM_NAVIGATION
    val showNavRail = isTabRoute && navType == AdaptiveNavigationType.NAVIGATION_RAIL
    val showNavDrawer = isTabRoute && navType == AdaptiveNavigationType.PERMANENT_NAVIGATION_DRAWER

    val onNavigateTab: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                val haptics = rememberHapticFeedbackHelper()
                ShellTheme {
                    SistaNavigationBar(
                        entries = bottomNavItems,
                        selectedKey = currentRoute,
                        onSelect = { entry ->
                            haptics.tapLight()
                            onNavigateTab(entry.key)
                        },
                    )
                }
            }
        },
        // FASE 76.3: one contextual action per role home (see ContextualFab).
        // Hidden everywhere else, including the whole CBT flow: the rule is
        // "only on a home tab", so an exam screen can never show it.
        floatingActionButton = {
            val fabAction = ContextualFab.actionFor(currentRoute, userRole)
                ?.takeIf { capabilityState.canOpen(it.targetRoute) }
            if (fabAction != null && isTabRoute) {
                val fabHaptics = rememberHapticFeedbackHelper()
                ExtendedFloatingActionButton(
                    onClick = {
                        fabHaptics.tapLight()
                        navController.navigate(fabAction.targetRoute) { launchSingleTop = true }
                    },
                    icon = { Icon(imageVector = fabAction.icon(), contentDescription = null) },
                    text = {
                        Text(
                            text = fabAction.label,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("contextual_fab")
                )
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            if (showNavRail || showNavDrawer) {
                ShellTheme {
                    SistaNavigationRail(
                        entries = bottomNavItems,
                        selectedKey = currentRoute,
                        onSelect = { entry -> onNavigateTab(entry.key) },
                    )
                }
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalWindowWidthSizeClass provides windowWidthClass,
                        LocalCapabilityState provides capabilityState,
                        LocalGateActions provides GateActions(
                            onBack = { if (!navController.popBackStack()) navigateToRoleHome() },
                            onHome = navigateToRoleHome,
                            onRetry = capabilitiesViewModel::refresh,
                        )
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Login.route,
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds(),
                            enterTransition = {
                        val isTabSwitch = initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes
                        if (isTabSwitch) {
                            SulaoneNavTransitions.tabEnterTransition(this)
                        } else {
                            SulaoneNavTransitions.enterTransition(this)
                        }
                    },
                    exitTransition = {
                        val isTabSwitch = initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes
                        if (isTabSwitch) {
                            SulaoneNavTransitions.tabExitTransition(this)
                        } else {
                            SulaoneNavTransitions.exitTransition(this)
                        }
                    },
                    popEnterTransition = SulaoneNavTransitions.predictiveBackPopEnterTransition,
                    popExitTransition = SulaoneNavTransitions.predictiveBackPopExitTransition
                ) {
                    // 1. Autentikasi & Home (FASE 53.2)
                    authNavGraph(
                        navController = navController,
                        onSignedIn = onSignedIn,
                        syncManager = syncManager
                    )

                    // Layanan: every feature of this account, same layout for all roles.
                    guardedComposable(Screen.ServicesHub.route) {
                        ServicesHubScreen(
                            state = capabilityState,
                            onOpen = { route -> navController.navigate(route) { launchSingleTop = true } },
                            onRetry = capabilitiesViewModel::refresh
                        )
                    }

                    // 2. Modul Akademik, LMS, Kalender & Analitik (FASE 53.2)
                    academicNavGraph(
                        navController = navController,
                        userRole = userRole,
                        navigateToRoleHome = navigateToRoleHome
                    )

                    // 3. Mesin Ujian CBT (FASE 53.2)
                    cbtNavGraph(
                        navController = navController,
                        isSensitiveProtectionEnabled = isSensitiveProtectionEnabled
                    )

                    // 4. Modul Guru & Pengajaran (FASE 53.2)
                    teacherNavGraph(
                        navController = navController,
                        userRole = userRole,
                        navigateToRoleHome = navigateToRoleHome
                    )

                    // 5. Portal Wali Murid (FASE 53.2)
                    parentNavGraph(
                        navController = navController,
                        userRole = userRole,
                        navigateToRoleHome = navigateToRoleHome
                    )

                    // 6. Command Center Admin / Pimpinan Eksekutif (FASE 53.2)
                    adminNavGraph(
                        navController = navController,
                        userRole = userRole,
                        navigateToRoleHome = navigateToRoleHome
                    )

                    // 7. Komunikasi, Chat Konsultasi & Pengumuman (FASE 53.2)
                    communicationNavGraph(
                        navController = navController
                    )

                    // 8. Pengaturan, Profil, Perangkat Keras & Kesiswaan (FASE 53.2)
                    settingsNavGraph(
                        navController = navController,
                        sessionManager = sessionManager,
                        themeManager = themeManager,
                        userRole = userRole,
                        navigateToRoleHome = navigateToRoleHome,
                        audioRecorderManager = audioRecorderManager,
                        downloadManager = downloadManager,
                        inAppUpdateManager = inAppUpdateManager,
                        liteModeManager = liteModeManager
                    )
                }
            }
        }
    }
}
}
}

private fun ContextualFabAction.icon(): ImageVector = when (this) {
    ContextualFabAction.QUICK_ATTENDANCE -> Icons.Default.LocationOn
    ContextualFabAction.TEACHING_JOURNAL -> Icons.Default.EditNote
    ContextualFabAction.MESSAGE_TEACHER -> Icons.AutoMirrored.Filled.Chat
}
