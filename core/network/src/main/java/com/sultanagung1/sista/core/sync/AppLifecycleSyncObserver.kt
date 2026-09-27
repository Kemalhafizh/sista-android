package com.sultanagung1.sista.core.sync

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FASE 61.3: WebSocket Catch-up & App Lifecycle Hooks.
 *
 * Observes application lifecycle events. When the app returns from background
 * (ON_RESUME), it triggers a catch-up sync to fetch missed events while the app
 * was in background or Doze mode.
 */
class AppLifecycleSyncObserver(
    private val syncManager: SyncManager? = null,
    private val onCatchUp: (suspend () -> Unit)? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val minimumBackgroundDowntimeMs: Long = 1_000L
) : DefaultLifecycleObserver {

    private val TAG = "AppLifecycleSyncObserver"
    private var lastPausedTimestamp: Long = 0L
    private var wasInBackground: Boolean = false

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        lastPausedTimestamp = System.currentTimeMillis()
        wasInBackground = true
        Log.d(TAG, "App paused at $lastPausedTimestamp, marked as in-background.")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        onAppResumed()
    }

    /**
     * Triggered when app enters foreground from background.
     */
    fun onAppResumed() {
        val now = System.currentTimeMillis()
        val downtime = if (lastPausedTimestamp > 0) now - lastPausedTimestamp else 0L
        Log.d(TAG, "App resumed after ${downtime}ms downtime. wasInBackground=$wasInBackground")

        if (wasInBackground || lastPausedTimestamp == 0L) {
            wasInBackground = false
            Log.i(TAG, "Triggering catch-up sync on app resume...")
            coroutineScope.launch {
                try {
                    syncManager?.performCatchUpSync()
                    onCatchUp?.invoke()
                } catch (e: Exception) {
                    Log.e(TAG, "Catch-up sync failed on resume: ${e.message}")
                }
            }
        }
    }

    /**
     * Explicit trigger for testing or manual resume simulations.
     */
    fun triggerManualResume() {
        wasInBackground = true
        onAppResumed()
    }
}
