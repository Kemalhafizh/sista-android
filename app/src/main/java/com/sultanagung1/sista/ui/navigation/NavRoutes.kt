package com.sultanagung1.sista.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-Safe Navigation Route Definitions (FASE 53.1).
 * Menggunakan Kotlin Serialization untuk Navigation Compose 2.8+.
 * Menghilangkan kebutuhan manual URL encoding dan string concatenation.
 */
sealed interface SulaoneRoute

// --- Auth & Onboarding ---
@Serializable
data object LoginRoute : SulaoneRoute

@Serializable
data object HomeRoute : SulaoneRoute

// --- Attendance & Biometrics ---
@Serializable
data object GeofenceAttendanceRoute : SulaoneRoute

@Serializable
data object DynamicQrRoute : SulaoneRoute

@Serializable
data object FaceBiometricRoute : SulaoneRoute

@Serializable
data object QrScannerRoute : SulaoneRoute

@Serializable
data object FaceEnrollmentRoute : SulaoneRoute

// --- Academic & LMS ---
@Serializable
data object ScheduleRoute : SulaoneRoute

@Serializable
data object GradesRoute : SulaoneRoute

@Serializable
data object RaporDetailRoute : SulaoneRoute

@Serializable
data object ElearningClassListRoute : SulaoneRoute

@Serializable
data class ElearningClassDetailRoute(val classId: Long) : SulaoneRoute

@Serializable
data class AssignmentSubmitRoute(val assignmentId: Long) : SulaoneRoute

@Serializable
data object AcademicCalendarRoute : SulaoneRoute

@Serializable
data class EventDetailRoute(val eventId: String) : SulaoneRoute

@Serializable
data object AcademicAnalyticsRoute : SulaoneRoute

@Serializable
data object ClassAnalyticsRoute : SulaoneRoute

// --- CBT Engine ---
@Serializable
data object CbtListRoute : SulaoneRoute

@Serializable
data class CbtTokenEntryRoute(
    val examId: Long,
    val examTitle: String,
    val examSubject: String,
    val examType: String,
    val duration: Int,
    val totalQuestions: Int
) : SulaoneRoute

@Serializable
data class CbtRoomRoute(val examId: Long) : SulaoneRoute

// --- Teacher Management ---
@Serializable
data object TeacherDashboardRoute : SulaoneRoute

@Serializable
data class TeacherAttendanceRoute(
    val scheduleId: String,
    val className: String
) : SulaoneRoute

@Serializable
data class TeacherJournalRoute(
    val scheduleId: String,
    val className: String,
    val subjectName: String
) : SulaoneRoute

@Serializable
data class TeacherProctorRoute(val examId: Long) : SulaoneRoute

@Serializable
data object TeacherCreateExamRoute : SulaoneRoute

@Serializable
data object QuestionBankRoute : SulaoneRoute

@Serializable
data object AutoGenerateExamRoute : SulaoneRoute

@Serializable
data object DailyAssessmentListRoute : SulaoneRoute

@Serializable
data class ScoreInputRoute(val assessmentId: Long) : SulaoneRoute

@Serializable
data object RemedialListRoute : SulaoneRoute

@Serializable
data object TeachingJournalRoute : SulaoneRoute

@Serializable
data object TeachingJournalMobileRoute : SulaoneRoute

@Serializable
data class JournalFormRoute(val scheduleId: String) : SulaoneRoute

// --- Parent Portal ---
@Serializable
data object ParentDashboardRoute : SulaoneRoute

@Serializable
data class ChildDetailRoute(val studentId: String) : SulaoneRoute

@Serializable
data object ChildProgressRoute : SulaoneRoute

@Serializable
data object ChildActivityFeedRoute : SulaoneRoute

// --- Admin / Executive ---
@Serializable
data object AdminDashboardRoute : SulaoneRoute

@Serializable
data object ExecutiveAnalyticsRoute : SulaoneRoute

// --- Communication & Chat ---
@Serializable
data object ConversationListRoute : SulaoneRoute

