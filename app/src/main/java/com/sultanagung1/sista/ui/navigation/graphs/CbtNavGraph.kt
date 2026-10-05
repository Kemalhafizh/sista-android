package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.R
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.ui.cbt.CbtExamListScreen
import com.sultanagung1.sista.ui.cbt.CbtExamRoomScreen
import com.sultanagung1.sista.ui.cbt.CbtTokenEntryScreen
import com.sultanagung1.sista.ui.cbt.CbtViewModel
import com.sultanagung1.sista.ui.navigation.Screen

/**
 * The student CBT flow (FASE 53.2): exam list, token gate, exam room.
 */
fun NavGraphBuilder.cbtNavGraph(
    navController: NavHostController,
    isSensitiveProtectionEnabled: Boolean
) {
    guardedComposable(Screen.CbtList.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: CbtViewModel = hiltViewModel()
            val context = LocalContext.current

            val biometricTitle = stringResource(R.string.cbt_biometric_title)
            val biometricSubtitle = stringResource(R.string.cbt_biometric_subtitle)
            val biometricCancel = stringResource(R.string.cbt_biometric_cancel)

            CbtExamListScreen(
                viewModel = viewModel,
                onNavigateToRoom = { examId ->
                    val launchCbt = { navController.navigate(Screen.CbtTokenEntry.createRoute(examId)) }
                    val activity = context as? FragmentActivity
                    if (isSensitiveProtectionEnabled && activity != null) {
                        BiometricVault.authenticate(
                            activity = activity,
                            title = biometricTitle,
                            subtitle = biometricSubtitle,
                            negativeButtonText = biometricCancel,
                            onSuccess = { launchCbt() }
                        )
                    } else {
                        launchCbt()
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    guardedComposable(
        route = Screen.CbtTokenEntry.route,
        arguments = listOf(navArgument("examId") { type = NavType.LongType })
    ) { backStackEntry ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val examId = backStackEntry.arguments?.getLong("examId") ?: return@CompositionLocalProvider
            val viewModel: CbtViewModel = hiltViewModel()
            CbtTokenEntryScreen(
                examId = examId,
                viewModel = viewModel,
                onTokenValidated = {
                    val validated = viewModel.uiState.value
                    navController.navigate(
                        Screen.CbtRoom.createRoute(examId, validated.studentId, validated.maxViolations, validated.remainingSeconds)
                    ) {
                        popUpTo(Screen.CbtTokenEntry.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    guardedComposable(
        route = Screen.CbtRoom.route,
        arguments = listOf(
            navArgument("examId") { type = NavType.LongType },
            navArgument("studentId") { type = NavType.LongType; defaultValue = -1L },
            navArgument("maxViolations") { type = NavType.IntType; defaultValue = -1 },
            navArgument("remainingSeconds") { type = NavType.LongType; defaultValue = -1L }
        )
    ) { backStackEntry ->
        val examId = backStackEntry.arguments?.getLong("examId") ?: return@guardedComposable
        val studentId = backStackEntry.arguments?.getLong("studentId")?.takeIf { it > 0 }
        val maxViolations = backStackEntry.arguments?.getInt("maxViolations")?.takeIf { it > 0 }
        val remainingSeconds = backStackEntry.arguments?.getLong("remainingSeconds")?.takeIf { it >= 0 }
        val viewModel: CbtViewModel = hiltViewModel()
        CbtExamRoomScreen(
            examId = examId,
            studentId = studentId,
            maxViolations = maxViolations,
            initialRemainingSeconds = remainingSeconds,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
