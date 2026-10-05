package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.document.DownloadManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.update.InAppUpdateManager
import com.sultanagung1.sista.ui.auth.LoginViewModel
import com.sultanagung1.sista.ui.achievement.AchievementUploadScreen
import com.sultanagung1.sista.ui.achievement.AchievementViewModel
import com.sultanagung1.sista.ui.ai.AiTutorScreen
import com.sultanagung1.sista.ui.ai.AiViewModel
import com.sultanagung1.sista.ui.attendance.*
import com.sultanagung1.sista.ui.counseling.*
import com.sultanagung1.sista.ui.discipline.DisciplineScreen
import com.sultanagung1.sista.ui.discipline.DisciplineViewModel
import com.sultanagung1.sista.ui.document.DocumentScannerScreen
import com.sultanagung1.sista.ui.document.DownloadHistoryScreen
import com.sultanagung1.sista.ui.document.PdfViewerScreen
import com.sultanagung1.sista.ui.document.SignatureScreen
import com.sultanagung1.sista.ui.evaluation.TeacherEvaluationScreen
import com.sultanagung1.sista.ui.evaluation.EvaluationViewModel
import com.sultanagung1.sista.ui.extracurricular.ExtracurricularScreen
import com.sultanagung1.sista.ui.extracurricular.ExtracurricularViewModel
import com.sultanagung1.sista.ui.finance.BillingScreen
import com.sultanagung1.sista.ui.gamification.BadgeCollectionScreen
import com.sultanagung1.sista.ui.gamification.GamificationDashboardScreen
import com.sultanagung1.sista.ui.gamification.GamificationViewModel
import com.sultanagung1.sista.ui.gamification.LeaderboardScreen
import com.sultanagung1.sista.ui.ibadah.IbadahViewModel
import com.sultanagung1.sista.ui.ibadah.MutabaahScreen
import com.sultanagung1.sista.ui.ibadah.TahsinRecorderScreen
import com.sultanagung1.sista.ui.library.LibraryCatalogScreen
import com.sultanagung1.sista.ui.library.LibraryViewModel
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.profile.ProfileScreen
import com.sultanagung1.sista.ui.profile.ProfileViewModel
import com.sultanagung1.sista.ui.profile.StudentProfileComprehensiveScreen
import com.sultanagung1.sista.ui.profile.StudentProfileViewModel
import com.sultanagung1.sista.ui.scanner.QrScannerScreen
import com.sultanagung1.sista.ui.scanner.ScannerViewModel
import com.sultanagung1.sista.ui.settings.*
import com.sultanagung1.sista.ui.spmb.SpmbInfoScreen
import com.sultanagung1.sista.ui.spmb.SpmbRegistrationScreen
import com.sultanagung1.sista.ui.spmb.SpmbTrackingScreen
import com.sultanagung1.sista.ui.spmb.SpmbViewModel
import com.sultanagung1.sista.ui.uks.HealthHistoryScreen
import com.sultanagung1.sista.ui.uks.UksVisitScreen
import com.sultanagung1.sista.ui.uks.UksViewModel
import com.sultanagung1.sista.ui.update.UpdatePromptScreen
import com.sultanagung1.sista.ui.utbk.UtbkTryOutScreen
import com.sultanagung1.sista.ui.utbk.UtbkViewModel

/**
 * Sub-Navigation Graph untuk Modul Pengaturan, Profil, Perangkat Keras,
 * Layanan Kesiswaan & Operasional Sekolah (FASE 53.2).
 */