@Serializable
data class ChatRoute(val conversationId: String) : SulaoneRoute

@Serializable
data object AnnouncementFeedRoute : SulaoneRoute

@Serializable
data class AnnouncementDetailRoute(val id: String) : SulaoneRoute

@Serializable
data object NotificationCenterRoute : SulaoneRoute

@Serializable
data object NotificationSettingsRoute : SulaoneRoute

// --- Ibadah, Islamic & Character ---
@Serializable
data object MutabaahRoute : SulaoneRoute

@Serializable
data object TahsinRecorderRoute : SulaoneRoute

@Serializable
data object PrayerTimesRoute : SulaoneRoute

// --- AI Tutor ---
@Serializable
data object AiTutorRoute : SulaoneRoute

@Serializable
data object AiEssayGraderRoute : SulaoneRoute

// --- Finance, Library, Counseling, Hardware ---
@Serializable
data object BillingRoute : SulaoneRoute

@Serializable
data object DigitalLibraryRoute : SulaoneRoute

@Serializable
data object LibraryCatalogRoute : SulaoneRoute

@Serializable
data object CounselingRoute : SulaoneRoute

@Serializable
data object CounselingDashboardRoute : SulaoneRoute

@Serializable
data class CounselingSessionFormRoute(val studentId: Long? = 0L) : SulaoneRoute

@Serializable
data object StudentCounselingRoute : SulaoneRoute

@Serializable
data object AntiBullyingSosRoute : SulaoneRoute

@Serializable
data object BlockchainPassportRoute : SulaoneRoute

@Serializable
data object EnterpriseCatalogRoute : SulaoneRoute

@Serializable
data object PdfViewerRoute : SulaoneRoute

@Serializable
data object DownloadHistoryRoute : SulaoneRoute

@Serializable
data object DocumentScannerRoute : SulaoneRoute

@Serializable
data object DigitalSignatureRoute : SulaoneRoute

// --- Settings & Profile ---
@Serializable
data object ProfileRoute : SulaoneRoute

@Serializable
data class StudentProfileComprehensiveRoute(val studentId: Long? = 0L) : SulaoneRoute

@Serializable
data object SettingsRoute : SulaoneRoute

@Serializable
data object LanguageSettingsRoute : SulaoneRoute

@Serializable
data object AccessibilitySettingsRoute : SulaoneRoute

@Serializable
data object SecuritySettingsRoute : SulaoneRoute

@Serializable
data object LiteModeSettingsRoute : SulaoneRoute

// --- Modules & Kesiswaan ---
@Serializable
data object DisciplineRoute : SulaoneRoute

@Serializable
data object UtbkTryoutRoute : SulaoneRoute

@Serializable
data object ExtracurricularRoute : SulaoneRoute

@Serializable
data object AchievementUploadRoute : SulaoneRoute

@Serializable
data object TeacherEvaluationRoute : SulaoneRoute

@Serializable
data object SpmbMobileRoute : SulaoneRoute

@Serializable
data object SpmbInfoRoute : SulaoneRoute

@Serializable
data object SpmbRegistrationRoute : SulaoneRoute

@Serializable
data class SpmbTrackingRoute(val regNumber: String? = null) : SulaoneRoute

@Serializable
data object UksDigitalRoute : SulaoneRoute

@Serializable
data object UksVisitRoute : SulaoneRoute

@Serializable
data class HealthHistoryRoute(val studentId: String? = null) : SulaoneRoute

// --- Gamification ---
@Serializable
data object GamificationDashboardRoute : SulaoneRoute

@Serializable
data object LeaderboardRoute : SulaoneRoute

@Serializable
data object BadgeCollectionRoute : SulaoneRoute

// --- Portal & Updates ---
@Serializable
data object SsoWebViewRoute : SulaoneRoute

@Serializable
data object ModuleFavoritesRoute : SulaoneRoute

@Serializable
data object InAppUpdateRoute : SulaoneRoute
