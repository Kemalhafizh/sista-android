package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.ui.cbt.CbtExamListScreen
import com.sultanagung1.sista.ui.cbt.CbtExamRoomScreen
import com.sultanagung1.sista.ui.cbt.CbtTokenEntryScreen
import com.sultanagung1.sista.ui.cbt.CbtViewModel
import com.sultanagung1.sista.ui.navigation.Screen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Sub-Navigation Graph untuk Mesin Ujian CBT Sultan Agung (FASE 53.2).
 */
fun NavGraphBuilder.cbtNavGraph(
    navController: NavHostController,
    isSensitiveProtectionEnabled: Boolean
) {
    composable(Screen.CbtList.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: CbtViewModel = hiltViewModel()
            val context = LocalContext.current

            CbtExamListScreen(
                viewModel = viewModel,
                onNavigateToRoom = { examId ->
                    val launchCbt = {
                        val exam = viewModel.getExamById(examId)
                        if (exam != null) {
                            navController.navigate(
                                Screen.CbtTokenEntry.createRoute(
                                    examId = exam.id,
                                    title = exam.title,
                                    subject = exam.subject,
                                    type = exam.status,
                                    duration = exam.durationMinutes,
                                    totalQuestions = exam.totalQuestions
                                )
                            )
                        } else {
                            navController.navigate(
                                Screen.CbtTokenEntry.createRoute(
                                    examId = examId,
                                    title = "Ujian CBT Sultan Agung",
                                    subject = "Mata Pelajaran",
                                    type = "CBT",
                                    duration = 90,
                                    totalQuestions = 30
                                )
                            )
                        }
                    }

                    val activity = context as? FragmentActivity
                    if (isSensitiveProtectionEnabled && activity != null) {
                        BiometricVault.authenticate(
                            activity = activity,
                            title = "Proteksi Ujian CBT",
                            subtitle = "Verifikasi sidik jari peserta sebelum masuk ruang ujian",
                            negativeButtonText = "Batal",
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

    composable(
        route = Screen.CbtTokenEntry.route,
        arguments = listOf(
            navArgument("examId") { type = NavType.LongType },
            navArgument("examTitle") { type = NavType.StringType },
            navArgument("examSubject") { type = NavType.StringType },
            navArgument("examType") { type = NavType.StringType },
            navArgument("duration") { type = NavType.IntType },
            navArgument("totalQuestions") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val examId = backStackEntry.arguments?.getLong("examId") ?: 1L
            val rawTitle = backStackEntry.arguments?.getString("examTitle") ?: "Ujian CBT"
            val rawSubject = backStackEntry.arguments?.getString("examSubject") ?: "Mata Pelajaran"
            val examType = backStackEntry.arguments?.getString("examType") ?: "CBT"
            val duration = backStackEntry.arguments?.getInt("duration") ?: 90
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 30

            val title = try { URLDecoder.decode(rawTitle, StandardCharsets.UTF_8.toString()) } catch (e: Exception) { rawTitle }
            val subject = try { URLDecoder.decode(rawSubject, StandardCharsets.UTF_8.toString()) } catch (e: Exception) { rawSubject }

            val viewModel: CbtViewModel = hiltViewModel()
            CbtTokenEntryScreen(
                examId = examId,
                examTitle = title,
                examSubject = subject,
                examType = examType,
                durationMinutes = duration,
                totalQuestions = totalQuestions,
                viewModel = viewModel,
                onTokenValidated = {
                    val validated = viewModel.uiState.value
                    navController.navigate(
                        Screen.CbtRoom.createRoute(examId, validated.studentId, validated.maxViolations)
                    ) {
                        popUpTo(Screen.CbtTokenEntry.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(
        route = Screen.CbtRoom.route,
        arguments = listOf(
            navArgument("examId") { type = NavType.LongType },
            navArgument("studentId") { type = NavType.LongType; defaultValue = -1L },
            navArgument("maxViolations") { type = NavType.IntType; defaultValue = -1 }
        )
    ) { backStackEntry ->
        val examId = backStackEntry.arguments?.getLong("examId") ?: 1L
        val studentId = backStackEntry.arguments?.getLong("studentId")?.takeIf { it > 0 }
        val maxViolations = backStackEntry.arguments?.getInt("maxViolations")?.takeIf { it > 0 }
        val viewModel: CbtViewModel = hiltViewModel()
        CbtExamRoomScreen(
            examId = examId,
            studentId = studentId,
            maxViolations = maxViolations,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
