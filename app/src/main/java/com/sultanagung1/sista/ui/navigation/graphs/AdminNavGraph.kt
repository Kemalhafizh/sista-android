package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.admin.AdminDashboardScreen
import com.sultanagung1.sista.ui.admin.AdminViewModel
import com.sultanagung1.sista.ui.admin.sessions.AdminAttendanceOverrideScreen
import com.sultanagung1.sista.ui.admin.sessions.AdminAttendanceOverrideViewModel
import com.sultanagung1.sista.ui.admin.sessions.AdminSessionManagementScreen
import com.sultanagung1.sista.ui.admin.sessions.AdminSessionManagementViewModel
import com.sultanagung1.sista.ui.analytics.AnalyticsViewModel
import com.sultanagung1.sista.ui.analytics.ExecutiveAnalyticsScreen
import com.sultanagung1.sista.ui.common.RoleGuardedScreen
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.UserRoles

/**
 * Sub-Navigation Graph untuk Dashboard Pimpinan & Admin Eksekutif (FASE 53.2).
 */
fun NavGraphBuilder.adminNavGraph(
    navController: NavHostController,
    userRole: String,
    navigateToRoleHome: () -> Unit
) {
    composable(Screen.AdminDashboard.route) {
        val viewModel: AdminViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("admin", "superadmin", "principal", "kepsek"),
            featureTitle = "Command Center Pimpinan Sekolah",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            AdminDashboardScreen(
                viewModel = viewModel,
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
    }

    composable(Screen.ExecutiveAnalytics.route) {
        val viewModel: AnalyticsViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("admin", "superadmin", "principal", "kepsek"),
            featureTitle = "KPI Eksekutif & Statistik Kampus",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            ExecutiveAnalyticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                // No backend endpoint generates an executive-summary PDF yet — PdfViewerScreen
                // shows an honest "not available" state rather than a fabricated document.
                onNavigateToPdf = { navController.navigate(Screen.PdfViewer.createRoute("", "Ringkasan Eksekutif", 0L)) }
            )
        }
    }

    // ── FASE 77.6: Sesi Kelas — admin, superadmin, Waka Kurikulum, TU ──

    composable(Screen.AdminSessionManagement.route) {
        val viewModel: AdminSessionManagementViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = UserRoles.CLASS_SESSION_ADMIN_ROLES,
            featureTitle = "Manajemen Sesi Kelas",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            AdminSessionManagementScreen(
                viewModel = viewModel,
                onOpenSession = { sessionId ->
                    navController.navigate(Screen.AdminAttendanceOverride.createRoute(sessionId)) { launchSingleTop = true }
                },
                // A tab for admin/Waka/TU: no back arrow when it is the root.
                onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null
            )
        }
    }

    composable(
        route = Screen.AdminAttendanceOverride.route,
        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
    ) {
        val viewModel: AdminAttendanceOverrideViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = UserRoles.CLASS_SESSION_ADMIN_ROLES,
            featureTitle = "Koreksi Absensi Sesi Kelas",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            AdminAttendanceOverrideScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
