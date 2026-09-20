package com.sultanagung1.sista.core.sync

import android.util.Log
import com.google.gson.Gson
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.IdempotencyInterceptor
import com.sultanagung1.sista.data.local.SulaoneLocalStore
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * FASE 61.1: Offline Action Queue with Strict Ordering & Sequential Lock.
 *
 * Enforces FIFO sequential replay of offline actions. Each action carries a stable
 * Idempotency-Key (action.id). If any action fails due to server error (5xx) or
 * network disconnect, subsequent actions are strictly blocked (sequential lock)
 * to prevent out-of-order state mutations.
 */
class OfflineActionQueue(
    private val localStore: SulaoneLocalStore,
    private val apiClient: ApiClient
) {

    private val TAG = "OfflineActionQueue"
    private val gson = Gson()
    private val queueMutex = Mutex()

    fun queueAttendance(request: GpsCheckinRequest): String {
        return localStore.enqueueAction("ATTENDANCE_CHECKIN", request)
    }

    fun queueMutabaah(activities: List<MutabaahLogItem>): String {
        return localStore.enqueueAction("MUTABAAH_LOG", activities)
    }

    fun queueJournal(request: StoreJournalRequest): String {
        return localStore.enqueueAction("TEACHING_JOURNAL", request)
    }

    /** FASE 69.3: queues a CBT submission that failed purely due to connectivity, so pressing "Submit" during a dropout still guarantees delivery once back online. */
    fun queueCbtSubmit(request: CbtSubmitRequest): String {
        return localStore.enqueueAction("CBT_SUBMIT", request)
    }

    suspend fun processPendingQueue(): Int = withContext(Dispatchers.IO) {
        // Enforce single-worker execution with Mutex
        queueMutex.withLock {
            val pendingActions = localStore.getPendingActions().sortedBy { it.createdAt }
            var successCount = 0

            for (action in pendingActions) {
                try {
                    // Inject stable action.id as Idempotency-Key across replays
                    IdempotencyInterceptor.activeKey.set(action.id)

                    var isSuccess = false
                    var statusCode = 200

                    when (action.actionType) {
                        "ATTENDANCE_CHECKIN" -> {
                            val req = gson.fromJson(action.payloadJson, GpsCheckinRequest::class.java)
                            val response = apiClient.attendanceApi.submitGpsCheckin(req)
                            statusCode = response.code()
                            isSuccess = response.isSuccessful
                        }
                        "MUTABAAH_LOG" -> {
                            val activities = gson.fromJson(action.payloadJson, Array<MutabaahLogItem>::class.java).toList()
                            localStore.saveMutabaah(activities)
                            isSuccess = true
                            statusCode = 200
                        }
                        "TEACHING_JOURNAL" -> {
                            val req = gson.fromJson(action.payloadJson, StoreJournalRequest::class.java)
                            val response = apiClient.teachingJournalApi.storeJournal(req)
                            statusCode = response.code()
                            isSuccess = response.isSuccessful
                        }
                        "CBT_SUBMIT" -> {
                            val req = gson.fromJson(action.payloadJson, CbtSubmitRequest::class.java)
                            val response = apiClient.cbtApi.submitExam(req.examId, req)
                            statusCode = response.code()
                            isSuccess = response.isSuccessful
                        }
                        else -> {
                            isSuccess = true
                            statusCode = 200
                        }
                    }

                    if (isSuccess) {
                        localStore.removeAction(action.id)
                        successCount++
                        Log.d(TAG, "Replayed action ${action.id} (${action.actionType}) successfully.")
                    } else if (statusCode in 400..499) {
                        // Permanent client/validation rejection: discard to unblock queue
                        Log.w(TAG, "Action ${action.id} permanently rejected by server with HTTP $statusCode. Discarding.")
                        localStore.removeAction(action.id)
                    } else {
                        // Server error (5xx): Increment retry and BREAK sequential lock
                        Log.e(TAG, "Server error HTTP $statusCode processing action ${action.id}. Halting queue processing (Sequential Lock).")
                        localStore.incrementRetry(action.id)
                        if (action.retryCount >= 5) {
                            localStore.removeAction(action.id)
                            Log.w(TAG, "Action ${action.id} exceeded max retry threshold (5). Dropping.")
                        }
                        break
                    }
                } catch (e: Exception) {
                    // Network disconnect / socket timeout: Sequential lock halts queue
                    Log.e(TAG, "Network exception replaying action ${action.id}: ${e.message}. Halting queue processing.")
                    localStore.incrementRetry(action.id)
                    break
                } finally {
                    IdempotencyInterceptor.activeKey.remove()
                }
            }
            successCount
        }
    }
}

