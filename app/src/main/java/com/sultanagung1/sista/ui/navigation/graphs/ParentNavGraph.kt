package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.analytics.AnalyticsViewModel
import com.sultanagung1.sista.ui.analytics.ChildProgressScreen
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
    guardedComposable(Screen.ParentDashboard.route) {
        val viewModel: ParentViewModel = hiltViewModel()
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

    guardedComposable(
        route = Screen.ChildDetail.route,
        arguments = listOf(navArgument("studentId") { type = NavType.StringType })
    ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getString("studentId") ?: "c1"
        val viewModel: ParentViewModel = hiltViewModel()
        ChildDetailScreen(
            studentId = studentId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.ChildProgress.route) {
        val viewModel: AnalyticsViewModel = hiltViewModel()
        ChildProgressScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.ChildActivityFeed.route) {
        val viewModel: ParentViewModel = hiltViewModel()
        ChildActivityFeedScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
