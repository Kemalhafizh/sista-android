package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.teacher.*
import com.sultanagung1.sista.ui.teacher.sessions.*
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
    guardedComposable(Screen.TeacherDashboard.route) {
        val viewModel: TeacherViewModel = hiltViewModel()
        TeacherDashboardScreen(
            viewModel = viewModel,
            onNavigateToAttendance = { classroomId, scheduleId, className ->
                navController.navigate(Screen.TeacherAttendance.createRoute(classroomId, scheduleId, className))
            },
            // The per-schedule "Isi Jurnal" quick action opens the real journal list/
            // compliance screen (Screen.TeachingJournalMobile) instead of a standalone
            // form — that screen already has the tabs, compliance %, and per-slot fill
            // status wired to real data; no need for a second, narrower journal flow.
            onNavigateToJournal = {
                navController.navigate(Screen.TeachingJournalMobile.route)
            },
            onNavigateRoute = { route -> navController.navigate(route) }
        )
    }

    guardedComposable(
        route = Screen.TeacherAttendance.route,
        arguments = listOf(
            navArgument("classroomId") { type = NavType.LongType },
            navArgument("scheduleId") { type = NavType.LongType },
            navArgument("className") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val classroomId = backStackEntry.arguments?.getLong("classroomId") ?: 0L
        val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
        val rawClassName = backStackEntry.arguments?.getString("className") ?: ""
        val className = URLDecoder.decode(rawClassName, StandardCharsets.UTF_8.toString())
        val viewModel: TeacherViewModel = hiltViewModel()

        TeacherAttendanceScreen(
            classroomId = classroomId,
            scheduleId = scheduleId,
            className = className,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // "Pengawas CBT" entry point: lists the exams this teacher really operates
    // (GET teacher/cbt/exams) and jumps straight in when exactly one is ongoing.
    guardedComposable(Screen.TeacherProctorExams.route) {
        TeacherProctorExamsScreen(
            onOpenExam = { examId, replacePicker ->
                navController.navigate(Screen.TeacherProctor.createRoute(examId)) {
                    if (replacePicker) {
                        popUpTo(Screen.TeacherProctorExams.route) { inclusive = true }
                    }
                }
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(
        route = Screen.TeacherProctor.route,
        arguments = listOf(navArgument("examId") { type = NavType.LongType })
    ) { backStackEntry ->
        // No placeholder id: a missing/invalid id means there is no exam to
        // proctor, so send the teacher to their real exam list instead.
        val examId = backStackEntry.arguments?.getLong("examId")?.takeIf { it > 0 }
        if (examId != null) {
            TeacherProctorDashboardScreen(
                examId = examId,
                onNavigateBack = { navController.popBackStack() }
            )
        } else {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.TeacherProctorExams.route) {
                    popUpTo(Screen.TeacherProctor.route) { inclusive = true }
                }
            }
        }
    }

    guardedComposable(Screen.TeacherCreateExam.route) {
        TeacherCreateExamScreen(
            onNavigateBack = { navController.popBackStack() },
            // Only offered when the server's create response says this
            // teacher may view the token. The form is replaced, not
            // stacked: going back from the proctor room must not reopen
            // an already-published draft.
            onExamCreated = { newExamId ->
                navController.navigate(Screen.TeacherProctor.createRoute(newExamId)) {
                    popUpTo(Screen.TeacherCreateExam.route) { inclusive = true }
                }
            }
        )
    }

    guardedComposable(Screen.QuestionBank.route) {
        val viewModel: QuestionBankViewModel = hiltViewModel()
        QuestionBankScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAutoGenerate = { navController.navigate(Screen.AutoGenerateExam.route) },
            onNavigateToManualCreate = { navController.navigate(Screen.TeacherCreateExam.route) }
        )
    }

    guardedComposable(Screen.AutoGenerateExam.route) {
        val viewModel: QuestionBankViewModel = hiltViewModel()
        AutoGenerateExamScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.DailyAssessmentList.route) {
        val viewModel: DailyAssessmentViewModel = hiltViewModel()
        DailyAssessmentScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToScoreInput = { assessmentId ->
                navController.navigate(Screen.ScoreInput.createRoute(assessmentId))
            }
        )
    }

    guardedComposable(
        route = Screen.ScoreInput.route,
        arguments = listOf(navArgument("assessmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val assessmentId = backStackEntry.arguments?.getLong("assessmentId") ?: 1L
        val viewModel: DailyAssessmentViewModel = hiltViewModel()
        ScoreInputScreen(
            assessmentId = assessmentId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.RemedialList.route) {
        val viewModel: DailyAssessmentViewModel = hiltViewModel()
        RemedialScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.TahsinTeacherReview.route) {
        val viewModel: com.sultanagung1.sista.ui.ibadah.TahsinViewModel = hiltViewModel()
        com.sultanagung1.sista.ui.ibadah.TahsinTeacherReviewListScreen(
            viewModel = viewModel,
            onOpenSubmission = { id ->
                navController.navigate(Screen.TahsinSubmissionDetail.createRoute(id, teacherMode = true))
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.TeachingJournalMobile.route) {
        val viewModel: JournalMobileViewModel = hiltViewModel()
        TeachingJournalMobileScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToForm = { scheduleId ->
                navController.navigate(Screen.JournalForm.createRoute(scheduleId))
            }
        )
    }

    guardedComposable(
        route = Screen.JournalForm.route,
        arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
    ) { backStackEntry ->
        val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: "sch-1"
        val viewModel: JournalMobileViewModel = hiltViewModel()
        JournalFormScreen(
            scheduleId = scheduleId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ── FASE 77: Sesi Kelas Hidup (guru) ─────────────────────────────────
    // Backend teacher routes are role:guru,bk; admins have their own screens (77.6).

    guardedComposable(Screen.TeacherTodaySessions.route) {
        val viewModel: TeacherTodaySessionsViewModel = hiltViewModel()
        TeacherTodaySessionsScreen(
            viewModel = viewModel,
            onOpenActiveSession = { sessionId ->
                navController.navigate(Screen.TeacherActiveSession.createRoute(sessionId)) { launchSingleTop = true }
            },
            // teacher/schedule (the week) lives in the journal screen's schedule tab.
            onOpenWeeklySchedule = { navController.navigate(Screen.TeachingJournalMobile.route) },
            onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null
        )
    }

    guardedComposable(
        route = Screen.TeacherActiveSession.route,
        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
    ) {
        val viewModel: TeacherActiveSessionViewModel = hiltViewModel()
        TeacherActiveSessionScreen(
            viewModel = viewModel,
            onOpenAttendanceList = { sessionId ->
                navController.navigate(Screen.TeacherAttendanceList.createRoute(sessionId)) { launchSingleTop = true }
            },
            onNavigateBack = { navController.popBackStack() },
            onOpenTeachingJournal = { navController.navigate(Screen.TeachingJournalMobile.route) }
        )
    }

    guardedComposable(
        route = Screen.TeacherAttendanceList.route,
        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
    ) {
        val viewModel: TeacherAttendanceListViewModel = hiltViewModel()
        TeacherAttendanceListScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

/** Matches the backend `teacher/…` group (role:guru,bk); "teacher" is the app's own spelling of guru. */
private val CLASS_SESSION_TEACHER_ROLES = listOf("teacher", "guru", "bk")
