package com.sultanagung1.sista.core.notification

import android.net.Uri
import com.sultanagung1.sista.ui.navigation.Screen

object NotificationRouter {

    fun parseDeepLink(uri: Uri?): String? {
        if (uri == null) return null

        val host = uri.host ?: return null
        val segments = uri.pathSegments

        return when (host) {
            "child_detail" -> {
                val studentId = segments.getOrNull(0) ?: "c1"
                Screen.ChildDetail.createRoute(studentId)
            }
            "chat" -> {
                val conversationId = segments.getOrNull(0) ?: "1"
                "chat_room/$conversationId"
            }
            // An exam is only ever entered through its token.
            "cbt_room" -> segments.getOrNull(0)?.toLongOrNull()
                ?.let { Screen.CbtTokenEntry.createRoute(it) } ?: Screen.CbtList.route
            "announcements" -> "announcement_feed"
            "billing" -> Screen.Billing.route
            "grades" -> Screen.Grades.route
            "schedule" -> Screen.Schedule.route
            else -> null
        }
    }
}
