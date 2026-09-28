package com.sultanagung1.sista.ui.navigation

import com.sultanagung1.sista.data.model.Capabilities
import com.sultanagung1.sista.data.model.CapabilityState
import com.sultanagung1.sista.data.model.FeatureAccess
import com.sultanagung1.sista.data.model.GateDecision

/**
 * Which server feature (config/mobile_features.php on the backend) each
 * screen needs, and which screen opens a feature.
 *
 * Every destination is listed here, either as needing one of a set of
 * features or as open to any signed-in account. The navigation guard reads
 * this for every route, including deep links and notification taps, so a
 * screen the account may not use is never shown (and never calls an API that
 * would only answer 403).
 */
object FeatureCatalog {

    /** Destinations any signed-in account may open. */
    val OPEN: Set<String> = setOf(
        Screen.Login, Screen.ServicesHub, Screen.Profile, Screen.Settings,
        Screen.LanguageSettings, Screen.AccessibilitySettings, Screen.SecuritySettings,
        Screen.LiteModeSettings, Screen.NotificationSettings, Screen.DiagnosticReport,
        Screen.InAppUpdate, Screen.ModuleFavorites, Screen.EnterpriseCatalog,
        Screen.PdfViewer, Screen.DownloadHistory, Screen.SsoWebView,
        // Admissions are public: anyone may look up or register.
        Screen.SpmbMobile, Screen.SpmbInfo, Screen.SpmbRegistration, Screen.SpmbTracking,
    ).map { it.route }.toSet()

    /**
     * Key of a feature no account is given: for screens that exist but do
     * not work yet, so nobody relies on them.
     */
    const val NOT_READY = "not_ready"

