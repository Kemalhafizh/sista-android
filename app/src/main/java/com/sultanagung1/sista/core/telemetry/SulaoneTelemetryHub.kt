package com.sultanagung1.sista.core.telemetry

import android.content.Context
import android.os.SystemClock
import android.util.Log

/**
 * Handle untuk mengontrol siklus hidup trace performa (start/stop)
 */
data class PerformanceTraceHandle(
    val traceName: String,
    val startTimeMs: Long = SystemClock.elapsedRealtime(),
    val firebaseTrace: Any? = null
) {
    fun stop(hub: SulaoneTelemetryHub): Long {
        return hub.stopTrace(this)
    }
}

/**
 * SulaoneTelemetryHub: Single Unified Telemetry Facade untuk aplikasi SISTA.
 * Mengoordinasikan pencatatan Breadcrumbs, Crash Handling, Firebase Crashlytics & Performance,
 * serta penyimpanan diagnostik lokal APM.
 */
class SulaoneTelemetryHub private constructor() {

    private val tag = "SulaoneTelemetryHub"
    private var context: Context? = null
    var breadcrumbBuffer: BreadcrumbBuffer = BreadcrumbBuffer()
        private set
    var crashRepository: CrashReportRepository? = null
        private set
    var firebaseSink: FirebaseTelemetrySink? = null
        private set
    private var crashHandler: SulaoneCrashHandler? = null

    private var isInitialized = false
    private val currentUserContext = mutableMapOf<String, String>()

    @Synchronized
    fun initialize(appContext: Context, enableCrashHandler: Boolean = true) {
        if (isInitialized) return
        this.context = appContext.applicationContext

        // 1. Inisialisasi Ring Buffer & Repositori
        breadcrumbBuffer = BreadcrumbBuffer()
        crashRepository = CrashReportRepository(appContext)
        firebaseSink = FirebaseTelemetrySink(appContext)

        // 2. Pasang Global Uncaught Exception Handler
        if (enableCrashHandler) {
            val handler = SulaoneCrashHandler(
                context = appContext,
                breadcrumbBuffer = breadcrumbBuffer,
                crashReportRepository = crashRepository!!,
                firebaseSink = firebaseSink
            )
            crashHandler = handler
            Thread.setDefaultUncaughtExceptionHandler(handler)
            Log.d(tag, "🛡️ Global Uncaught Exception Handler registered")
        }

        // 3. Catat breadcrumb inisialisasi awal
        isInitialized = true
        logBreadcrumb(
            category = BreadcrumbCategory.LIFECYCLE,
            message = "SulaoneTelemetryHub initialized successfully",
            level = BreadcrumbLevel.INFO
        )
    }

    fun isReady(): Boolean = isInitialized

    fun logBreadcrumb(
        category: BreadcrumbCategory,
        message: String,
        level: BreadcrumbLevel = BreadcrumbLevel.INFO,
        data: Map<String, String> = emptyMap()
    ) {
        val sanitizedMessage = TelemetrySanitizer.sanitizeString(message)
        val sanitizedData = TelemetrySanitizer.sanitizeData(data)

        val breadcrumb = Breadcrumb(
            timestampMs = System.currentTimeMillis(),
            category = category,
            level = level,
            message = sanitizedMessage,
            data = sanitizedData
        )

        // Simpan ke ring buffer lokal
        breadcrumbBuffer.add(breadcrumb)

        // Teruskan ke Firebase Crashlytics jika aktif
        firebaseSink?.logBreadcrumb(
            category = category.name,
            message = sanitizedMessage,
            level = level.name
        )

        if (level == BreadcrumbLevel.ERROR || level == BreadcrumbLevel.WARN) {
            Log.w(tag, "🍞 $breadcrumb")
        } else {
            Log.d(tag, "🍞 $breadcrumb")
        }
    }

    fun recordException(
        throwable: Throwable,
        isFatal: Boolean = false,
        extraData: Map<String, String> = emptyMap()
    ) {
        val sanitizedData = TelemetrySanitizer.sanitizeData(extraData)
        val fullData = currentUserContext + sanitizedData

        // Simpan ke Firebase Crashlytics
        firebaseSink?.recordException(throwable, isFatal = isFatal, metadata = fullData)

        // Catat breadcrumb error
        logBreadcrumb(
            category = BreadcrumbCategory.SYSTEM,
            message = "Exception recorded [${throwable.javaClass.simpleName}]: ${throwable.message}",
            level = BreadcrumbLevel.ERROR,
            data = sanitizedData
        )

        // Jika non-fatal dan ada crashRepository, catat juga ke crash repo jika error kritis
        if (isFatal) {
            Log.e(tag, "🚨 Fatal exception recorded: ${throwable.message}", throwable)
        } else {
            Log.w(tag, "⚠️ Non-fatal exception recorded: ${throwable.message}", throwable)
        }
    }

    fun setUserContext(userId: String?, role: String?, studentClass: String? = null) {
        if (userId != null) {
            val sanitizedId = TelemetrySanitizer.sanitizeString(userId)
            currentUserContext["user_id"] = sanitizedId
            firebaseSink?.setUserId(sanitizedId)
        }
        if (role != null) {
            currentUserContext["user_role"] = role
            firebaseSink?.setCustomKey("user_role", role)
        }
        if (studentClass != null) {
            currentUserContext["student_class"] = studentClass
            firebaseSink?.setCustomKey("student_class", studentClass)
        }
        crashHandler?.setUserContext(currentUserContext)

        logBreadcrumb(
            category = BreadcrumbCategory.USER_ACTION,
            message = "User context updated: role=$role",
            level = BreadcrumbLevel.INFO
        )
    }

    fun setCustomKey(key: String, value: String) {
        val sanitizedKey = TelemetrySanitizer.sanitizeString(key)
        val sanitizedValue = TelemetrySanitizer.sanitizeString(value)
        currentUserContext[sanitizedKey] = sanitizedValue
        firebaseSink?.setCustomKey(sanitizedKey, sanitizedValue)
        crashHandler?.setUserContext(currentUserContext)
    }

    fun startTrace(traceName: String): PerformanceTraceHandle {
        val fbTrace = firebaseSink?.startTrace(traceName)
        logBreadcrumb(
            category = BreadcrumbCategory.SYSTEM,
            message = "Started Performance Trace: $traceName",
            level = BreadcrumbLevel.DEBUG
        )
        return PerformanceTraceHandle(
            traceName = traceName,
            startTimeMs = SystemClock.elapsedRealtime(),
            firebaseTrace = fbTrace
        )
    }

    fun stopTrace(handle: PerformanceTraceHandle): Long {
        val durationMs = SystemClock.elapsedRealtime() - handle.startTimeMs
        if (handle.firebaseTrace is com.google.firebase.perf.metrics.Trace) {
            firebaseSink?.stopTrace(handle.firebaseTrace)
        }
        logBreadcrumb(
            category = BreadcrumbCategory.SYSTEM,
            message = "Completed Performance Trace [${handle.traceName}] in ${durationMs}ms",
            level = if (durationMs > 2000) BreadcrumbLevel.WARN else BreadcrumbLevel.INFO,
            data = mapOf("duration_ms" to durationMs.toString())
        )
        return durationMs
    }

    fun getRecentBreadcrumbs(): List<Breadcrumb> {
        return breadcrumbBuffer.getSnapshot()
    }

    companion object {
        val instance: SulaoneTelemetryHub by lazy { SulaoneTelemetryHub() }
    }
}
