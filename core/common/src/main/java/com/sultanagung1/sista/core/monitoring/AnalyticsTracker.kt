package com.sultanagung1.sista.core.monitoring

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

object AnalyticsTracker {

    private const val TAG = "AnalyticsTracker"
    private val userProperties = ConcurrentHashMap<String, String>()

    fun setUserProperties(role: String, userIdentifier: String, academicYear: String = "2025/2026", className: String = "") {
        userProperties["user_role"] = role
        userProperties["user_id"] = userIdentifier
        userProperties["academic_year"] = academicYear
        if (className.isNotEmpty()) {
            userProperties["class_name"] = className
        }
        Log.d(TAG, "👤 User properties updated: $userProperties")
        CrashReportingTree.logBreadcrumb("Analytics", "User identified as $role ($userIdentifier)")
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        Log.i(TAG, "📊 Event logged: [$eventName] with params: $params")
        CrashReportingTree.logBreadcrumb("AnalyticsEvent", "Event: $eventName")
    }

    // Standard Enterprise Events
    fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    fun logAttendanceSuccess(method: String, distanceMeters: Double) {
        logEvent("attendance_checkin", mapOf("method" to method, "distance_meters" to distanceMeters))
    }

    fun logCbtSubmit(examId: String, totalAnswered: Int, durationSeconds: Long) {
        logEvent("cbt_exam_submitted", mapOf("exam_id" to examId, "answered" to totalAnswered, "duration_s" to durationSeconds))
    }
}
