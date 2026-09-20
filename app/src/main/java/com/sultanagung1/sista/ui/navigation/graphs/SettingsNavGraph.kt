package com.sultanagung1.sista.ui.navigation.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sultanagung1.sista.core.motion.LocalNavAnimatedVisibilityScope
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.document.DownloadManager
import com.sultanagung1.sista.core.lite.LiteModeManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.update.InAppUpdateManager
import com.sultanagung1.sista.ui.auth.LoginViewModel
import com.sultanagung1.sista.ui.achievement.AchievementUploadScreen
import com.sultanagung1.sista.ui.achievement.AchievementViewModel
import com.sultanagung1.sista.ui.ai.AiTutorScreen
import com.sultanagung1.sista.ui.ai.AiViewModel
import com.sultanagung1.sista.ui.attendance.*
import com.sultanagung1.sista.ui.common.RoleGuardedScreen
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
import com.sultanagung1.sista.ui.portal.EnterpriseCatalogScreen
import com.sultanagung1.sista.ui.portal.ModuleFavoritesScreen
import com.sultanagung1.sista.ui.portal.SsoWebViewScreen
import com.sultanagung1.sista.ui.profile.ProfileScreen
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
import kotlinx.coroutines.launch

/**
 * Sub-Navigation Graph untuk Modul Pengaturan, Profil, Perangkat Keras,
 * Layanan Kesiswaan & Operasional Sekolah (FASE 53.2).
 */
