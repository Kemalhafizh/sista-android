package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.ui.academic.*
import com.sultanagung1.sista.ui.attendance.StudentSessionQrScanScreen
import com.sultanagung1.sista.ui.attendance.StudentSessionQrScanViewModel
import com.sultanagung1.sista.ui.analytics.AcademicAnalyticsScreen
import com.sultanagung1.sista.ui.analytics.AnalyticsViewModel
import com.sultanagung1.sista.ui.analytics.ClassAnalyticsScreen
import com.sultanagung1.sista.ui.calendar.AcademicCalendarScreen
import com.sultanagung1.sista.ui.calendar.CalendarViewModel
import com.sultanagung1.sista.ui.calendar.EventDetailScreen
import com.sultanagung1.sista.ui.elearning.AdaptiveElearningScreen
import com.sultanagung1.sista.ui.elearning.AssignmentSubmitScreen
import com.sultanagung1.sista.ui.elearning.ElearningClassDetailScreen
import com.sultanagung1.sista.ui.elearning.ElearningClassListScreen
import com.sultanagung1.sista.ui.elearning.ElearningViewModel
import com.sultanagung1.sista.ui.navigation.Screen

/**
 * Sub-Navigation Graph untuk Modul Akademik, LMS, Kalender & Analitik (FASE 53.2 & FASE 55.1).
 */
fun NavGraphBuilder.academicNavGraph(
    navController: NavHostController,
    userRole: String,
    navigateToRoleHome: () -> Unit
) {
    // FASE 77.5: per-subject attendance by scanning the teacher's rotating QR.
    // Backend student/* routes are role:student,siswa.
    guardedComposable(Screen.StudentSessionQrScan.route) {
        val viewModel: StudentSessionQrScanViewModel = hiltViewModel()
        StudentSessionQrScanScreen(
            viewModel = viewModel,
            onNavigateHome = navigateToRoleHome,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.Schedule.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: AcademicViewModel = hiltViewModel()
            ScheduleScreen(
                viewModel = viewModel,
                onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null
            )
        }
    }

    guardedComposable(Screen.Grades.route) {
        val viewModel: AcademicViewModel = hiltViewModel()
        GradesScreen(
            viewModel = viewModel,
            onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null
        )
    }

    guardedComposable(Screen.RaporDetail.route) {
        val viewModel: RaporViewModel = hiltViewModel()
        RaporDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPdfViewer = { filePath, title ->
                val resolvedUrl = com.sultanagung1.sista.core.util.Constants.resolveStorageUrl(filePath)
                navController.navigate(Screen.PdfViewer.createRoute(resolvedUrl, title, 0L))
            }
        )
    }

    guardedComposable(Screen.ElearningClassList.route) {
        val viewModel: ElearningViewModel = hiltViewModel()
        AdaptiveElearningScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onSelectClass = { classId ->
                navController.navigate(Screen.ElearningClassDetail.createRoute(classId))
            },
            onNavigateToSubmitAssignment = { assignmentId ->
                navController.navigate(Screen.AssignmentSubmit.createRoute(assignmentId))
            }
        )
    }

    guardedComposable(
        route = Screen.ElearningClassDetail.route,
        arguments = listOf(navArgument("classId") { type = NavType.LongType })
    ) { backStackEntry ->
        val classId = backStackEntry.arguments?.getLong("classId") ?: 1L
        val viewModel: ElearningViewModel = hiltViewModel()
        ElearningClassDetailScreen(
            classId = classId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSubmitAssignment = { assignmentId ->
                navController.navigate(Screen.AssignmentSubmit.createRoute(assignmentId))
            }
        )
    }

    guardedComposable(
        route = Screen.AssignmentSubmit.route,
        arguments = listOf(navArgument("assignmentId") { type = NavType.LongType })
    ) { backStackEntry ->
        val assignmentId = backStackEntry.arguments?.getLong("assignmentId") ?: 1L
        val viewModel: ElearningViewModel = hiltViewModel()
        AssignmentSubmitScreen(
            assignmentId = assignmentId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.AcademicCalendar.route) {
        val viewModel: CalendarViewModel = hiltViewModel()
        AcademicCalendarScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEventDetail = { eventId ->
                navController.navigate(Screen.EventDetail.createRoute(eventId))
            }
        )
    }

    guardedComposable(
        route = Screen.EventDetail.route,
        arguments = listOf(navArgument("eventId") { type = NavType.StringType })
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
        val viewModel: CalendarViewModel = hiltViewModel()
        EventDetailScreen(
            eventId = eventId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.AcademicAnalytics.route) {
        val viewModel: AnalyticsViewModel = hiltViewModel()
        AcademicAnalyticsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.ClassAnalytics.route) {
        val viewModel: AnalyticsViewModel = hiltViewModel()
        ClassAnalyticsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
