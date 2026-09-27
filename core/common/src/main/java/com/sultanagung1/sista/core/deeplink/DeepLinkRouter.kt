package com.sultanagung1.sista.core.deeplink

import android.net.Uri
import com.sultanagung1.sista.ui.navigation.Screen

object DeepLinkRouter {

    fun resolveRoute(uri: Uri?): String? {
        if (uri == null) return null

        val scheme = uri.scheme
        val host = uri.host ?: ""
        val path = uri.path ?: ""

        // Handle sulaone:// scheme
        if (scheme == "sulaone") {
            return when (host) {
                "attendance" -> Screen.GeofenceAttendance.route
                "scanner", "qr" -> Screen.QrScanner.route
                "cbt" -> {
                    val examId = uri.lastPathSegment
                    if (examId != null && examId != "cbt") {
                        Screen.CbtRoom.createRoute(examId.toLongOrNull() ?: 1L)
                    } else {
                        Screen.CbtList.route
                    }
                }
                "billing", "spp" -> Screen.Billing.route
                "schedule" -> Screen.Schedule.route
                // FASE 77.7: class-session pushes (sesi dimulai, pengingat jadwal).
                "class-session" -> classSessionRoute(uri.lastPathSegment)
                "grades", "rapor" -> Screen.PdfViewer.route
                "pdf" -> Screen.PdfViewer.route
                "sos", "emergency" -> Screen.AntiBullyingSos.route
                "mutabaah" -> Screen.Mutabaah.route
                "tahsin" -> Screen.TahsinRecorder.route
                "announcement" -> {
                    val id = uri.lastPathSegment
                    if (id != null && id != "announcement") {
                        Screen.AnnouncementDetail.createRoute(id)
                    } else {
                        Screen.AnnouncementFeed.route
                    }
                }
                "chat" -> {
                    val id = uri.lastPathSegment
                    if (id != null && id != "chat") {
                        Screen.Chat.createRoute(id)
                    } else {
                        Screen.ConversationList.route
                    }
                }
                "settings" -> Screen.Settings.route
                "diagnostics" -> Screen.DiagnosticReport.route
                "profile" -> Screen.Profile.route
                "biometrics", "face_enrollment" -> Screen.FaceEnrollment.route
                else -> null
            }
        }

        // Handle https://sista.sultanagung1.sch.id/app/* Web Links
        val isTrustedHost = host.equals("sista.sultanagung1.sch.id", ignoreCase = true) ||
                host.equals("api.sultanagung1.sch.id", ignoreCase = true) ||
                (host.endsWith(".sultanagung1.sch.id", ignoreCase = true) && !host.contains("@"))
        if (scheme == "https" && isTrustedHost) {
            return when {
                path.startsWith("/app/attendance") -> Screen.GeofenceAttendance.route
                path.startsWith("/app/cbt") -> Screen.CbtList.route
                path.startsWith("/app/billing") -> Screen.Billing.route
                path.startsWith("/app/rapor") -> Screen.PdfViewer.route
                path.startsWith("/app/announcement") -> Screen.AnnouncementFeed.route
                else -> null
            }
        }

        return null
    }

    /**
     * `sulaone://class-session/scan` (student: scan the teacher's QR),
     * `…/teach` (teacher: today's sessions), `…/manage` (admin/Waka/TU),
     * `…/active/{sessionId}` (teacher: open that running session).
     */
    fun classSessionRoute(lastSegment: String?): String? = when (lastSegment) {
        "scan" -> Screen.StudentSessionQrScan.route
        "teach" -> Screen.TeacherTodaySessions.route
        "manage" -> Screen.AdminSessionManagement.route
        else -> lastSegment?.toLongOrNull()?.let { Screen.TeacherActiveSession.createRoute(it) }
    }
}
