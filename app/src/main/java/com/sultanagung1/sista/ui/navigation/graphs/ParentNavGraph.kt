package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.analytics.AnalyticsViewModel
import com.sultanagung1.sista.ui.analytics.ChildProgressScreen
import com.sultanagung1.sista.ui.common.RoleGuardedScreen
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.parent.*

/**
 * Sub-Navigation Graph untuk Portal Wali Murid (FASE 53.2).
 */
fun NavGraphBuilder.parentNavGraph(
    navController: NavHostController,
    userRole: String,
    navigateToRoleHome: () -> Unit
) {
    composable(Screen.ParentDashboard.route) {
        val viewModel: ParentViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("parent", "ortu", "admin", "superadmin"),
            featureTitle = "Portal Wali Murid",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            ParentDashboardScreen(
                viewModel = viewModel,
                onNavigateToChildDetail = { studentId ->
                    navController.navigate(Screen.ChildDetail.createRoute(studentId))
                },
                onNavigateToBilling = { navController.navigate(Screen.Billing.route) },
                onNavigateRoute = { route -> navController.navigate(route) },
                onNavigateToActivityFeed = { navController.navigate(Screen.ChildActivityFeed.route) }
            )
        }
    }

    composable(
        route = Screen.ChildDetail.route,
        arguments = listOf(navArgument("studentId") { type = NavType.StringType })
    ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getString("studentId") ?: "c1"
        val viewModel: ParentViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("parent", "ortu", "admin", "superadmin", "teacher", "guru"),
            featureTitle = "Detail Perkembangan Anak",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            ChildDetailScreen(
                studentId = studentId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.ChildProgress.route) {
        val viewModel: AnalyticsViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("parent", "ortu", "admin", "superadmin", "teacher", "guru"),
            featureTitle = "Pantau Progres Anak",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            ChildProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.ChildActivityFeed.route) {
        val viewModel: ParentViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("parent", "ortu", "admin", "superadmin", "teacher", "guru"),
            featureTitle = "Aktivitas Harian Siswa",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            ChildActivityFeedScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
