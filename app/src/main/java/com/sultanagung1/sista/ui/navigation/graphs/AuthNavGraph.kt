package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.sultanagung1.sista.ui.navigation.guardedComposable
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
    onSignedIn: () -> Unit,
    syncManager: SyncManager
) {
    guardedComposable(Screen.Login.route) {
        val viewModel: LoginViewModel = hiltViewModel()
        LoginScreen(
            viewModel = viewModel,
            // The home is chosen from the account's server-given features,
            // loaded right after sign-in (AppNavigation.onSignedIn).
            onLoginSuccess = onSignedIn
        )
    }

    guardedComposable(Screen.Home.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateRoute = { route -> navController.navigate(route) { launchSingleTop = true } },
                syncManager = syncManager
            )
        }
    }
}
