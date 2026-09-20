package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.core.sync.SyncManager
import com.sultanagung1.sista.ui.auth.LoginScreen
import com.sultanagung1.sista.ui.auth.LoginViewModel
import com.sultanagung1.sista.ui.home.HomeScreen
import com.sultanagung1.sista.ui.home.HomeViewModel
import com.sultanagung1.sista.ui.navigation.Screen

/**
 * Sub-Navigation Graph untuk modul Autentikasi dan Beranda (FASE 53.2).
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    userRole: String,
    syncManager: SyncManager
) {
    composable(Screen.Login.route) {
        val viewModel: LoginViewModel = hiltViewModel()
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                val targetRoute = when {
                    userRole.contains("teacher", ignoreCase = true) || userRole.contains("guru", ignoreCase = true) -> Screen.TeacherDashboard.route
                    userRole.contains("parent", ignoreCase = true) || userRole.contains("ortu", ignoreCase = true) -> Screen.ParentDashboard.route
                    userRole.contains("admin", ignoreCase = true) || userRole.contains("kepsek", ignoreCase = true) -> Screen.AdminDashboard.route
                    else -> Screen.Home.route
                }
                navController.navigate(targetRoute) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.Home.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGeofence = { navController.navigate(Screen.GeofenceAttendance.route) },
                onNavigateToDynamicQr = { navController.navigate(Screen.DynamicQr.route) },
                onNavigateToCbt = { navController.navigate(Screen.CbtList.route) },
                onNavigateToAiTutor = { navController.navigate(Screen.AiTutor.route) },
                onNavigateToMutabaah = { navController.navigate(Screen.Mutabaah.route) },
                onNavigateToBilling = { navController.navigate(Screen.Billing.route) },
                onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                onNavigateToGrades = { navController.navigate(Screen.Grades.route) },
                onNavigateToSos = { navController.navigate(Screen.AntiBullyingSos.route) },
                onNavigateToCatalog = { navController.navigate(Screen.EnterpriseCatalog.route) },
                onNavigateToChat = { navController.navigate(Screen.ConversationList.route) },
                onNavigateToAnnouncements = { navController.navigate(Screen.NotificationCenter.route) },
                onNavigateRoute = { route -> navController.navigate(route) },
                syncManager = syncManager,
                onNavigateToGamification = { navController.navigate(Screen.GamificationDashboard.route) }
            )
        }
    }
}
