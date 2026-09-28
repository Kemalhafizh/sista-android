package com.sultanagung1.sista.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AppRegistration
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.FaceRetouchingNatural
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

/** The icon each server feature is drawn with. Unknown keys get a neutral one. */
object FeatureIcons {

    private val BY_KEY: Map<String, ImageVector> = mapOf(
        "student.schedule" to Icons.Outlined.CalendarMonth,
        "student.grades" to Icons.Outlined.Grade,
        "student.rapor" to Icons.Outlined.Assignment,
        "student.remedials" to Icons.Outlined.Rule,
        "student.cbt" to Icons.Outlined.Quiz,
        "student.elearning" to Icons.Outlined.Class,
        "student.analytics" to Icons.Outlined.Insights,
        "student.utbk" to Icons.Outlined.School,
        "student.ai_tutor" to Icons.Outlined.AutoAwesome,
        "student.ai_essay" to Icons.Outlined.EditNote,
        "library" to Icons.AutoMirrored.Outlined.MenuBook,
        "calendar" to Icons.Outlined.EventNote,
        "question_bank" to Icons.Outlined.Quiz,
        "class_analytics" to Icons.Outlined.BarChart,
        "attendance.gps" to Icons.Outlined.LocationOn,
        "attendance.class_scan" to Icons.Outlined.QrCodeScanner,
        "attendance.history" to Icons.Outlined.History,
        "attendance.id_qr" to Icons.Outlined.QrCode2,
        "attendance.face_enroll" to Icons.Outlined.FaceRetouchingNatural,
        "scanner.identity" to Icons.Outlined.QrCodeScanner,
        "scanner.event" to Icons.Outlined.AppRegistration,
        "student.mutabaah" to Icons.Outlined.SelfImprovement,
        "student.tahfidz" to Icons.AutoMirrored.Outlined.MenuBook,
        "tahsin.record" to Icons.Outlined.Mic,
        "tahsin.review" to Icons.Outlined.RecordVoiceOver,
        "student.billing" to Icons.Outlined.AccountBalanceWallet,
        "discipline" to Icons.Outlined.Gavel,
        "counseling.student" to Icons.Outlined.SupportAgent,
        "health.history" to Icons.Outlined.HealthAndSafety,
        "student.profile360" to Icons.Outlined.PersonSearch,
        "extracurricular" to Icons.Outlined.Groups,
        "achievements" to Icons.Outlined.EmojiEvents,
        "student.evaluations" to Icons.Outlined.HowToVote,
        "student.gamification" to Icons.Outlined.MilitaryTech,
        "student.credentials" to Icons.Outlined.WorkspacePremium,
        "activity.feed" to Icons.Outlined.Timeline,
        "teacher.sessions" to Icons.Outlined.CoPresent,
        "teacher.schedule" to Icons.Outlined.CalendarMonth,
        "teacher.classes" to Icons.Outlined.CheckCircle,
        "teacher.journal" to Icons.Outlined.EditNote,
        "teacher.assessments" to Icons.Outlined.Grade,
        "teacher.elearning" to Icons.Outlined.Class,
        "teacher.cbt" to Icons.Outlined.Quiz,
        "teacher.messages" to Icons.AutoMirrored.Outlined.Chat,
        "counseling.staff" to Icons.Outlined.Psychology,
        "uks.record" to Icons.Outlined.LocalHospital,
        "parent.children" to Icons.Outlined.FamilyRestroom,
        "parent.progress" to Icons.AutoMirrored.Outlined.TrendingUp,
        "parent.messages" to Icons.AutoMirrored.Outlined.Chat,
        "admin.dashboard" to Icons.Outlined.AdminPanelSettings,
        "admin.approvals" to Icons.Outlined.VerifiedUser,
        "admin.broadcast" to Icons.Outlined.Campaign,
        "admin.executive" to Icons.Outlined.Insights,
        "admin.class_sessions" to Icons.Outlined.CoPresent,
        "announcements" to Icons.Outlined.Campaign,
        "notifications" to Icons.Outlined.Notifications,
        "documents.scan" to Icons.Outlined.DocumentScanner,
        "documents.signature" to Icons.Outlined.Draw,
    )

    fun of(key: String): ImageVector = BY_KEY[key] ?: Icons.Outlined.Apps

    /** Hardware hint icons, in the order the server lists them. */
    fun hint(hardware: String): ImageVector? = when (hardware) {
        "camera" -> Icons.Outlined.CameraAlt
        "location" -> Icons.Outlined.LocationOn
        "microphone" -> Icons.Outlined.Mic
        "biometric" -> Icons.Outlined.Badge
        else -> null
    }

    fun hintLabel(hardware: String): String? = when (hardware) {
        "camera" -> "kamera"
        "location" -> "lokasi"
        "microphone" -> "mikrofon"
        "biometric" -> "biometrik"
        else -> null
    }
}
