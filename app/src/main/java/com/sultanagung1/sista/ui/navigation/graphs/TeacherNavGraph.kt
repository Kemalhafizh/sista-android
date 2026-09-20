package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.academic.RemedialScreen
import com.sultanagung1.sista.ui.common.RoleGuardedScreen
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.teacher.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Sub-Navigation Graph untuk Modul Guru & Pengajaran (FASE 53.2).
 */
fun NavGraphBuilder.teacherNavGraph(
    navController: NavHostController,
    userRole: String,
    navigateToRoleHome: () -> Unit
) {
    composable(Screen.TeacherDashboard.route) {
        val viewModel: TeacherViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Dashboard Guru & Pendidik",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeacherDashboardScreen(
                viewModel = viewModel,
                onNavigateToAttendance = { scheduleId, className ->
                    navController.navigate(Screen.TeacherAttendance.createRoute(scheduleId, className))
                },
                onNavigateToJournal = { scheduleId, className, subjectName ->
                    navController.navigate(Screen.TeacherJournal.createRoute(scheduleId, className, subjectName))
                },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
    }

    composable(
        route = Screen.TeacherAttendance.route,
        arguments = listOf(
            navArgument("scheduleId") { type = NavType.StringType },
            navArgument("className") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: "s1"
        val rawClassName = backStackEntry.arguments?.getString("className") ?: "XII MIPA 1"
        val className = URLDecoder.decode(rawClassName, StandardCharsets.UTF_8.toString())
        val viewModel: TeacherViewModel = hiltViewModel()

        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Presensi Kelas (Guru)",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeacherAttendanceScreen(
                scheduleId = scheduleId,
                className = className,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(
        route = Screen.TeacherJournal.route,
        arguments = listOf(
            navArgument("scheduleId") { type = NavType.StringType },
            navArgument("className") { type = NavType.StringType },
            navArgument("subjectName") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: "s1"
        val rawClassName = backStackEntry.arguments?.getString("className") ?: "XII MIPA 1"
        val rawSubject = backStackEntry.arguments?.getString("subjectName") ?: "Matematika"
        val className = URLDecoder.decode(rawClassName, StandardCharsets.UTF_8.toString())
        val subjectName = URLDecoder.decode(rawSubject, StandardCharsets.UTF_8.toString())
        val viewModel: TeacherViewModel = hiltViewModel()

        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Jurnal Mengajar Guru",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeachingJournalScreen(
                scheduleId = scheduleId,
                className = className,
                subjectName = subjectName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(
        route = Screen.TeacherProctor.route,
        arguments = listOf(navArgument("examId") { type = NavType.LongType })
    ) { backStackEntry ->
        val examId = backStackEntry.arguments?.getLong("examId") ?: 101L
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Pengawas Ujian Live (Proctor)",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeacherProctorDashboardScreen(
                examId = examId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.TeacherCreateExam.route) {
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Buat Ulangan Daring",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeacherCreateExamScreen(
                onNavigateBack = { navController.popBackStack() },
                onExamCreated = { newExamId ->
                    navController.navigate(Screen.TeacherProctor.createRoute(newExamId))
                }
            )
        }
    }

    composable(Screen.QuestionBank.route) {
        val viewModel: QuestionBankViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Bank Soal Terpusat",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            QuestionBankScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAutoGenerate = { navController.navigate(Screen.AutoGenerateExam.route) },
                onNavigateToManualCreate = { navController.navigate(Screen.TeacherCreateExam.route) }
            )
        }
    }

    composable(Screen.AutoGenerateExam.route) {
        val viewModel: QuestionBankViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Auto-Generate Ujian (AI)",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            AutoGenerateExamScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.DailyAssessmentList.route) {
        val viewModel: DailyAssessmentViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Penilaian Harian KBM",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            DailyAssessmentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScoreInput = { assessmentId ->
                    navController.navigate(Screen.ScoreInput.createRoute(assessmentId))
                }
            )
        }
    }

    composable(
        route = Screen.ScoreInput.route,
        arguments = listOf(navArgument("assessmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val assessmentId = backStackEntry.arguments?.getLong("assessmentId") ?: 1L
        val viewModel: DailyAssessmentViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Input Nilai Siswa",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            ScoreInputScreen(
                assessmentId = assessmentId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.RemedialList.route) {
        val viewModel: DailyAssessmentViewModel = hiltViewModel()
        RemedialScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.TeachingJournal.route) {
        val viewModel: JournalMobileViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Jurnal Mengajar Guru",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeachingJournalMobileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { scheduleId ->
                    navController.navigate(Screen.JournalForm.createRoute(scheduleId))
                }
            )
        }
    }

    composable(Screen.TeachingJournalMobile.route) {
        val viewModel: JournalMobileViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Jurnal Mengajar Guru",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            TeachingJournalMobileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { scheduleId ->
                    navController.navigate(Screen.JournalForm.createRoute(scheduleId))
                }
            )
        }
    }

    composable(
        route = Screen.JournalForm.route,
        arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
    ) { backStackEntry ->
        val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: "sch-1"
        val viewModel: JournalMobileViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "admin", "superadmin"),
            featureTitle = "Catat Jurnal Mengajar",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            JournalFormScreen(
                scheduleId = scheduleId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