    /** Route → the features that open it (any one is enough). */
    val REQUIRES: Map<String, Set<String>> = buildMap {
        fun put(screen: Screen, vararg keys: String) = put(screen.route, keys.toSet())

        // Home screens of each audience
        put(Screen.Home, "student.schedule")
        put(Screen.TeacherDashboard, "teacher.sessions", "teacher.classes")
        put(Screen.ParentDashboard, "parent.children")
        put(Screen.AdminDashboard, "admin.dashboard")

        // Akademik
        put(Screen.Schedule, "student.schedule")
        put(Screen.Grades, "student.grades")
        put(Screen.RaporDetail, "student.rapor", "parent.children")
        put(Screen.RemedialList, "student.remedials", "teacher.assessments")
        put(Screen.CbtList, "student.cbt")
        put(Screen.CbtTokenEntry, "student.cbt")
        put(Screen.CbtRoom, "student.cbt")
        put(Screen.ElearningClassList, "student.elearning", "teacher.elearning")
        put(Screen.ElearningClassDetail, "student.elearning", "teacher.elearning")
        put(Screen.AssignmentSubmit, "student.elearning")
        put(Screen.AcademicAnalytics, "student.analytics")
        put(Screen.UtbkTryout, "student.utbk")
        put(Screen.AiTutor, "student.ai_tutor")
        put(Screen.AiEssayGrader, "student.ai_essay")
        put(Screen.LibraryCatalog, "library")
        put(Screen.AcademicCalendar, "calendar")
        put(Screen.EventDetail, "calendar")
        put(Screen.QuestionBank, "question_bank")
        put(Screen.AutoGenerateExam, "question_bank")
        put(Screen.ClassAnalytics, "class_analytics")

        // Presensi
        put(Screen.GeofenceAttendance, "attendance.gps")
        put(Screen.StudentSessionQrScan, "attendance.class_scan")
        put(Screen.DynamicQr, "attendance.id_qr")
        put(Screen.FaceEnrollment, "attendance.face_enroll")
        put(Screen.FaceBiometric, "attendance.face_enroll")
        put(Screen.QrScanner, "scanner.identity", "scanner.event")

        // Ibadah
        put(Screen.Mutabaah, "student.mutabaah")
        put(Screen.TahsinRecorder, "tahsin.record")
        put(Screen.TahsinHistory, "tahsin.record")
        put(Screen.TahsinSubmissionDetail, "tahsin.record", "tahsin.review")
        put(Screen.TahsinTeacherReview, "tahsin.review")

        // Keuangan
        put(Screen.Billing, "student.billing")

        // Kesiswaan
        put(Screen.Discipline, "discipline")
        put(Screen.StudentCounseling, "counseling.student")
        put(Screen.HealthHistory, "health.history")
        put(Screen.StudentProfileComprehensive, "student.profile360", "parent.children", "counseling.staff", "teacher.classes")
        put(Screen.Extracurricular, "extracurricular")
        put(Screen.AchievementUpload, "achievements")
        put(Screen.TeacherEvaluation, "student.evaluations")
        put(Screen.GamificationDashboard, "student.gamification")
        put(Screen.Leaderboard, "student.gamification")
        put(Screen.BadgeCollection, "student.gamification")
        put(Screen.ChildActivityFeed, "activity.feed")
        // The SOS button only ran a countdown on the phone: nothing reached
        // the school. Kept shut until it sends a real report.
        put(Screen.AntiBullyingSos, NOT_READY)

        // Mengajar
        put(Screen.TeacherTodaySessions, "teacher.sessions")
        put(Screen.TeacherActiveSession, "teacher.sessions")
        put(Screen.TeacherAttendanceList, "teacher.sessions")
        put(Screen.TeacherAttendance, "teacher.classes")
        put(Screen.TeachingJournalMobile, "teacher.journal")
        put(Screen.JournalForm, "teacher.journal")
        put(Screen.DailyAssessmentList, "teacher.assessments")
        put(Screen.ScoreInput, "teacher.assessments")
        put(Screen.TeacherProctorExams, "teacher.cbt")
        put(Screen.TeacherProctor, "teacher.cbt")
        put(Screen.TeacherCreateExam, "teacher.cbt")
        put(Screen.CounselingDashboard, "counseling.staff")
        put(Screen.CounselingSessionForm, "counseling.staff")
        put(Screen.UksVisit, "uks.record")

        // Anak saya
        put(Screen.ChildDetail, "parent.children")
        put(Screen.ChildProgress, "parent.progress")

        // Komunikasi
        put(Screen.ConversationList, "parent.messages", "teacher.messages")
        put(Screen.Chat, "parent.messages", "teacher.messages")
        put(Screen.AnnouncementFeed, "announcements")
        put(Screen.AnnouncementDetail, "announcements")
        put(Screen.NotificationCenter, "notifications")

        // Dokumen
        put(Screen.DocumentScanner, "documents.scan")
        put(Screen.DigitalSignature, "documents.signature")

        // Manajemen
        put(Screen.ExecutiveAnalytics, "admin.executive")
        put(Screen.AdminSessionManagement, "admin.class_sessions")
        put(Screen.AdminAttendanceOverride, "admin.class_sessions")
    }

