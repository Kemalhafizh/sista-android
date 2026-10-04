package com.sultanagung1.sista.architecture

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The app is offered in Indonesian, English and Arabic, like the web, so text
 * on screen comes from strings.xml in values, values-en and values-ar
 * (stringResource / UiText), not
 * from a literal in the code. This guard finds literals passed as on-screen
 * text (Text("…"), title = "…", label = "…", contentDescription = "…").
 *
 * [debt] lists the files that still had such text when the guard arrived,
 * with exact counts. Translate a file, then lower or remove its entry: the
 * list only shrinks. Any other file must have none. Data from the server
 * (names, the server's own messages) is not a literal and is not counted.
 *
 * See CLAUDE.md, "Tiga bahasa".
 */
class HardcodedUiTextTest {

    private val debt: Map<String, Int> = mapOf(
        "app/src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt" to 1,
        "app/src/main/java/com/sultanagung1/sista/ui/navigation/FeatureGate.kt" to 4,
        "app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/CbtNavGraph.kt" to 3,
        "app/src/main/java/com/sultanagung1/sista/ui/shell/ServicesHubScreen.kt" to 7,
        "core/common/src/main/java/com/sultanagung1/sista/core/monitoring/PerformanceTracer.kt" to 2,
        "core/common/src/main/java/com/sultanagung1/sista/core/telemetry/AppStartupTracker.kt" to 1,
        "core/common/src/main/java/com/sultanagung1/sista/core/telemetry/NetworkPerformanceInterceptor.kt" to 1,
        "core/common/src/main/java/com/sultanagung1/sista/core/telemetry/SulaoneTelemetryHub.kt" to 5,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneComponents.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneDatePicker.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneDropdown.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneModalBottomSheet.kt" to 1,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneTextField.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneTieredLoading.kt" to 3,
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SwipeActions.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/cbt/components/CbtImageViewer.kt" to 6,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/cbt/components/CbtLatexToolbar.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/common/DraftRestoreDialog.kt" to 4,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/common/EmergencyAlertDialog.kt" to 2,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/common/OfflineQueuedBanner.kt" to 1,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/common/SulaoneExecutiveHeader.kt" to 6,
        "core/designsystem/src/main/java/com/sultanagung1/sista/ui/common/SyncStatusHeader.kt" to 3,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/GradesScreen.kt" to 13,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/RaporDetailScreen.kt" to 10,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/academic/ScheduleScreen.kt" to 8,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/achievement/AchievementUploadScreen.kt" to 20,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/ai/AiTutorScreen.kt" to 12,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/ai/AiViewModel.kt" to 1,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/analytics/AcademicAnalyticsScreen.kt" to 9,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/analytics/AnalyticsUi.kt" to 1,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/analytics/ChildProgressScreen.kt" to 7,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/analytics/ClassAnalyticsScreen.kt" to 15,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/analytics/ExecutiveAnalyticsScreen.kt" to 14,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/announcements/AnnouncementDetailScreen.kt" to 10,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/announcements/AnnouncementFeedScreen.kt" to 6,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/attendance/DynamicQrScreen.kt" to 8,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/attendance/FaceEnrollmentScreen.kt" to 14,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/attendance/GeofenceAttendanceScreen.kt" to 7,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/calendar/AcademicCalendarScreen.kt" to 10,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/calendar/EventDetailScreen.kt" to 11,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/counseling/AntiBullyingSosScreen.kt" to 11,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/counseling/CounselingDashboardScreen.kt" to 8,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/counseling/CounselingSessionFormScreen.kt" to 7,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/counseling/StudentCounselingScreen.kt" to 12,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/discipline/DisciplineScreen.kt" to 27,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/document/DocumentScannerScreen.kt" to 7,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/document/DownloadHistoryScreen.kt" to 5,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/document/PdfViewerScreen.kt" to 2,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/document/SignatureScreen.kt" to 7,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/elearning/AdaptiveElearningScaffold.kt" to 2,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/elearning/AssignmentSubmitScreen.kt" to 5,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/elearning/ElearningClassDetailScreen.kt" to 4,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/elearning/ElearningClassListScreen.kt" to 2,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/evaluation/TeacherEvaluationScreen.kt" to 29,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/extracurricular/ExtracurricularScreen.kt" to 11,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/gamification/BadgeCollectionScreen.kt" to 4,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/gamification/GamificationDashboardScreen.kt" to 9,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/gamification/LeaderboardScreen.kt" to 5,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/library/LibraryCatalogScreen.kt" to 12,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/spmb/SpmbInfoScreen.kt" to 10,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/spmb/SpmbRegistrationScreen.kt" to 28,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/spmb/SpmbTrackingScreen.kt" to 8,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/uks/HealthHistoryScreen.kt" to 15,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/uks/UksVisitScreen.kt" to 28,
        "feature/academic/src/main/java/com/sultanagung1/sista/ui/utbk/UtbkTryOutScreen.kt" to 14,
        "feature/admin/src/main/java/com/sultanagung1/sista/ui/admin/AdminDashboardScreen.kt" to 17,
        "feature/admin/src/main/java/com/sultanagung1/sista/ui/admin/AdminUi.kt" to 9,
        "feature/cbt/src/main/java/com/sultanagung1/sista/ui/cbt/CbtExamListScreen.kt" to 7,
        "feature/cbt/src/main/java/com/sultanagung1/sista/ui/cbt/CbtExamRoomScreen.kt" to 21,
        "feature/cbt/src/main/java/com/sultanagung1/sista/ui/cbt/CbtTokenEntryScreen.kt" to 11,
        "feature/chat/src/main/java/com/sultanagung1/sista/ui/chat/AdaptiveChatScaffold.kt" to 2,
        "feature/chat/src/main/java/com/sultanagung1/sista/ui/chat/ChatScreen.kt" to 7,
        "feature/chat/src/main/java/com/sultanagung1/sista/ui/chat/ConversationListScreen.kt" to 17,
        "feature/finance/src/main/java/com/sultanagung1/sista/ui/finance/BillingScreen.kt" to 32,
        "feature/home/src/main/java/com/sultanagung1/sista/ui/portal/EnterpriseCatalogScreen.kt" to 6,
        "feature/home/src/main/java/com/sultanagung1/sista/ui/portal/ModuleFavoritesScreen.kt" to 2,
        "feature/home/src/main/java/com/sultanagung1/sista/ui/portal/SsoWebViewScreen.kt" to 1,
        "feature/ibadah/src/main/java/com/sultanagung1/sista/ui/ibadah/MutabaahScreen.kt" to 8,
        "feature/ibadah/src/main/java/com/sultanagung1/sista/ui/ibadah/TahsinAudioPlayer.kt" to 1,
        "feature/ibadah/src/main/java/com/sultanagung1/sista/ui/ibadah/TahsinHistoryScreen.kt" to 4,
        "feature/ibadah/src/main/java/com/sultanagung1/sista/ui/ibadah/TahsinRecorderScreen.kt" to 8,
        "feature/ibadah/src/main/java/com/sultanagung1/sista/ui/ibadah/TahsinSubmissionDetailScreen.kt" to 9,
        "feature/ibadah/src/main/java/com/sultanagung1/sista/ui/ibadah/TahsinTeacherReviewListScreen.kt" to 4,
        "feature/profile/src/main/java/com/sultanagung1/sista/ui/profile/DiagnosticReportScreen.kt" to 28,
        "feature/profile/src/main/java/com/sultanagung1/sista/ui/scanner/QrScannerScreen.kt" to 7,
        "feature/profile/src/main/java/com/sultanagung1/sista/ui/scanner/ScanResultHandler.kt" to 5,
        "feature/profile/src/main/java/com/sultanagung1/sista/ui/update/UpdatePromptScreen.kt" to 7,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/AutoGenerateExamScreen.kt" to 12,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/DailyAssessmentScreen.kt" to 7,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/JournalFormScreen.kt" to 12,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/QuestionBankScreen.kt" to 19,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/RemedialScreen.kt" to 9,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/ScoreInputScreen.kt" to 13,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherAttendanceScreen.kt" to 20,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherCreateExamScreen.kt" to 23,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherDashboardScreen.kt" to 12,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherProctorDashboardScreen.kt" to 11,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherProctorExamsScreen.kt" to 6,
        "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeachingJournalMobileScreen.kt" to 6,
    )

    private val pattern = Regex("""(?:\bText\(\s*|\b(?:title|subtitle|label|text|body|message|headline|supporting|actionLabel|contentDescription|placeholder|overline|description|errorTitle)\s*=\s*)"([^"\\]*+(?:\\.[^"\\]*+)*+)"""")

    private val root: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle").exists() || File(it, "settings.gradle.kts").exists() }

    /** On-screen literals in [source]: words, not an animation label like "skeleton". */
    internal fun literals(source: String): List<String> =
        source.replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")
            .lines()
            .filterNot { it.trim().startsWith("//") }
            .flatMap { line -> pattern.findAll(line).map { it.groupValues[1] }.toList() }
            .map { it.replace(Regex("""\$\{[^}]*\}|\$\w+"""), "") }
            .filterNot { Regex("""[a-z][A-Za-z0-9_]*""").matches(it) }
            .filter { Regex("""[A-Za-z]{2,}""").containsMatchIn(it) }

    @Test
    fun onScreenTextComesFromStringResources() {
        val found = (listOf(File(root, "app")) + listOf("core", "feature").flatMap { File(root, it).listFiles()?.toList().orEmpty() })
            .map { File(it, "src/main") }
            .filter { it.isDirectory }
            .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
            .associate { it.relativeTo(root).invariantSeparatorsPath to literals(it.readText()).size }
            .filterValues { it > 0 }

        val problems = found.mapNotNull { (path, count) ->
            val allowed = debt[path] ?: 0
            when {
                count > allowed -> "$path: $count teks tertulis langsung (batas $allowed). Pindahkan ke strings.xml (id, en, ar)."
                count < allowed -> "$path: tinggal $count dari $allowed. Bagus, turunkan angkanya di debt."
                else -> null
            }
        } + debt.keys.filterNot { it in found }.map { "$it: sudah bersih. Hapus entrinya dari debt." }

        assertEquals("Teks layar di luar strings.xml:\n" + problems.joinToString("\n"), emptyList<String>(), problems)
    }

    @Test
    fun theScannerSeesOnScreenTextOnly() {
        val sample = """
            Text("Simpan")
            SistaTopBar(title = "Profil", onBack = {})
            val t = rememberInfiniteTransition(label = "skeleton")
            Text(stringResource(R.string.profile_title))
            // Text("di komentar")
            Text("${'$'}count")
        """.trimIndent()
        assertEquals(listOf("Simpan", "Profil"), literals(sample))
    }
}
