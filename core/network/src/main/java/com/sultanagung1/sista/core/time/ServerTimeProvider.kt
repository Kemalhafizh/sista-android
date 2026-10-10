package com.sultanagung1.sista.core.time

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/**
 * FASE 71 — anti-tamper time source. Every "what time is it, what day is it"
 * decision that matters (schedule "sedang berlangsung" status, next-class
 * countdown, attendance windows, journal/report dates) must not trust the
 * device's own clock, since a student or parent can freely change it in
 * Settings. This periodically syncs against the backend's real clock and
 * feeds the resulting offset into [DateUtils], which every such check already
 * goes through.
 *
 * Reuses `GET mobile/config` — a real, already-authenticated-free endpoint the
 * app already calls at startup for update/maintenance checks (`InAppUpdateManager`)
 * — rather than adding a second network round trip for a dedicated endpoint.
 * `server_time` was already present in that real response and simply never
 * consumed until now.
 *
 * Sync accuracy: round-trip time is measured via [SystemClock.elapsedRealtime]
 * (a monotonic boot clock, itself immune to wall-clock changes) and assumed
 * symmetric, so the server timestamp is anchored to the request's midpoint
 * rather than either endpoint — the standard NTP-style correction, good enough
 * for minute-granularity school schedule checks without needing a dedicated
 * time-sync protocol.
 */
class ServerTimeProvider(
    private val context: Context,
    private val apiClient: ApiClient
) {
    private val tag = "ServerTimeProvider"
    private val syncMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Re-sync at least this often while the app is in active use (resume hook covers backgrounding). */
    private val periodicSyncIntervalMillis = 20 * 60 * 1000L

    /** Fire-and-forget sync — safe to call from a UI thread lifecycle hook (app start/resume). */
    fun syncAsync() {
        scope.launch { sync() }
    }

    /** Syncs only if never synced yet, or it's been a while — cheap to call before any time-sensitive action. */
    suspend fun syncIfStale() {
        if (!DateUtils.isServerTimeSynced() || DateUtils.millisSinceLastSync() > periodicSyncIntervalMillis) {
            sync()
        }
    }

    /** Performs the actual round trip. Returns true on success. Safe to call concurrently — only one real sync runs at a time. */
    suspend fun sync(): Boolean = syncMutex.withLock {
        val requestSentWallClock = System.currentTimeMillis()
        val requestSentElapsed = SystemClock.elapsedRealtime()
        try {
            val response = apiClient.mobileConfigApi.getConfig(platform = "android", build = currentBuildNumber())
            val responseReceivedElapsed = SystemClock.elapsedRealtime()
            val serverTimeRaw = response.body()?.serverTime
            if (!response.isSuccessful || serverTimeRaw.isNullOrBlank()) {
                Log.w(tag, "Server time sync failed: HTTP ${response.code()}")
                return@withLock false
            }

            val serverEpochMillis = parseServerTime(serverTimeRaw) ?: run {
                Log.w(tag, "Could not parse server_time: $serverTimeRaw")
                return@withLock false
            }

            val roundTripMillis = responseReceivedElapsed - requestSentElapsed
            // Anchor the server timestamp to the midpoint of the request, assuming symmetric latency.
            val deviceWallClockAtServerMoment = requestSentWallClock + roundTripMillis / 2
            val offsetMillis = serverEpochMillis - deviceWallClockAtServerMoment

            DateUtils.applyServerOffset(offsetMillis, responseReceivedElapsed)
            Log.d(tag, "Server time synced: offset=${offsetMillis}ms rtt=${roundTripMillis}ms")
            true
        } catch (e: Exception) {
            Log.w(tag, "Server time sync exception: ${e.message}")
            false
        }
    }

    private fun parseServerTime(raw: String): Long? {
        return try {
            OffsetDateTime.parse(raw).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun currentBuildNumber(): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) it.longVersionCode.toInt() else @Suppress("DEPRECATION") it.versionCode
            }
        } catch (_: Exception) {
            0
        }
    }
}