fun NavGraphBuilder.settingsNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    themeManager: ThemeManager,
    navigateToRoleHome: () -> Unit,
    audioRecorderManager: AudioRecorderManager,
    downloadManager: DownloadManager,
    inAppUpdateManager: InAppUpdateManager
) {
    // --- Profile & Settings ---
    guardedComposable(Screen.Profile.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                sessionManager = sessionManager,
                themeManager = themeManager,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    loginViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }

    guardedComposable(Screen.DiagnosticReport.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            com.sultanagung1.sista.ui.profile.DiagnosticReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    guardedComposable(Screen.Settings.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        SettingsScreen(
            viewModel = viewModel,
            onNavigateToLanguage = { navController.navigate(Screen.LanguageSettings.route) },
            onNavigateToAccessibility = { navController.navigate(Screen.AccessibilitySettings.route) },
            onNavigateToBiometrics = { navController.navigate(Screen.FaceEnrollment.route) },
            onNavigateToSecurity = { navController.navigate(Screen.SecuritySettings.route) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.LanguageSettings.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        LanguageSettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.AccessibilitySettings.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        AccessibilitySettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.SecuritySettings.route) {
        SecuritySettingsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigate = { route -> navController.navigate(route) }
        )
    }

    guardedComposable(
        route = Screen.StudentProfileComprehensive.route,
        arguments = listOf(navArgument("studentId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) {
        // Which student comes from the route (read by StudentProfileViewModel);
        // the server decides who may see it and answers 403/404 otherwise.
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            StudentProfileComprehensiveScreen(
                viewModel = hiltViewModel<StudentProfileViewModel>(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    // --- Hardware, Sensors & Attendance ---
    guardedComposable(Screen.GeofenceAttendance.route) {
        val viewModel: AttendanceViewModel = hiltViewModel()
        GeofenceAttendanceScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.DynamicQr.route) {
        val viewModel: AttendanceViewModel = hiltViewModel()
        DynamicQrScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.QrScanner.route) {
        val viewModel: ScannerViewModel = hiltViewModel()
        QrScannerScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.FaceEnrollment.route) {
        val viewModel: FaceEnrollmentViewModel = hiltViewModel()
        FaceEnrollmentScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.FaceBiometric.route) {
        val viewModel: FaceEnrollmentViewModel = hiltViewModel()
        FaceEnrollmentScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Ibadah & Islamic ---
    guardedComposable(Screen.Mutabaah.route) {
        val viewModel: IbadahViewModel = hiltViewModel()
        MutabaahScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.TahsinRecorder.route) {
        val viewModel: com.sultanagung1.sista.ui.ibadah.TahsinRecorderViewModel = hiltViewModel()
        TahsinRecorderScreen(
            recorderManager = audioRecorderManager,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHistory = { navController.navigate(Screen.TahsinHistory.route) }
        )
    }

    guardedComposable(Screen.TahsinHistory.route) {
        val viewModel: com.sultanagung1.sista.ui.ibadah.TahsinViewModel = hiltViewModel()
        com.sultanagung1.sista.ui.ibadah.TahsinHistoryScreen(
            viewModel = viewModel,
            onOpenSubmission = { id ->
                navController.navigate(Screen.TahsinSubmissionDetail.createRoute(id, teacherMode = false))
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(
        route = Screen.TahsinSubmissionDetail.route,
        arguments = listOf(
            navArgument("submissionId") { type = NavType.LongType },
            navArgument("teacherMode") { type = NavType.BoolType; defaultValue = false }
        )
    ) { backStackEntry ->
        val submissionId = backStackEntry.arguments?.getLong("submissionId") ?: 0L
        val teacherMode = backStackEntry.arguments?.getBoolean("teacherMode") ?: false
        val viewModel: com.sultanagung1.sista.ui.ibadah.TahsinViewModel = hiltViewModel()
        com.sultanagung1.sista.ui.ibadah.TahsinSubmissionDetailScreen(
            submissionId = submissionId,
            isTeacherMode = teacherMode,
            recorderManager = audioRecorderManager,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- AI Tutor ---
    guardedComposable(Screen.AiTutor.route) {
        val viewModel: AiViewModel = hiltViewModel()
        AiTutorScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Finance, SOS, Blockchain, Catalog ---
    guardedComposable(Screen.Billing.route) {
        BillingScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.AntiBullyingSos.route) {
        AntiBullyingSosScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Counseling BK ---
    guardedComposable(Screen.CounselingDashboard.route) {
        val viewModel: CounselingViewModel = hiltViewModel()
        CounselingDashboardScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToNewSession = { studentId ->
                navController.navigate(Screen.CounselingSessionForm.createRoute(studentId))
            }
        )
    }

    guardedComposable(
        route = Screen.CounselingSessionForm.route,
        arguments = listOf(navArgument("studentId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getLong("studentId")
        val viewModel: CounselingViewModel = hiltViewModel()
        CounselingSessionFormScreen(
            prefilledStudentId = if (studentId != null && studentId > 0) studentId else null,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.StudentCounseling.route) {
        val viewModel: CounselingViewModel = hiltViewModel()
        StudentCounselingScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Documents & PDF Management ---
    guardedComposable(
        route = Screen.PdfViewer.route,
        arguments = listOf(
            navArgument("fileUrl") { type = NavType.StringType; defaultValue = "" },
            navArgument("title") { type = NavType.StringType; defaultValue = "Dokumen" },
            navArgument("sizeBytes") { type = NavType.LongType; defaultValue = 0L }
        )
    ) { backStackEntry ->
        val fileUrlEncoded = backStackEntry.arguments?.getString("fileUrl") ?: ""
        val titleEncoded = backStackEntry.arguments?.getString("title") ?: "Dokumen"
        val sizeBytes = backStackEntry.arguments?.getLong("sizeBytes") ?: 0L
        val fileUrl = java.net.URLDecoder.decode(fileUrlEncoded, "UTF-8")
        val title = java.net.URLDecoder.decode(titleEncoded, "UTF-8")
        PdfViewerScreen(
            fileUrl = fileUrl,
            documentTitle = title,
            sizeBytes = sizeBytes,
            onNavigateBack = { navController.popBackStack() },
            onDownloadPdf = {
                downloadManager.startDownload(
                    title = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf",
                    fileType = "PDF",
                    sizeBytes = sizeBytes,
                    url = fileUrl
                )
            }
        )
    }

    guardedComposable(Screen.DownloadHistory.route) {
        DownloadHistoryScreen(
            downloadManager = downloadManager,
            onNavigateBack = { navController.popBackStack() },
            onOpenFile = { task ->
                task.localFilePath?.let { path ->
                    navController.navigate(Screen.PdfViewer.createRoute("file://$path", task.title, task.sizeBytes))
                }
            }
        )
    }

    guardedComposable(Screen.DocumentScanner.route) {
        DocumentScannerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.DigitalSignature.route) {
        SignatureScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- SuperApp Marketplace, SSO & Updates ---
    guardedComposable(Screen.InAppUpdate.route) {
        UpdatePromptScreen(
            updateManager = inAppUpdateManager,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Kesiswaan & Operasional Sekolah ---
    guardedComposable(
        route = Screen.Discipline.route,
        // Read by DisciplineViewModel through its SavedStateHandle.
        arguments = listOf(navArgument("studentUuid") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: DisciplineViewModel = hiltViewModel()
        DisciplineScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.UtbkTryout.route) {
        val viewModel: UtbkViewModel = hiltViewModel()
        UtbkTryOutScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onStartExamRoom = { examId -> navController.navigate(Screen.CbtRoom.createRoute(examId)) }
        )
    }

    guardedComposable(Screen.LibraryCatalog.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: LibraryViewModel = hiltViewModel()
            LibraryCatalogScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate(Screen.QrScanner.route) }
            )
        }
    }

    guardedComposable(Screen.Extracurricular.route) {
        val viewModel: ExtracurricularViewModel = hiltViewModel()
        ExtracurricularScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToScanner = { navController.navigate(Screen.QrScanner.route) }
        )
    }

    guardedComposable(Screen.AchievementUpload.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: AchievementViewModel = hiltViewModel()
            AchievementUploadScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    guardedComposable(Screen.TeacherEvaluation.route) {
        val viewModel: EvaluationViewModel = hiltViewModel()
        TeacherEvaluationScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- SPMB / PPDB ---
    guardedComposable(Screen.SpmbMobile.route) {
        val viewModel: SpmbViewModel = hiltViewModel()
        SpmbInfoScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRegister = { navController.navigate(Screen.SpmbRegistration.route) },
            onNavigateToTracking = { navController.navigate(Screen.SpmbTracking.createRoute(null)) }
        )
    }

    guardedComposable(Screen.SpmbInfo.route) {
        val viewModel: SpmbViewModel = hiltViewModel()
        SpmbInfoScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRegister = { navController.navigate(Screen.SpmbRegistration.route) },
            onNavigateToTracking = { navController.navigate(Screen.SpmbTracking.createRoute(null)) }
        )
    }

    guardedComposable(Screen.SpmbRegistration.route) {
        val viewModel: SpmbViewModel = hiltViewModel()
        SpmbRegistrationScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onRegistrationSuccess = { regNo ->
                navController.navigate(Screen.SpmbTracking.createRoute(regNo)) {
                    popUpTo(Screen.SpmbInfo.route) { inclusive = false }
                }
            }
        )
    }

    guardedComposable(
        route = Screen.SpmbTracking.route,
        arguments = listOf(navArgument("regNumber") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) { backStackEntry ->
        val regNumber = backStackEntry.arguments?.getString("regNumber")
        val viewModel: SpmbViewModel = hiltViewModel()
        SpmbTrackingScreen(
            initialRegNumber = regNumber,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- UKS Digital ---
    // Guarded like every destination by the server's feature list (uks.record).
    guardedComposable(Screen.UksVisit.route) {
        val viewModel: UksViewModel = hiltViewModel()
        val uksUiState by viewModel.uiState.collectAsState()
        UksVisitScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHealthHistory = {
                navController.navigate(Screen.HealthHistory.createRoute(uksUiState.selectedStudent?.id?.toString()))
            }
        )
    }

    guardedComposable(
        route = Screen.HealthHistory.route,
        arguments = listOf(navArgument("studentId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getString("studentId")
        val viewModel: UksViewModel = hiltViewModel()
        HealthHistoryScreen(
            studentId = studentId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Gamifikasi ---
    guardedComposable(Screen.GamificationDashboard.route) {
        val viewModel: GamificationViewModel = hiltViewModel()
        GamificationDashboardScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
            onNavigateToBadges = { navController.navigate(Screen.BadgeCollection.route) }
        )
    }

    guardedComposable(Screen.Leaderboard.route) {
        val viewModel: GamificationViewModel = hiltViewModel()
        LeaderboardScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.BadgeCollection.route) {
        val viewModel: GamificationViewModel = hiltViewModel()
        BadgeCollectionScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
