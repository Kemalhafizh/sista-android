package com.sultanagung1.sista.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String, val title: String = "") {
    object Login : Screen("login", "Masuk")
    object Home : Screen("home", "Beranda")
    /** Every feature this account has, grouped: the same screen for every role. */
    object ServicesHub : Screen("layanan", "Layanan")
    
    // Attendance & Hardware
    object GeofenceAttendance : Screen("geofence_attendance", "Presensi GPS")
    object DynamicQr : Screen("dynamic_qr", "QR Presensi")
    object FaceBiometric : Screen("face_biometric", "Face Biometric")
    object QrScanner : Screen("qr_scanner", "Pemindai Kamera Multi-Mode")
    object FaceEnrollment : Screen("face_enrollment", "Keamanan Sidik Jari (m-Banking)")

    // Academic & LMS
    object Schedule : Screen("schedule", "Jadwal Pelajaran")
    object Grades : Screen("grades", "Rapor & Nilai")
    
    // CBT Anti-Cheat
    object CbtList : Screen("cbt_list", "Ujian CBT")
    object CbtTokenEntry : Screen("cbt_token/{examId}/{examTitle}/{examSubject}/{examType}/{duration}/{totalQuestions}", "Token Masuk Ujian") {
        fun createRoute(examId: Long, title: String, subject: String, type: String, duration: Int, totalQuestions: Int): String {
            val encTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8.toString())
            return "cbt_token/$examId/$encTitle/$encSubject/$type/$duration/$totalQuestions"
        }
    }
    object CbtRoom : Screen("cbt_room/{examId}?studentId={studentId}&maxViolations={maxViolations}", "Ruang Ujian") {
        fun createRoute(examId: Long, studentId: Long? = null, maxViolations: Int? = null) =
            "cbt_room/$examId?studentId=${studentId ?: -1L}&maxViolations=${maxViolations ?: -1}"
    }

    // Ibadah & Character
    object Mutabaah : Screen("mutabaah", "Mutabaah Yaumiyah")
    object TahsinRecorder : Screen("tahsin_recorder", "Setoran Tahfidz")
    object TahsinHistory : Screen("tahsin_history", "Riwayat Setoran Tahsin")
    object TahsinTeacherReview : Screen("tahsin_teacher_review", "Evaluasi Setoran Tahsin")
    object TahsinSubmissionDetail : Screen("tahsin_submission/{submissionId}?teacherMode={teacherMode}", "Detail Setoran Tahsin") {
        fun createRoute(submissionId: Long, teacherMode: Boolean) = "tahsin_submission/$submissionId?teacherMode=$teacherMode"
    }

    // AI Tutor
    object AiTutor : Screen("ai_tutor", "Sultan AI Tutor")
    object AiEssayGrader : Screen("ai_essay_grader", "Koreksi Esai AI")

    // Finance & Library & Counseling
    object Billing : Screen("billing", "Tagihan SPP")
    object AntiBullyingSos : Screen("antibullying_sos", "Tombol Panik SOS")

    // Teacher Screens
    object TeacherDashboard : Screen("teacher_dashboard", "Dashboard Guru")
    object TeacherAttendance : Screen("teacher_attendance/{classroomId}/{scheduleId}/{className}", "Presensi Kelas") {
        fun createRoute(classroomId: Long, scheduleId: Long, className: String): String {
            val encClass = URLEncoder.encode(className, StandardCharsets.UTF_8.toString())
            return "teacher_attendance/$classroomId/$scheduleId/$encClass"
        }
    }

    // Parent Screens
    object ParentDashboard : Screen("parent_dashboard", "Portal Wali Murid")
    object ChildDetail : Screen("child_detail/{studentId}", "Detail Perkembangan Anak") {
        fun createRoute(studentId: String) = "child_detail/$studentId"
    }

    // Admin / Principal Screen
    object AdminDashboard : Screen("admin_dashboard", "Executive Command Center")

    // Real-Time Chat & Communication
    object ConversationList : Screen("conversation_list", "Pesan & Konsultasi")
    object Chat : Screen("chat/{conversationId}", "Ruang Konsultasi") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }

    // Live Announcements & Notifications
    object AnnouncementFeed : Screen("announcement_feed", "Pengumuman Sekolah")
    object AnnouncementDetail : Screen("announcement_detail/{id}", "Rincian Pengumuman") {
        fun createRoute(id: String) = "announcement_detail/$id"
    }
    object NotificationCenter : Screen("notification_center", "Pusat Notifikasi")

    object EnterpriseCatalog : Screen("enterprise_catalog", "Direktori Enterprise")

    // Documents & PDF Management
    object PdfViewer : Screen("pdf_viewer?fileUrl={fileUrl}&title={title}&sizeBytes={sizeBytes}", "Rapor & Dokumen PDF") {
        fun createRoute(fileUrl: String, title: String, sizeBytes: Long): String {
            val encodedUrl = java.net.URLEncoder.encode(fileUrl, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            return "pdf_viewer?fileUrl=$encodedUrl&title=$encodedTitle&sizeBytes=$sizeBytes"
        }
    }
    object DownloadHistory : Screen("download_history", "Manajer Unduhan")
    object DocumentScanner : Screen("document_scanner", "Pemindai Dokumen OCR")
    object DigitalSignature : Screen("digital_signature", "Tanda Tangan Digital")

    // Analytics & KPI Dashboards
    object AcademicAnalytics : Screen("academic_analytics", "Analitik Siswa")
    object ClassAnalytics : Screen("class_analytics", "Analitik Kelas")
    object ChildProgress : Screen("child_progress", "Pantau Progres Anak")
    object ExecutiveAnalytics : Screen("executive_analytics", "KPI Eksekutif")

    // SuperApp Marketplace, SSO & Updates
    object SsoWebView : Screen("sso_webview", "Portal Web SSO")
    object ModuleFavorites : Screen("module_favorites", "Modul Favorit")
    object InAppUpdate : Screen("in_app_update", "Pembaruan Aplikasi")

    // User Profile & Settings
    object Profile : Screen("profile", "Profil Saya")
    object Settings : Screen("settings", "Pengaturan SuperApp")
    object LanguageSettings : Screen("language_settings", "Pengaturan Bahasa")
    object AccessibilitySettings : Screen("accessibility_settings", "Aksesibilitas & Font")
    object SecuritySettings : Screen("security_settings", "Keamanan & Biometrik")

    // Realistic Roadmap (Fase 18 - 24)
    // studentUuid: which child a parent is looking at. Without it a parent
    // with several children always got the first one, whatever child was
    // selected on the dashboard. Always navigate via createRoute(), never
    // `.route` (that would pass the literal "{studentUuid}").
    object Discipline : Screen("discipline?studentUuid={studentUuid}", "Tata Tertib & Poin") {
        fun createRoute(studentUuid: String? = null) =
            if (!studentUuid.isNullOrBlank()) "discipline?studentUuid=$studentUuid" else "discipline"
    }
    object UtbkTryout : Screen("utbk_tryout", "Simulasi UTBK & Analisis")
    object LibraryCatalog : Screen("library_catalog", "E-Pustaka Pintar")
    object Extracurricular : Screen("extracurricular", "Ekstrakurikuler & OSIS")
    object AchievementUpload : Screen("achievement_upload", "Portofolio Prestasi")
    object TeacherEvaluation : Screen("teacher_evaluation", "Evaluasi Guru & E-Voting")
    object LiteModeSettings : Screen("lite_mode_settings", "Mode Hemat Kuota")

    // Teacher CBT Online Proctoring & Exam Creation
    object TeacherProctor : Screen("teacher_proctor/{examId}", "Pengawas Ujian Daring") {
        fun createRoute(examId: Long) = "teacher_proctor/$examId"
    }
    object TeacherProctorExams : Screen("teacher_proctor_exams", "Pilih Ujian untuk Diawasi")
    object TeacherCreateExam : Screen("teacher_create_exam", "Buat Ulangan Daring")

    // Fase 29: Bank Soal & Auto-Generate Ujian
    object QuestionBank : Screen("question_bank", "Bank Soal Terpusat")
    object AutoGenerateExam : Screen("auto_generate_exam", "Auto-Generate Ujian")

    // Fase 30: E-Rapor Kurikulum Merdeka
    object RaporDetail : Screen("rapor_detail", "Rapor Kurikulum Merdeka")

    // Fase 32: Penilaian Harian & Remedial
    object DailyAssessmentList : Screen("daily_assessment_list", "Penilaian Harian")
    object ScoreInput : Screen("score_input/{assessmentId}", "Input Nilai Siswa") {
        fun createRoute(id: Long) = "score_input/$id"
    }
    object RemedialList : Screen("remedial_list", "Tanggungan Remedial")

    // Fase 31: E-Learning Mobile LMS
    object ElearningClassList : Screen("elearning_class_list", "E-Learning & Ruang Kelas")
    object ElearningClassDetail : Screen("elearning_class_detail/{classId}", "Detail Ruang Kelas") {
        fun createRoute(classId: Long) = "elearning_class_detail/$classId"
    }
    object AssignmentSubmit : Screen("assignment_submit/{assignmentId}", "Kumpulkan Tugas") {
        fun createRoute(assignmentId: Long) = "assignment_submit/$assignmentId"
    }

    // Fase 33: Bimbingan Konseling (BK) Mobile & At-Risk
    object CounselingDashboard : Screen("counseling_dashboard", "Bimbingan Konseling BK")
    object CounselingSessionForm : Screen("counseling_session_form?studentId={studentId}", "Catat Sesi Konseling") {
        fun createRoute(studentId: Long? = null) = "counseling_session_form?studentId=${studentId ?: 0}"
    }
    object StudentCounseling : Screen("student_counseling", "Konsultasi Mandiri Siswa")

    // Fase 36: Profil Komprehensif Siswa 360 Derajat
    object StudentProfileComprehensive : Screen("student_profile_comprehensive?studentId={studentId}", "Profil Siswa 360°") {
        fun createRoute(studentId: Long? = null) = "student_profile_comprehensive?studentId=${studentId ?: 0}"
    }

    // Fase 34: Kalender Pendidikan & Event Sekolah
    object AcademicCalendar : Screen("academic_calendar", "Kalender Pendidikan")
    object EventDetail : Screen("event_detail/{eventId}", "Detail Agenda Sekolah") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    // Fase 35: SPMB / PPDB Mobile
    object SpmbMobile : Screen("spmb_mobile", "SPMB & PPDB")
    object SpmbInfo : Screen("spmb_info", "Informasi SPMB")
    object SpmbRegistration : Screen("spmb_registration", "Pendaftaran Calon Siswa")
    object SpmbTracking : Screen("spmb_tracking?regNumber={regNumber}", "Lacak Status SPMB") {
        fun createRoute(regNumber: String? = null) = if (!regNumber.isNullOrBlank()) "spmb_tracking?regNumber=$regNumber" else "spmb_tracking"
    }

    // Fase 37: UKS Digital & Buku Kesehatan Siswa
    object UksVisit : Screen("uks_visit", "Kunjungan UKS & Pelayanan Medis")
    object HealthHistory : Screen("health_history?studentId={studentId}", "Riwayat Medis Siswa") {
        fun createRoute(studentId: String? = null) = if (!studentId.isNullOrBlank()) "health_history?studentId=$studentId" else "health_history"
    }

    // Fase 38: Jurnal Guru & Agenda Mengajar Harian
    object TeachingJournalMobile : Screen("teaching_journal_mobile", "Jurnal KBM Harian")
    object JournalForm : Screen("journal_form/{scheduleId}", "Catat Jurnal Mengajar") {
        fun createRoute(scheduleId: String) = "journal_form/$scheduleId"
    }

    // Fase 39 - 48: SuperApp Next-Gen, Gamifikasi, Notifikasi & Parent Experience
    object GamificationDashboard : Screen("gamification_dashboard", "Gamifikasi & Level")
    object Leaderboard : Screen("leaderboard", "Papan Peringkat Siswa")
    object BadgeCollection : Screen("badge_collection", "Koleksi Lencana & Prestasi")
    object NotificationSettings : Screen("notification_settings", "Preferensi & Saluran Notifikasi")
    // The child is optional so a menu can open the feed; the dashboard passes
    // the chosen child so a parent of two sees the right one.
    object ChildActivityFeed : Screen("child_activity_feed?studentUuid={studentUuid}", "Aktivitas Harian Siswa") {
        fun createRoute(studentUuid: String? = null) =
            if (!studentUuid.isNullOrBlank()) "child_activity_feed?studentUuid=$studentUuid" else "child_activity_feed"
    }

    // Fase 64: Production Observability & Crash Analytics
    object DiagnosticReport : Screen("diagnostic_report", "Pusat Diagnostik & Laporan Kendala")

    // FASE 77: Sesi Kelas Hidup & Presensi Per-Mapel
    object TeacherTodaySessions : Screen("teacher_today_sessions", "Sesi Kelas Hari Ini")
    object TeacherActiveSession : Screen("teacher_active_session/{sessionId}", "Sesi Kelas Aktif") {
        fun createRoute(sessionId: Long) = "teacher_active_session/$sessionId"
    }
    object TeacherAttendanceList : Screen("teacher_attendance_list/{sessionId}", "Daftar Hadir") {
        fun createRoute(sessionId: Long) = "teacher_attendance_list/$sessionId"
    }
    object StudentSessionQrScan : Screen("student_session_qr_scan", "Presensi Kelas")
    object AdminSessionManagement : Screen("admin_session_management", "Manajemen Sesi Kelas")
    object AdminAttendanceOverride : Screen("admin_attendance_override/{sessionId}", "Koreksi Absensi") {
        fun createRoute(sessionId: Long) = "admin_attendance_override/$sessionId"
    }
}


