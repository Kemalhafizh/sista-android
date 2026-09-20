package com.sultanagung1.sista.core.monitoring

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

object PerformanceTracer {

    private const val TAG = "PerformanceTracer"
    private val activeTraces = ConcurrentHashMap<String, Long>()

    // Predefined Critical Traces
    const val TRACE_LOGIN_TO_HOME = "trace_login_to_home"
    const val TRACE_CBT_EXAM_LOAD = "trace_cbt_exam_load"
    const val TRACE_ATTENDANCE_GPS = "trace_attendance_gps_acquire"
    const val TRACE_OFFLINE_SYNC = "trace_offline_sync_cycle"

    fun startTrace(traceName: String) {
        val startTime = SystemClock.elapsedRealtime()
        activeTraces[traceName] = startTime
        Log.d(TAG, "⏱️ Started trace: [$traceName] at ${startTime}ms")
        try {
            com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.logBreadcrumb(
                category = com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.SYSTEM,
                message = "Performance Trace started: $traceName",
                level = com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.DEBUG
            )
        } catch (e: Throwable) {
            // Ignore bridge errors
        }
    }

    fun stopTrace(traceName: String): Long {
        val startTime = activeTraces.remove(traceName) ?: return -1L
        val durationMs = SystemClock.elapsedRealtime() - startTime
        Log.i(TAG, "🏁 Completed trace: [$traceName] in ${durationMs}ms")
        try {
            com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.logBreadcrumb(
                category = com.sultanagung1.sista.core.telemetry.BreadcrumbCategory.SYSTEM,
                message = "Performance Trace [$traceName] completed in ${durationMs}ms",
                level = if (durationMs > 2000L) com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.WARN else com.sultanagung1.sista.core.telemetry.BreadcrumbLevel.INFO,
                data = mapOf("duration_ms" to durationMs.toString())
            )
        } catch (e: Throwable) {
            // Ignore bridge errors
        }
        return durationMs
    }

    inline fun <T> measureTrace(traceName: String, block: () -> T): T {
        startTrace(traceName)
        try {
            return block()
        } finally {
            stopTrace(traceName)
        }
    }
}
