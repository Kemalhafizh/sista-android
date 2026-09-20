package com.sultanagung1.sista.core.monitoring

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

data class Breadcrumb(
    val timestamp: String,
    val category: String,
    val message: String,
    val level: String
)

object CrashReportingTree {

    private const val TAG = "CrashReportingTree"
    private const val MAX_BREADCRUMBS = 50
    private val breadcrumbQueue = ConcurrentLinkedQueue<Breadcrumb>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun logBreadcrumb(category: String, message: String, level: String = "INFO") {
        val bc = Breadcrumb(
            timestamp = dateFormat.format(Date()),
            category = category,
            message = message,
            level = level
        )
        breadcrumbQueue.add(bc)
        while (breadcrumbQueue.size > MAX_BREADCRUMBS) {
            breadcrumbQueue.poll()
        }
        Log.d(TAG, "🍞 [Breadcrumb][$category] $message")

        // Forward to unified telemetry hub
        try {
            val catEnum = when (category.uppercase()) {
                "NAVIGATION" -> com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.NAVIGATION
                "NETWORK" -> com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.NETWORK
                "LIFECYCLE" -> com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.LIFECYCLE
                "USER_ACTION" -> com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.USER_ACTION
                else -> com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.SYSTEM
            }
            val lvlEnum = when (level.uppercase()) {
                "DEBUG" -> com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.DEBUG
                "WARN" -> com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.WARN
                "ERROR" -> com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.ERROR
                else -> com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.INFO
            }
            com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.logBreadcrumb(
                category = catEnum,
                message = "[$category] $message",
                level = lvlEnum
            )
        } catch (e: Throwable) {
            // Ignore bridge errors
        }
    }

    fun recordNonFatalException(throwable: Throwable, contextTag: String = "App") {
        val stackTraceStr = Log.getStackTraceString(throwable)
        Log.e(TAG, "🚨 Non-Fatal Exception in [$contextTag]: ${throwable.message}\n$stackTraceStr")
        logBreadcrumb(contextTag, "Exception: ${throwable.message}", "ERROR")

        try {
            com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.recordException(
                throwable = throwable,
                isFatal = false,
                extraData = mapOf("context_tag" to contextTag)
            )
        } catch (e: Throwable) {
            // Ignore bridge errors
        }
    }

    fun getDeviceDiagnostics(): Map<String, String> {
        return mapOf(
            "device_model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "android_version" to Build.VERSION.RELEASE,
            "sdk_int" to Build.VERSION.SDK_INT.toString(),
            "fingerprint" to Build.FINGERPRINT,
            "app_version" to "2.0.0 (Enterprise Sprint 17)"
        )
    }

    fun getRecentBreadcrumbs(): List<Breadcrumb> = breadcrumbQueue.toList()
}
