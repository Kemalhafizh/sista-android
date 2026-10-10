package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.admin.AdminDashboardScreen
import com.sultanagung1.sista.ui.admin.AdminViewModel
import com.sultanagung1.sista.ui.admin.sessions.AdminAttendanceOverrideScreen
import com.sultanagung1.sista.ui.admin.sessions.AdminAttendanceOverrideViewModel
import com.sultanagung1.sista.ui.admin.sessions.AdminSessionManagementScreen
import com.sultanagung1.sista.ui.admin.sessions.AdminSessionManagementViewModel
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
    guardedComposable(Screen.AdminDashboard.route) {
        val viewModel: AdminViewModel = hiltViewModel()
        AdminDashboardScreen(
            viewModel = viewModel,
            onNavigateRoute = { route -> navController.navigate(route) }
        )
    }

    // ── FASE 77.6: Sesi Kelas — admin, superadmin, Waka Kurikulum, TU ──

    guardedComposable(Screen.AdminSessionManagement.route) {
        val viewModel: AdminSessionManagementViewModel = hiltViewModel()
        AdminSessionManagementScreen(
            viewModel = viewModel,
            onOpenSession = { sessionId ->
                navController.navigate(Screen.AdminAttendanceOverride.createRoute(sessionId)) { launchSingleTop = true }
            },
            // A tab for admin/Waka/TU: no back arrow when it is the root.
            onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null
        )
    }

    guardedComposable(
        route = Screen.AdminAttendanceOverride.route,
        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
    ) {
        val viewModel: AdminAttendanceOverrideViewModel = hiltViewModel()
        AdminAttendanceOverrideScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
