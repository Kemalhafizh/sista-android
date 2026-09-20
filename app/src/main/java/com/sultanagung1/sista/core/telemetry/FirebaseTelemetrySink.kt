package com.sultanagung1.sista.core.telemetry

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * Sink implementasi telemetri ke Firebase Crashlytics & Firebase Performance.
 * Memiliki mekanisme fail-safe: jika Firebase belum diinisialisasi atau perangkat tidak memiliki
 * Google Play Services, seluruh pemanggilan diabaikan secara aman tanpa melempar Exception.
 */
class FirebaseTelemetrySink(private val context: Context) {

    private val tag = "FirebaseTelemetrySink"
    private var crashlytics: FirebaseCrashlytics? = null
    private var performance: FirebasePerformance? = null
    private var analytics: FirebaseAnalytics? = null
    private var isInitialized = false

    init {
        initialize()
    }

    fun initialize() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:100961092811:android:df02284a53fea80e78474e")
                    .setApiKey("AIzaSyBzFc7_PmoNmn4i_-mkYjYpl7NCLRGO_Lw")
                    .setProjectId("device-streaming-31ed4203")
                    .setStorageBucket("device-streaming-31ed4203.firebasestorage.app")
                    .setGcmSenderId("100961092811")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            crashlytics = FirebaseCrashlytics.getInstance()
            performance = FirebasePerformance.getInstance()
            analytics = FirebaseAnalytics.getInstance(context)
            isInitialized = true
            Log.i(tag, "🔥 Firebase Telemetry Sink initialized successfully (Crashlytics & Perf Active)")
        } catch (e: Throwable) {
            isInitialized = false
            Log.w(tag, "⚠️ Firebase Telemetry Sink unavailable (safe fallback active): ${e.message}")
        }
    }

    fun isAvailable(): Boolean = isInitialized

    fun recordException(throwable: Throwable, isFatal: Boolean = false, metadata: Map<String, String> = emptyMap()) {
        if (!isInitialized) return
        try {
            metadata.forEach { (k, v) ->
                crashlytics?.setCustomKey(k, v)
            }
            if (isFatal) {
                crashlytics?.setCustomKey("is_fatal", true)
            }
            crashlytics?.recordException(throwable)
        } catch (e: Throwable) {
            Log.w(tag, "Failed to record exception in Crashlytics: ${e.message}")
        }
    }

    fun logBreadcrumb(category: String, message: String, level: String = "INFO") {
        if (!isInitialized) return
        try {
            crashlytics?.log("[$level][$category] $message")
        } catch (e: Throwable) {
            Log.w(tag, "Failed to log breadcrumb to Crashlytics: ${e.message}")
        }
    }

    fun setUserId(userId: String) {
        if (!isInitialized) return
        try {
            val sanitizedId = TelemetrySanitizer.sanitizeString(userId)
            crashlytics?.setUserId(sanitizedId)
            analytics?.setUserId(sanitizedId)
        } catch (e: Throwable) {
            Log.w(tag, "Failed to set user ID in Firebase: ${e.message}")
        }
    }

    fun setCustomKey(key: String, value: String) {
        if (!isInitialized) return
        try {
            val sanitizedKey = TelemetrySanitizer.sanitizeString(key)
            val sanitizedValue = TelemetrySanitizer.sanitizeString(value)
            crashlytics?.setCustomKey(sanitizedKey, sanitizedValue)
        } catch (e: Throwable) {
            Log.w(tag, "Failed to set custom key in Crashlytics: ${e.message}")
        }
    }

    fun startTrace(traceName: String): Trace? {
        if (!isInitialized) return null
        return try {
            val trace = performance?.newTrace(traceName)
            trace?.start()
            trace
        } catch (e: Throwable) {
            Log.w(tag, "Failed to start Firebase Perf trace: ${e.message}")
            null
        }
    }

    fun stopTrace(trace: Trace?) {
        if (!isInitialized || trace == null) return
        try {
            trace.stop()
        } catch (e: Throwable) {
            Log.w(tag, "Failed to stop Firebase Perf trace: ${e.message}")
        }
    }
}
