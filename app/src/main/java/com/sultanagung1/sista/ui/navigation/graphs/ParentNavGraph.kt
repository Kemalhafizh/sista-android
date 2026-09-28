package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.analytics.ChildProgressScreen
import com.sultanagung1.sista.ui.analytics.ChildProgressViewModel
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
            onNavigateToActivityFeed = { uuid -> navController.navigate(Screen.ChildActivityFeed.createRoute(uuid)) }
        )
    }

    guardedComposable(
        route = Screen.ChildDetail.route,
        arguments = listOf(navArgument("studentId") { type = NavType.StringType })
    ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getString("studentId").orEmpty()
        val viewModel: ParentViewModel = hiltViewModel()
        ChildDetailScreen(
            studentId = studentId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(
        route = Screen.ChildProgress.route,
        arguments = listOf(navArgument("studentUuid") { type = NavType.StringType; nullable = true; defaultValue = null })
    ) {
        val viewModel: ChildProgressViewModel = hiltViewModel()
        ChildProgressScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(
        route = Screen.ChildActivityFeed.route,
        arguments = listOf(navArgument("studentUuid") { type = NavType.StringType; nullable = true; defaultValue = null })
    ) { backStackEntry ->
        val viewModel: ParentViewModel = hiltViewModel()
        ChildActivityFeedScreen(
            studentUuid = backStackEntry.arguments?.getString("studentUuid"),
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
