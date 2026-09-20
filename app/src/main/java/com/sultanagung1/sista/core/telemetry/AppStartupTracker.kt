package com.sultanagung1.sista.core.telemetry

import android.os.SystemClock
import android.util.Log

/**
 * Pelacak durasi waktu muat awal aplikasi (App Start-up Time / Cold Start).
 * Mengukur waktu dari Application.onCreate() hingga frame komposisi pertama selesai digambar.
 */
object AppStartupTracker {

    private const val tag = "AppStartupTracker"
    private var appStartTimeElapsed: Long = 0L
    private var appStartTimeEpoch: Long = 0L
    private var firstDrawTimeElapsed: Long = 0L
    private var hasStarted = false
    private var isCompleted = false

    private fun getCurrentMonotonicMs(): Long {
        return try {
            val elapsed = SystemClock.elapsedRealtime()
            if (elapsed > 0L) elapsed else System.currentTimeMillis()
        } catch (_: Throwable) {
            System.currentTimeMillis()
        }
    }

    fun recordAppStart() {
        if (!hasStarted) {
            hasStarted = true
            appStartTimeElapsed = getCurrentMonotonicMs()
            appStartTimeEpoch = System.currentTimeMillis()
            Log.d(tag, "⏱️ App cold start initiated at epoch $appStartTimeEpoch")
        }
    }

    fun recordFirstDraw(hub: SulaoneTelemetryHub = SulaoneTelemetryHub.instance) {
        if (isCompleted || !hasStarted) return
        firstDrawTimeElapsed = getCurrentMonotonicMs()
        isCompleted = true

        val coldStartDurationMs = (firstDrawTimeElapsed - appStartTimeElapsed).coerceAtLeast(1L)
        Log.i(tag, "🚀 App Cold Start completed in ${coldStartDurationMs}ms")

        val level = when {
            coldStartDurationMs > 3000L -> BreadcrumbLevel.WARN
            else -> BreadcrumbLevel.INFO
        }

        hub.logBreadcrumb(
            category = BreadcrumbCategory.LIFECYCLE,
            message = "App Cold Start rendered in ${coldStartDurationMs}ms",
            level = level,
            data = mapOf(
                "cold_start_ms" to coldStartDurationMs.toString(),
                "threshold_target_ms" to "2000"
            )
        )

        hub.setCustomKey("cold_start_duration_ms", coldStartDurationMs.toString())
    }

    fun getColdStartDurationMs(): Long {
        if (!isCompleted || !hasStarted) return -1L
        return (firstDrawTimeElapsed - appStartTimeElapsed).coerceAtLeast(1L)
    }

    fun isCompleted(): Boolean = isCompleted

    fun resetForTesting() {
        appStartTimeElapsed = 0L
        appStartTimeEpoch = 0L
        firstDrawTimeElapsed = 0L
        hasStarted = false
        isCompleted = false
    }
}
