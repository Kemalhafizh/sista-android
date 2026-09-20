package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sultanagung1.sista.ui.admin.AdminDashboardScreen
import com.sultanagung1.sista.ui.admin.AdminViewModel
import com.sultanagung1.sista.ui.analytics.AnalyticsViewModel
import com.sultanagung1.sista.ui.analytics.ExecutiveAnalyticsScreen
import com.sultanagung1.sista.ui.common.RoleGuardedScreen
import com.sultanagung1.sista.ui.navigation.Screen

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
                onNavigateToPdf = { navController.navigate(Screen.PdfViewer.route) }
            )
        }
    }
}
