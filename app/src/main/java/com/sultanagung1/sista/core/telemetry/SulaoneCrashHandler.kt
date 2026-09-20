package com.sultanagung1.sista.core.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.util.UUID

/**
 * Global Uncaught Exception Handler untuk menangkap semua fatal crash di thread utama
 * maupun worker thread coroutines.
 * Memperkaya laporan dengan metadata perangkat, memori, baterai, jaringan, dan 50 breadcrumbs terakhir.
 */
class SulaoneCrashHandler(
    private val context: Context,
    private val breadcrumbBuffer: BreadcrumbBuffer,
    private val crashReportRepository: CrashReportRepository,
    private val firebaseSink: FirebaseTelemetrySink?,
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) : Thread.UncaughtExceptionHandler {

    private val tag = "SulaoneCrashHandler"
    private var currentUserContext: Map<String, String> = emptyMap()

    fun setUserContext(contextMap: Map<String, String>) {
        currentUserContext = contextMap
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(tag, "💥 CRITICAL UNCAUGHT EXCEPTION on thread [${thread.name}]: ${throwable.message}")

            val stackTraceStr = Log.getStackTraceString(throwable)
            val deviceInfo = collectDeviceInfo()
            val appInfo = collectAppInfo(thread)
            val breadcrumbSnapshot = breadcrumbBuffer.getSnapshot().map { it.toString() }

            val crashReport = CrashReport(
                reportId = UUID.randomUUID().toString(),
                timestampMs = System.currentTimeMillis(),
                exceptionType = throwable.javaClass.name,
                exceptionMessage = throwable.message ?: "No message provided",
                stackTrace = stackTraceStr,
                isFatal = true,
                deviceInfo = deviceInfo,
                appInfo = appInfo,
                userContext = currentUserContext,
                breadcrumbs = breadcrumbSnapshot,
                isSent = false
            )

            // 1. Simpan dump lokal berformat JSON
            crashReportRepository.saveCrashReport(crashReport)

            // 2. Kirim ke Firebase Crashlytics jika tersedia
            val crashMeta = mutableMapOf<String, String>()
            crashMeta["thread_name"] = thread.name
            crashMeta["exception_type"] = throwable.javaClass.name
            crashMeta["device_model"] = deviceInfo["model"] ?: "Unknown"
            crashMeta["ram_available"] = deviceInfo["ram_available_mb"] ?: "Unknown"
            crashMeta.putAll(currentUserContext)

            firebaseSink?.recordException(throwable, isFatal = true, metadata = crashMeta)

        } catch (e: Throwable) {
            Log.e(tag, "Failed to capture crash dump: ${e.message}")
        } finally {
            // Serahkan ke default exception handler Android untuk proses terminasi normal
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun collectDeviceInfo(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            map["manufacturer"] = Build.MANUFACTURER
            map["model"] = Build.MODEL
            map["brand"] = Build.BRAND
            map["android_version"] = Build.VERSION.RELEASE
            map["sdk_int"] = Build.VERSION.SDK_INT.toString()
            map["board"] = Build.BOARD
            map["fingerprint"] = Build.FINGERPRINT

            // Memory Info
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)
            map["ram_available_mb"] = (memInfo.availMem / (1024 * 1024)).toString()
            map["ram_total_mb"] = (memInfo.totalMem / (1024 * 1024)).toString()
            map["ram_low_memory"] = memInfo.lowMemory.toString()

            // Internal Storage Info
            val stat = StatFs(Environment.getDataDirectory().path)
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            map["storage_free_mb"] = (freeBytes / (1024 * 1024)).toString()

            // Battery Info
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
            map["battery_pct"] = "$batteryPct%"

            // Network Type
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(network)
            val netType = when {
                caps == null -> "Offline"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Other"
            }
            map["network_type"] = netType

        } catch (e: Throwable) {
            map["device_info_error"] = e.message ?: "Failed collecting info"
        }
        return map
    }

    private fun collectAppInfo(thread: Thread): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            map["version_name"] = pInfo.versionName ?: "2.0"
            map["version_code"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toString()
            }
            map["package_name"] = context.packageName
            map["thread_name"] = thread.name
            map["thread_id"] = thread.id.toString()
        } catch (e: Throwable) {
            map["app_info_error"] = e.message ?: "Failed collecting app info"
        }
        return map
    }
}
