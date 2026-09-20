package com.sultanagung1.sista.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.sultanagung1.sista.core.update.InAppUpdateManager
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.data.local.SulaoneLocalStore
import com.sultanagung1.sista.ui.auth.LoginViewModel
import com.sultanagung1.sista.ui.navigation.graphs.*

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

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
    val userRoleRaw by sessionManager.userRoleFlow.collectAsState(initial = "student")
    val userRole = userRoleRaw ?: "student"
    val isSensitiveProtectionEnabled by sessionManager.isSensitiveProtectionEnabledFlow.collectAsState(initial = true)

    val navigateToRoleHome = remember(userRole) {
        {
            val targetRoute = when {
                userRole.contains("teacher", ignoreCase = true) || userRole.contains("guru", ignoreCase = true) -> Screen.TeacherDashboard.route
                userRole.contains("parent", ignoreCase = true) || userRole.contains("ortu", ignoreCase = true) -> Screen.ParentDashboard.route
                userRole.contains("admin", ignoreCase = true) || userRole.contains("kepsek", ignoreCase = true) -> Screen.AdminDashboard.route
                else -> Screen.Home.route
            }
            navController.navigate(targetRoute) {
                popUpTo(0) { inclusive = false }
            }
        }
    }

    // Real-Time & Offline Sync Services
    val effectiveApiClient = remember(apiClient, context) { apiClient ?: ApiClient(context) }
    val localStore = remember { SulaoneLocalStore.getInstance(context) }
    val actionQueue = remember { OfflineActionQueue(localStore, effectiveApiClient) }
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val syncManager = remember { SyncManager(context, effectiveApiClient, localStore, actionQueue, connectivityObserver) }
    val lifecycleSyncObserver = remember { AppLifecycleSyncObserver(syncManager) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleSyncObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleSyncObserver)
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
    val inAppUpdateManager = remember { InAppUpdateManager(context) }
    val liteModeManager = remember { LiteModeManager(context) }

    // Role-Based Multilingual Bottom Navigation Bar Items (FASE 60.3: Consolidated 4-Tab System)
    val bottomNavItems = when {
        userRole.contains("teacher", ignoreCase = true) || userRole.contains("guru", ignoreCase = true) -> listOf(
            BottomNavItem(Screen.TeacherDashboard.route, strings.teacherTab, Icons.Default.Dashboard),
            BottomNavItem(Screen.Schedule.route, strings.scheduleTab, Icons.Default.School),
            BottomNavItem(Screen.NotificationCenter.route, strings.notificationsTab, Icons.Default.Campaign),
            BottomNavItem(Screen.Profile.route, strings.profileTab, Icons.Default.Person)
        )
        userRole.contains("parent", ignoreCase = true) || userRole.contains("ortu", ignoreCase = true) -> listOf(
            BottomNavItem(Screen.ParentDashboard.route, strings.parentTab, Icons.Default.FamilyRestroom),
            BottomNavItem(Screen.Grades.route, strings.gradesTab, Icons.Default.AutoGraph),
            BottomNavItem(Screen.NotificationCenter.route, strings.notificationsTab, Icons.Default.Campaign),
            BottomNavItem(Screen.Profile.route, strings.profileTab, Icons.Default.Person)
        )
        userRole.contains("admin", ignoreCase = true) || userRole.contains("kepsek", ignoreCase = true) || userRole.contains("principal", ignoreCase = true) -> listOf(
            BottomNavItem(Screen.AdminDashboard.route, strings.executiveTab, Icons.Default.AdminPanelSettings),
            BottomNavItem(Screen.Schedule.route, strings.scheduleTab, Icons.Default.School),
            BottomNavItem(Screen.NotificationCenter.route, strings.notificationsTab, Icons.Default.Campaign),
            BottomNavItem(Screen.Profile.route, strings.profileTab, Icons.Default.Person)
        )
        else -> listOf(
            BottomNavItem(Screen.Home.route, strings.homeTab, Icons.Default.Home),
            BottomNavItem(Screen.Schedule.route, strings.scheduleTab, Icons.Default.School),
            BottomNavItem(Screen.NotificationCenter.route, strings.notificationsTab, Icons.Default.Campaign),
            BottomNavItem(Screen.Profile.route, strings.profileTab, Icons.Default.Person)
        )
    }

    val tabRoutes = remember(bottomNavItems) { bottomNavItems.map { it.route }.toSet() }
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
                val isDark = MaterialTheme.colorScheme.surface.let { (0.299f * it.red + 0.587f * it.green + 0.114f * it.blue) < 0.5f }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.extendedColors.borderSubtle.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isDark) 8.dp else 4.dp,
                                    shape = RoundedCornerShape(22.dp),
                                    spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ),
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                1.dp,
                                if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                else MaterialTheme.extendedColors.borderSubtle.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentRoute == item.route
                                    val activeColor = MaterialTheme.colorScheme.primary
                                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                if (currentRoute != item.route) {
                                                    haptics.tapLight()
                                                    onNavigateTab(item.route)
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (isSelected) activeColor.copy(alpha = 0.14f) else Color.Transparent
                                                )
                                                .padding(horizontal = 14.dp, vertical = 4.dp),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = if (isSelected) activeColor else inactiveColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                            ),
                                            color = if (isSelected) activeColor else inactiveColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            if (showNavRail) {
                SulaoneNavigationRail(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onNavigate = onNavigateTab
                )
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
            } else if (showNavDrawer) {
                SulaonePermanentNavDrawer(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onNavigate = onNavigateTab
                )
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
                        LocalWindowWidthSizeClass provides windowWidthClass
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
                        userRole = userRole,
                        syncManager = syncManager
                    )

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