fun NavGraphBuilder.settingsNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    themeManager: ThemeManager,
    userRole: String,
    navigateToRoleHome: () -> Unit,
    audioRecorderManager: AudioRecorderManager,
    downloadManager: DownloadManager,
    inAppUpdateManager: InAppUpdateManager,
    liteModeManager: LiteModeManager
) {
    // --- Profile & Settings ---
    composable(Screen.Profile.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val coroutineScope = rememberCoroutineScope()
            val loginViewModel: LoginViewModel = hiltViewModel()
            ProfileScreen(
                sessionManager = sessionManager,
                themeManager = themeManager,
                onNavigateBack = null,
                onNavigateToBiometrics = { navController.navigate(Screen.FaceEnrollment.route) },
                onNavigateToAnnouncements = { navController.navigate(Screen.AnnouncementFeed.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.DiagnosticReport.route) },
                onRoleSwitch = { newRole ->
                    coroutineScope.launch {
                        val roleKey = newRole.lowercase()
                        when {
                            roleKey.contains("teacher") || roleKey.contains("guru") -> {
                                sessionManager.saveAuthSession("token_teacher", "teacher", "Ustadz Ahmad Fauzi, M.Pd", "guru.matematika@sultanagung1.sch.id", "198504122010011002")
                                navController.navigate(Screen.TeacherDashboard.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            roleKey.contains("parent") || roleKey.contains("ortu") -> {
                                sessionManager.saveAuthSession("token_parent", "parent", "Bapak Hendra Gunawan, S.T.", "ortu1@parent.sa1.sch.id", "wali-0071829102")
                                navController.navigate(Screen.ParentDashboard.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            roleKey.contains("admin") || roleKey.contains("kepsek") -> {
                                sessionManager.saveAuthSession("token_admin", "admin", "Drs. H. Muhammad Arif, M.Pd", "admin@sultanagung1.sch.id", "197405121998031002")
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            else -> {
                                sessionManager.saveAuthSession("token_student", "student", "Ahmad Kemal Hafizh", "siswa1@student.sa1.sch.id", "0071829102")
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                },
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

    composable(Screen.DiagnosticReport.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            com.sultanagung1.sista.ui.profile.DiagnosticReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.Settings.route) {
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

    composable(Screen.LanguageSettings.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        LanguageSettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.AccessibilitySettings.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        AccessibilitySettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.SecuritySettings.route) {
        SecuritySettingsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.LiteModeSettings.route) {
        LiteModeSettingsScreen(
            liteModeManager = liteModeManager,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.StudentProfileComprehensive.route,
        arguments = listOf(navArgument("studentId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val studentId = backStackEntry.arguments?.getLong("studentId")
            val isViewingOther = studentId != null && studentId > 0L
            val isPrivileged = userRole.contains("teacher", ignoreCase = true) ||
                    userRole.contains("guru", ignoreCase = true) ||
                    userRole.contains("admin", ignoreCase = true) ||
                    userRole.contains("kepsek", ignoreCase = true) ||
                    userRole.contains("principal", ignoreCase = true) ||
                    userRole.contains("superadmin", ignoreCase = true) ||
                    userRole.contains("bk", ignoreCase = true)

            if (isViewingOther && !isPrivileged) {
                RoleGuardedScreen(
                    currentRole = userRole,
                    allowedRoles = listOf("teacher", "guru", "bk", "admin", "superadmin", "principal"),
                    featureTitle = "Profil 360 Siswa Lain",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateHome = navigateToRoleHome
                ) {
                    Box(Modifier.fillMaxSize())
                }
            } else {
                val viewModel: StudentProfileViewModel = hiltViewModel()
                StudentProfileComprehensiveScreen(
                    studentId = if (studentId != null && studentId > 0) studentId else null,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    // --- Hardware, Sensors & Attendance ---
    composable(Screen.GeofenceAttendance.route) {
        val viewModel: AttendanceViewModel = hiltViewModel()
        GeofenceAttendanceScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.DynamicQr.route) {
        val viewModel: AttendanceViewModel = hiltViewModel()
        DynamicQrScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.QrScanner.route) {
        val viewModel: ScannerViewModel = hiltViewModel()
        QrScannerScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.FaceEnrollment.route) {
        val viewModel: FaceEnrollmentViewModel = hiltViewModel()
        FaceEnrollmentScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.FaceBiometric.route) {
        val viewModel: FaceEnrollmentViewModel = hiltViewModel()
        FaceEnrollmentScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Ibadah & Islamic ---
    composable(Screen.Mutabaah.route) {
        val viewModel: IbadahViewModel = hiltViewModel()
        MutabaahScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.TahsinRecorder.route) {
        TahsinRecorderScreen(
            recorderManager = audioRecorderManager,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- AI Tutor ---
    composable(Screen.AiTutor.route) {
        val viewModel: AiViewModel = hiltViewModel()
        AiTutorScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Finance, SOS, Blockchain, Catalog ---
    composable(Screen.Billing.route) {
        BillingScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.AntiBullyingSos.route) {
        AntiBullyingSosScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.EnterpriseCatalog.route) {
        EnterpriseCatalogScreen(
            userRole = userRole,
            onNavigateBack = { navController.popBackStack() },
            onNavigateRoute = { route -> navController.navigate(route) }
        )
    }

    // --- Counseling BK ---
    composable(Screen.CounselingDashboard.route) {
        val viewModel: CounselingViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "bk", "admin", "superadmin"),
            featureTitle = "Dashboard Bimbingan Konseling (BK)",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            CounselingDashboardScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewSession = { studentId ->
                    navController.navigate(Screen.CounselingSessionForm.createRoute(studentId))
                }
            )
        }
    }

    composable(
        route = Screen.CounselingSessionForm.route,
        arguments = listOf(navArgument("studentId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        val studentId = backStackEntry.arguments?.getLong("studentId")
        val viewModel: CounselingViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("teacher", "guru", "bk", "admin", "superadmin"),
            featureTitle = "Pencatatan Sesi Konseling",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            CounselingSessionFormScreen(
                prefilledStudentId = if (studentId != null && studentId > 0) studentId else null,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.StudentCounseling.route) {
        val viewModel: CounselingViewModel = hiltViewModel()
        StudentCounselingScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Documents & PDF Management ---
    composable(
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

    composable(Screen.DownloadHistory.route) {
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

    composable(Screen.DocumentScanner.route) {
        DocumentScannerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.DigitalSignature.route) {
        SignatureScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- SuperApp Marketplace, SSO & Updates ---
    composable(Screen.SsoWebView.route) {
        SsoWebViewScreen(
            sessionManager = sessionManager,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.ModuleFavorites.route) {
        ModuleFavoritesScreen(
            onNavigateToRoute = { route -> navController.navigate(route) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.InAppUpdate.route) {
        UpdatePromptScreen(
            updateManager = inAppUpdateManager,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- Kesiswaan & Operasional Sekolah ---
    composable(Screen.Discipline.route) {
        val viewModel: DisciplineViewModel = hiltViewModel()
        DisciplineScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.UtbkTryout.route) {
        val viewModel: UtbkViewModel = hiltViewModel()
        UtbkTryOutScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onStartExamRoom = { examId -> navController.navigate(Screen.CbtRoom.createRoute(examId)) }
        )
    }

    composable(Screen.LibraryCatalog.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: LibraryViewModel = hiltViewModel()
            LibraryCatalogScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate(Screen.QrScanner.route) }
            )
        }
    }

    composable(Screen.Extracurricular.route) {
        val viewModel: ExtracurricularViewModel = hiltViewModel()
        ExtracurricularScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToScanner = { navController.navigate(Screen.QrScanner.route) }
        )
    }

    composable(Screen.AchievementUpload.route) {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            val viewModel: AchievementViewModel = hiltViewModel()
            AchievementUploadScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.TeacherEvaluation.route) {
        val viewModel: EvaluationViewModel = hiltViewModel()
        TeacherEvaluationScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // --- SPMB / PPDB ---
    composable(Screen.SpmbMobile.route) {
        val viewModel: SpmbViewModel = hiltViewModel()
        SpmbInfoScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRegister = { navController.navigate(Screen.SpmbRegistration.route) },
            onNavigateToTracking = { navController.navigate(Screen.SpmbTracking.createRoute(null)) }
        )
    }

    composable(Screen.SpmbInfo.route) {
        val viewModel: SpmbViewModel = hiltViewModel()
        SpmbInfoScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRegister = { navController.navigate(Screen.SpmbRegistration.route) },
            onNavigateToTracking = { navController.navigate(Screen.SpmbTracking.createRoute(null)) }
        )
    }

    composable(Screen.SpmbRegistration.route) {
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

    composable(
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
    // NOTE: Screen.UksDigital used to duplicate this exact destination WITHOUT
    // a RoleGuardedScreen wrap, leaving UKS staff-only medical visit records
    // reachable by any authenticated role via that route string. Nothing in
    // the app navigated to it (HomeServicesBottomSheet already points at
    // Screen.UksVisit below), so it was removed rather than guarded.
    composable(Screen.UksVisit.route) {
        val viewModel: UksViewModel = hiltViewModel()
        RoleGuardedScreen(
            currentRole = userRole,
            allowedRoles = listOf("petugas_uks", "uks", "teacher", "guru", "admin", "superadmin"),
            featureTitle = "Kunjungan & Layanan UKS",
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = navigateToRoleHome
        ) {
            UksVisitScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHealthHistory = { navController.navigate(Screen.HealthHistory.createRoute(null)) }
            )
        }
    }

    composable(
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
    composable(Screen.GamificationDashboard.route) {
        val viewModel: GamificationViewModel = hiltViewModel()
        GamificationDashboardScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
            onNavigateToBadges = { navController.navigate(Screen.BadgeCollection.route) }
        )
    }

    composable(Screen.Leaderboard.route) {
        val viewModel: GamificationViewModel = hiltViewModel()
        LeaderboardScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.BadgeCollection.route) {
        val viewModel: GamificationViewModel = hiltViewModel()
        BadgeCollectionScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