    /**
     * The screen that opens each feature from a menu. Features without an
     * entry here have no screen in this version of the app and are not listed.
     * Values are navigable routes: never a pattern with a `{placeholder}`.
     */
    val ENTRY: Map<String, String> = mapOf(
        "student.schedule" to Screen.Schedule.route,
        "student.grades" to Screen.Grades.route,
        "student.rapor" to Screen.RaporDetail.route,
        "student.remedials" to Screen.RemedialList.route,
        "student.cbt" to Screen.CbtList.route,
        "student.elearning" to Screen.ElearningClassList.route,
        "student.analytics" to Screen.AcademicAnalytics.route,
        "student.utbk" to Screen.UtbkTryout.route,
        "student.ai_tutor" to Screen.AiTutor.route,
        "student.ai_essay" to Screen.AiEssayGrader.route,
        "library" to Screen.LibraryCatalog.route,
        "calendar" to Screen.AcademicCalendar.route,
        "question_bank" to Screen.QuestionBank.route,
        "class_analytics" to Screen.ClassAnalytics.route,
        "attendance.gps" to Screen.GeofenceAttendance.route,
        "attendance.class_scan" to Screen.StudentSessionQrScan.route,
        "attendance.id_qr" to Screen.DynamicQr.route,
        "attendance.face_enroll" to Screen.FaceEnrollment.route,
        "scanner.identity" to Screen.QrScanner.route,
        "scanner.event" to Screen.QrScanner.route,
        "student.mutabaah" to Screen.Mutabaah.route,
        "tahsin.record" to Screen.TahsinRecorder.route,
        "tahsin.review" to Screen.TahsinTeacherReview.route,
        "student.billing" to Screen.Billing.route,
        "discipline" to Screen.Discipline.createRoute(),
        "counseling.student" to Screen.StudentCounseling.route,
        "health.history" to Screen.HealthHistory.createRoute(),
        "student.profile360" to Screen.StudentProfileComprehensive.createRoute(),
        "extracurricular" to Screen.Extracurricular.route,
        "achievements" to Screen.AchievementUpload.route,
        "student.evaluations" to Screen.TeacherEvaluation.route,
        "student.gamification" to Screen.GamificationDashboard.route,
        "activity.feed" to Screen.ChildActivityFeed.createRoute(),
        "teacher.sessions" to Screen.TeacherTodaySessions.route,
        "teacher.classes" to Screen.TeacherDashboard.route,
        "teacher.journal" to Screen.TeachingJournalMobile.route,
        "teacher.assessments" to Screen.DailyAssessmentList.route,
        "teacher.elearning" to Screen.ElearningClassList.route,
        "teacher.cbt" to Screen.TeacherProctorExams.route,
        "teacher.messages" to Screen.ConversationList.route,
        "counseling.staff" to Screen.CounselingDashboard.route,
        "uks.record" to Screen.UksVisit.route,
        "parent.children" to Screen.ParentDashboard.route,
        "parent.progress" to Screen.ChildProgress.route,
        "parent.messages" to Screen.ConversationList.route,
        "admin.dashboard" to Screen.AdminDashboard.route,
        "admin.executive" to Screen.ExecutiveAnalytics.route,
        "admin.class_sessions" to Screen.AdminSessionManagement.route,
        "announcements" to Screen.AnnouncementFeed.route,
        "notifications" to Screen.NotificationCenter.route,
        "documents.scan" to Screen.DocumentScanner.route,
        "documents.signature" to Screen.DigitalSignature.route,
    )

    /**
     * Which home a Beranda tab opens, most specific audience first. An
     * account with none of these (e.g. a canteen merchant) gets Layanan.
     */
    private val HOME_PREFERENCE = listOf(
        "admin.dashboard" to Screen.AdminDashboard.route,
        "teacher.sessions" to Screen.TeacherDashboard.route,
        "parent.children" to Screen.ParentDashboard.route,
        "student.schedule" to Screen.Home.route,
        "admin.class_sessions" to Screen.AdminSessionManagement.route,
    )

    fun homeRouteFor(capabilities: Capabilities): String {
        val key = FeatureAccess.firstAvailable(capabilities, HOME_PREFERENCE.map { it.first })
        return HOME_PREFERENCE.firstOrNull { it.first == key }?.second ?: Screen.ServicesHub.route
    }

    /** The route pattern without its query part (`discipline?studentUuid={x}` → `discipline`). */
    fun basePattern(route: String): String = route.substringBefore('?')

    /**
     * Features that open [routePattern] (a NavDestination route). Null means
     * any signed-in account; an unknown route needs [NOT_READY], so a screen
     * added without being listed here is shut rather than open.
     */
    fun requiredFor(routePattern: String): Set<String>? {
        val base = basePattern(routePattern)
        if (routePattern in OPEN || base in OPEN.map(::basePattern)) return null
        return REQUIRES[routePattern] ?: REQUIRES.entries.firstOrNull { basePattern(it.key) == base }?.value
            ?: setOf(NOT_READY)
    }

    fun decide(state: CapabilityState, routePattern: String): GateDecision {
        val required = requiredFor(routePattern) ?: return GateDecision.OPEN
        return required
            .map { FeatureAccess.decide(state, it) }
            .let { decisions ->
                when {
                    GateDecision.OPEN in decisions -> GateDecision.OPEN
                    GateDecision.UNKNOWN in decisions -> GateDecision.UNKNOWN
                    GateDecision.WAITING in decisions -> GateDecision.WAITING
                    else -> GateDecision.LOCKED
                }
            }
    }
}
