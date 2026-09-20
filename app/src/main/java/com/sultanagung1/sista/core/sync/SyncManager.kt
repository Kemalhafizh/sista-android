package com.sultanagung1.sista.core.sync

import android.content.Context
import android.util.Log
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.data.local.SulaoneLocalStore
import com.sultanagung1.sista.data.model.CatchUpEventItem
import com.sultanagung1.sista.data.model.TombstoneItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FASE 61: Hardened Sync Engine.
 *
 * Coordinates offline action replay (FIFO with Idempotency), delta updates,
 * tombstone purge (anti-ghost data), and WebSocket catch-up upon app resume.
 */
class SyncManager(
    private val context: Context,
    private val apiClient: ApiClient,
    private val localStore: SulaoneLocalStore,
    private val actionQueue: OfflineActionQueue,
    private val connectivityObserver: NetworkConnectivityObserver
) {

    private val TAG = "SyncManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isOnline = MutableStateFlow(connectivityObserver.checkCurrentConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow("Baru saja")
    val lastSyncedTime: StateFlow<String> = _lastSyncedTime.asStateFlow()

    private val _pendingActionsCount = MutableStateFlow(localStore.getPendingActionsCount())
    val pendingActionsCount: StateFlow<Int> = _pendingActionsCount.asStateFlow()

    var lastSyncTimestampMs: Long = System.currentTimeMillis() - (86400 * 1000L) // Default 24h ago
        private set

    var lastEventId: String = "0"
        private set

    init {
        // Observe network state changes
        scope.launch {
            connectivityObserver.isOnline.collect { online ->
                val wasOffline = !_isOnline.value
                _isOnline.value = online
                if (online && wasOffline) {
                    Log.d(TAG, "Network restored. Triggering automatic queue sync & delta catch-up...")
                    performFullSync()
                }
            }
        }
    }

    /**
     * Purges records locally based on Tombstone items received from server.
     * Guarantees zero ghost data when records are soft-deleted elsewhere.
     */
    fun processTombstones(tombstones: List<TombstoneItem>) {
        if (tombstones.isEmpty()) return
        Log.d(TAG, "Processing ${tombstones.size} tombstones from sync...")

        val grouped = tombstones.groupBy { it.type.lowercase() }
        for ((type, items) in grouped) {
            val ids = items.map { it.id }
            localStore.purgeTombstones(type, ids)
            Log.d(TAG, "Purged ${ids.size} tombstone records of type '$type'")
        }
    }

    /**
     * Executes full sync: processes pending FIFO offline queue, pulls delta changes,
     * purges tombstones, and refreshes academic data.
     */
    fun performFullSync() {
        if (_isSyncing.value) return
        scope.launch {
            _isSyncing.value = true
            try {
                // 1. Process pending offline action mutations first (strict FIFO with Idempotency)
                actionQueue.processPendingQueue()
                _pendingActionsCount.value = localStore.getPendingActionsCount()

                // 2. Fetch delta changes and tombstones if online
                if (_isOnline.value) {
                    try {
                        val deltaResp = apiClient.syncApi.getDelta(lastSyncTimestampMs)
                        if (deltaResp.isSuccessful && deltaResp.body() != null) {
                            val body = deltaResp.body()!!
                            processTombstones(body.tombstones)
                            if (body.syncTimestamp > 0) {
                                lastSyncTimestampMs = body.syncTimestamp
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Delta sync request bypassed: ${e.message}")
                    }

                    // 3. Fetch fresh academic & schedule data
                    val scheduleResp = apiClient.studentApi.getSchedule()
                    if (scheduleResp.isSuccessful && scheduleResp.body() != null) {
                        localStore.saveSchedule(scheduleResp.body()!!)
                    }

                    val gradesResp = apiClient.studentApi.getAcademicSummary()
                    if (gradesResp.isSuccessful && gradesResp.body() != null) {
                        localStore.saveGrades(gradesResp.body()!!)
                    }

                    val annResp = apiClient.notificationApi.getAnnouncements()
                    if (annResp.isSuccessful && annResp.body() != null) {
                        localStore.saveAnnouncements(annResp.body()!!)
                    }
                }

                val timeFormat = SimpleDateFormat("HH:mm WIB", Locale.getDefault())
                _lastSyncedTime.value = timeFormat.format(Date())
            } catch (e: Exception) {
                Log.e(TAG, "Sync error: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * FASE 61.3: WebSocket Catch-Up Request.
     * Invoked when the application resumes from background or recovers connection.
     * Fetches missed stream events since [lastEventId].
     */
    suspend fun performCatchUpSync(overrideEventId: String? = null): List<CatchUpEventItem> {
        val targetEventId = overrideEventId ?: lastEventId
        Log.d(TAG, "Performing WebSocket Catch-Up since event ID '$targetEventId'...")

        return try {
            val response = apiClient.syncApi.getCatchUp(targetEventId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (!body.lastEventId.isNullOrBlank()) {
                    lastEventId = body.lastEventId
                }
                Log.d(TAG, "Catch-Up successful. Retrieved ${body.events.size} missed events. New event ID: $lastEventId")
                body.events
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Catch-Up failed: ${e.message}")
            emptyList()
        }
    }

    fun updatePendingCount() {
        _pendingActionsCount.value = localStore.getPendingActionsCount()
    }
}
