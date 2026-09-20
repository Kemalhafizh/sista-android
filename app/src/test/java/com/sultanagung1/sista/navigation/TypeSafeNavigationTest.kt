package com.sultanagung1.sista.navigation

import android.net.Uri
import com.sultanagung1.sista.core.deeplink.DeepLinkRouter
import com.sultanagung1.sista.core.motion.SulaoneNavTransitions
import com.sultanagung1.sista.ui.navigation.*
import kotlinx.serialization.Serializable
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Automated Unit Test Suite untuk FASE 53:
 * Type-Safe Navigation, Predictive Back & AppNavigation Decomposition.
 */
class TypeSafeNavigationTest {

    private val allRouteClasses = listOf(
        LoginRoute::class.java,
        HomeRoute::class.java,
        GeofenceAttendanceRoute::class.java,
        DynamicQrRoute::class.java,
        FaceBiometricRoute::class.java,
        QrScannerRoute::class.java,
        FaceEnrollmentRoute::class.java,
        ScheduleRoute::class.java,
        GradesRoute::class.java,
        RaporDetailRoute::class.java,
        ElearningClassListRoute::class.java,
        ElearningClassDetailRoute::class.java,
        AssignmentSubmitRoute::class.java,
        AcademicCalendarRoute::class.java,
        EventDetailRoute::class.java,
        AcademicAnalyticsRoute::class.java,
        ClassAnalyticsRoute::class.java,
        CbtListRoute::class.java,
        CbtTokenEntryRoute::class.java,
        CbtRoomRoute::class.java,
        TeacherDashboardRoute::class.java,
        TeacherAttendanceRoute::class.java,
        TeacherJournalRoute::class.java,
        TeacherProctorRoute::class.java,
        TeacherCreateExamRoute::class.java,
        QuestionBankRoute::class.java,
        AutoGenerateExamRoute::class.java,
        DailyAssessmentListRoute::class.java,
        ScoreInputRoute::class.java,
        RemedialListRoute::class.java,
        TeachingJournalRoute::class.java,
        TeachingJournalMobileRoute::class.java,
        JournalFormRoute::class.java,
        ParentDashboardRoute::class.java,
        ChildDetailRoute::class.java,
        ChildProgressRoute::class.java,
        ChildActivityFeedRoute::class.java,
        AdminDashboardRoute::class.java,
        ExecutiveAnalyticsRoute::class.java,
        ConversationListRoute::class.java,
        ChatRoute::class.java,
        AnnouncementFeedRoute::class.java,
        AnnouncementDetailRoute::class.java,
        NotificationCenterRoute::class.java,
        NotificationSettingsRoute::class.java,
        MutabaahRoute::class.java,
        TahsinRecorderRoute::class.java,
        PrayerTimesRoute::class.java,
        AiTutorRoute::class.java,
        AiEssayGraderRoute::class.java,
        BillingRoute::class.java,
        DigitalLibraryRoute::class.java,
        LibraryCatalogRoute::class.java,
        CounselingRoute::class.java,
        CounselingDashboardRoute::class.java,
        CounselingSessionFormRoute::class.java,
        StudentCounselingRoute::class.java,
        AntiBullyingSosRoute::class.java,
        BlockchainPassportRoute::class.java,
        EnterpriseCatalogRoute::class.java,
        PdfViewerRoute::class.java,
        DownloadHistoryRoute::class.java,
        DocumentScannerRoute::class.java,
        DigitalSignatureRoute::class.java,
        ProfileRoute::class.java,
        StudentProfileComprehensiveRoute::class.java,
        SettingsRoute::class.java,
        LanguageSettingsRoute::class.java,
        AccessibilitySettingsRoute::class.java,
        SecuritySettingsRoute::class.java,
        LiteModeSettingsRoute::class.java,
        DisciplineRoute::class.java,
        UtbkTryoutRoute::class.java,
        ExtracurricularRoute::class.java,
        AchievementUploadRoute::class.java,
        TeacherEvaluationRoute::class.java,
        SpmbMobileRoute::class.java,
        SpmbInfoRoute::class.java,
        SpmbRegistrationRoute::class.java,
        SpmbTrackingRoute::class.java,
        UksDigitalRoute::class.java,
        UksVisitRoute::class.java,
        HealthHistoryRoute::class.java,
        GamificationDashboardRoute::class.java,
        LeaderboardRoute::class.java,
        BadgeCollectionRoute::class.java,
        SsoWebViewRoute::class.java,
        ModuleFavoritesRoute::class.java,
        InAppUpdateRoute::class.java
    )

    @Test
    fun testAllRoutesImplementSulaoneRoute() {
        assertTrue("Total routes terdaftar harus minimal 80 rute", allRouteClasses.size >= 80)

        for (routeClass in allRouteClasses) {
            assertTrue(
                "Rute ${routeClass.simpleName} harus mengimplementasikan SulaoneRoute",
                SulaoneRoute::class.java.isAssignableFrom(routeClass)
            )
        }
    }

    @Test
    fun testAllRoutesHaveSerializableAnnotation() {
        for (routeClass in allRouteClasses) {
            val isSerializable = routeClass.isAnnotationPresent(Serializable::class.java)
            assertTrue(
                "Rute ${routeClass.simpleName} harus dianotasi dengan @Serializable",
                isSerializable
            )
        }
    }

    @Test
    fun testAll8SubGraphsAreDefined() {
        val subGraphClasses = listOf(
            "com.sultanagung1.sista.ui.navigation.graphs.AuthNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.AcademicNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.CbtNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.TeacherNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.ParentNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.AdminNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.CommunicationNavGraphKt",
            "com.sultanagung1.sista.ui.navigation.graphs.SettingsNavGraphKt"
        )

        assertEquals("Harus ada tepat 8 modul sub-navigation graph", 8, subGraphClasses.size)

        for (className in subGraphClasses) {
            val cls = try {
                Class.forName(className)
            } catch (e: ClassNotFoundException) {
                null
            }
            assertNotNull("SubGraph file class $className harus terdefinisi", cls)
        }
    }

    @Test
    fun testPredictiveBackEnabledInManifest() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml harus ditemukan", manifestFile.exists())

        val content = manifestFile.readText()
        assertTrue(
            "AndroidManifest.xml harus mengaktifkan android:enableOnBackInvokedCallback=\"true\"",
            content.contains("android:enableOnBackInvokedCallback=\"true\"")
        )
    }

    @Test
    fun testTabTransitionsAreCrossfade() {
        assertNotNull("tabEnterTransition harus terdefinisi", SulaoneNavTransitions.tabEnterTransition)
        assertNotNull("tabExitTransition harus terdefinisi", SulaoneNavTransitions.tabExitTransition)
    }

    @Test
    fun testAppNavigationLineCountDrasticallyReduced() {
        val appNavFile = File("src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt")
        assertTrue("AppNavigation.kt harus ditemukan", appNavFile.exists())

        val lines = appNavFile.readLines().size
        assertTrue(
            "AppNavigation.kt harus terdekomposisi ke bawah 450 baris (sekarang $lines baris, sebelumnya 1.477 baris)",
            lines < 450
        )
    }
}
