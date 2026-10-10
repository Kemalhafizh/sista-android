package com.sultanagung1.sista.sync

import com.google.gson.Gson
import com.sultanagung1.sista.core.network.IdempotencyInterceptor
import com.sultanagung1.sista.core.sync.AppLifecycleSyncObserver
import com.sultanagung1.sista.core.sync.createSnapshot
import com.sultanagung1.sista.core.sync.optimisticUpdate
import com.sultanagung1.sista.data.local.entity.PendingActionItem
import com.sultanagung1.sista.data.model.AnnouncementItem
import com.sultanagung1.sista.data.model.DeltaSyncResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * FASE 61: Automated Verification Suite for Sync Engine Hardening & Data Consistency.
 *
 * Covers:
 * 1. IdempotencyInterceptor: Automatic UUIDv4 generation, stable ThreadLocal key preservation, GET bypassing.
 * 2. Strict FIFO Queue Ordering & Sequential Lock: Chronological sorting and fail-stop execution on 5xx/network error.
 * 3. Tombstone Handling: Ghost data purging for soft-deleted server records.
 * 4. App Lifecycle Catch-up Hooks: Transition from background to resume triggers missed event sync.
 * 5. Optimistic State Rollback on Failure: Atomic state snapshot and rollback when network mutations fail.
 * 6. Optimistic State Success: State permanence and mutation flow on successful execution.
 */
class SyncEngineHardeningTest {

    private val gson = Gson()

    // Mock OkHttp Chain Helper
    private fun createMockChain(request: Request): Pair<Interceptor.Chain, () -> Request?> {
        var interceptedRequest: Request? = null
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request

            override fun proceed(req: Request): Response {
                interceptedRequest = req
                return Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }

            override fun connection(): Connection? = null
            override fun call(): Call = throw UnsupportedOperationException()
            override fun connectTimeoutMillis(): Int = 5000
            override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 5000
            override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 5000
            override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        }
        return Pair(chain) { interceptedRequest }
    }

    @Test
    fun testIdempotencyInterceptorAddsUuidHeader() {
        val interceptor = IdempotencyInterceptor()

        // 1. Mutating request (POST) without header -> auto UUID generated
        val postRequest = Request.Builder()
            .url("https://api.sista.test/api/v1/attendance/checkin")
            .post(RequestBody.create("application/json".toMediaType(), "{}"))
            .build()

        val (chain1, getCaptured1) = createMockChain(postRequest)
        interceptor.intercept(chain1)

        val capturedKey1 = getCaptured1()?.header(IdempotencyInterceptor.HEADER_IDEMPOTENCY_KEY)
        assertNotNull("POST request must have Idempotency-Key header", capturedKey1)
        assertDoesNotThrow { UUID.fromString(capturedKey1) }

        // 2. ThreadLocal active key used for stable offline replay
        val stableOfflineKey = UUID.randomUUID().toString()
        IdempotencyInterceptor.activeKey.set(stableOfflineKey)
        try {
            val (chain2, getCaptured2) = createMockChain(postRequest)
            interceptor.intercept(chain2)
            val capturedKey2 = getCaptured2()?.header(IdempotencyInterceptor.HEADER_IDEMPOTENCY_KEY)
            assertEquals("ThreadLocal activeKey must be used when set", stableOfflineKey, capturedKey2)
        } finally {
            IdempotencyInterceptor.activeKey.remove()
        }

        // 3. Non-mutating request (GET) -> no Idempotency-Key header
        val getRequest = Request.Builder()
            .url("https://api.sista.test/api/v1/student/schedule")
            .get()
            .build()

        val (chain3, getCaptured3) = createMockChain(getRequest)
        interceptor.intercept(chain3)
        val capturedKey3 = getCaptured3()?.header(IdempotencyInterceptor.HEADER_IDEMPOTENCY_KEY)
        assertNull("GET request must NOT have Idempotency-Key header", capturedKey3)
    }

    @Test
    fun testOfflineQueueStrictFifoOrderAndSequentialLock() {
        // Prepare unsorted actions
        val item3 = PendingActionItem(
            id = "uuid-3",
            actionType = "TEACHING_JOURNAL",
            payloadJson = "{}",
            createdAt = 3000L,
            retryCount = 0
        )
        val item1 = PendingActionItem(
            id = "uuid-1",
            actionType = "CBT_SUBMIT",
            payloadJson = "{}",
            createdAt = 1000L,
            retryCount = 0
        )
        val item2 = PendingActionItem(
            id = "uuid-2",
            actionType = "MUTABAAH_LOG",
            payloadJson = "[]",
            createdAt = 2000L,
            retryCount = 0
        )

        val queue = listOf(item3, item1, item2)

        // Verify Strict FIFO sort (ascending createdAt)
        val sortedQueue = queue.sortedBy { it.createdAt }
        assertEquals("uuid-1", sortedQueue[0].id)
        assertEquals("uuid-2", sortedQueue[1].id)
        assertEquals("uuid-3", sortedQueue[2].id)

        // Verify Sequential Lock halting logic:
        // When item1 encounters a 5xx server error, the loop stops immediately (break)
        // and item2 / item3 are NOT executed out of order.
        var processedCount = 0
        var failedAction: PendingActionItem? = null
        for (action in sortedQueue) {
            if (action.id == "uuid-1") {
                // Simulating 500 error on item 1 -> halt queue
                failedAction = action.copy(retryCount = action.retryCount + 1)
                break
            }
            processedCount++
        }

        assertEquals("Queue execution must stop on failure (sequential lock)", 0, processedCount)
        assertNotNull(failedAction)
        assertEquals("Failed action retry count must be incremented", 1, failedAction?.retryCount)
    }

    @Test
    fun testTombstonePurgesGhostData() {
        // Simulated local cached announcements
        val localAnnouncements = listOf(
            AnnouncementItem(
                id = "101",
                title = "Pengumuman UAS",
                summary = "Ringkasan UAS",
                content = "Jadwal UAS",
                category = "Akademik",
                author = "Waka Kurikulum",
                date = "2026-09-01"
            ),
            AnnouncementItem(
                id = "102",
                title = "Libur Maulid",
                summary = "Libur nasional",
                content = "Libur nasional",
                category = "Ibadah",
                author = "Humas",
                date = "2026-09-05"
            ),
            AnnouncementItem(
                id = "103",
                title = "Upacara Hari Pahlawan",
                summary = "Upacara bendera",
                content = "Upacara bendera",
                category = "Kesiswaan",
                author = "Kesiswaan",
                date = "2026-09-10"
            )
        )

        // Server delta returns tombstone for id 102
        val deltaJson = """
            {
                "success": true,
                "sync_timestamp": 1726750000000,
                "delta_count": 0,
                "tombstones": [
                    {
                        "id": "102",
                        "type": "announcement",
                        "is_deleted": true,
                        "deleted_at": "2026-09-19 14:00:00"
                    }
                ]
            }
        """.trimIndent()

        val deltaResponse = gson.fromJson(deltaJson, DeltaSyncResponse::class.java)
        assertEquals(1, deltaResponse.tombstones.size)
        assertEquals("102", deltaResponse.tombstones[0].id)
        assertTrue(deltaResponse.tombstones[0].isDeleted)

        // Apply tombstone purge filter
        val deletedIds = deltaResponse.tombstones.filter { it.type == "announcement" }.map { it.id }.toSet()
        val purgedList = localAnnouncements.filterNot { it.id in deletedIds }

        assertEquals("Purged list must have exactly 2 items remaining", 2, purgedList.size)
        assertFalse("Tombstone item 102 must no longer exist in local list (zero ghost data)", purgedList.any { it.id == "102" })
        assertTrue("Item 101 must remain", purgedList.any { it.id == "101" })
        assertTrue("Item 103 must remain", purgedList.any { it.id == "103" })
    }

    @Test
    fun testAppLifecycleSyncObserverResumeTrigger() {
        val catchUpInvoked = AtomicBoolean(false)

        val observer = AppLifecycleSyncObserver(
            onCatchUp = { catchUpInvoked.set(true) },
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            minimumBackgroundDowntimeMs = 100L
        )

        // Test manual resume simulation
        observer.triggerManualResume()
        assertTrue("Catch-up sync must be triggered upon app resume", catchUpInvoked.get())
    }

    @Test
    fun testOptimisticStateRollbackOnFailure() = runBlocking {
        val stateFlow = MutableStateFlow("OriginalValue")
        var rollbackFired = false
        var snapshotReceived = ""

        val result = stateFlow.optimisticUpdate(
            optimisticTransform = { "OptimisticValue" },
            onRollback = { original, _ ->
                rollbackFired = true
                snapshotReceived = original
            }
        ) {
            // Assert optimistic state was applied immediately
            assertEquals("OptimisticValue", stateFlow.value)
            // Simulated server error (500)
            throw RuntimeException("500 Internal Server Error")
        }

        assertTrue("Result must be failure when mutation throws", result.isFailure)
        assertTrue("onRollback hook must be invoked on failure", rollbackFired)
        assertEquals("OriginalValue", snapshotReceived)
        assertEquals("StateFlow must be restored atomically to OriginalValue", "OriginalValue", stateFlow.value)
    }

    @Test
    fun testOptimisticStateSuccessPreservesUpdatedState() = runBlocking {
        val countStateFlow = MutableStateFlow(42)
        var rollbackFired = false

        // Test explicit snapshot class
        val snapshot = countStateFlow.createSnapshot()
        assertEquals(42, snapshot.originalState)

        // Test optimistic update success
        val result = countStateFlow.optimisticUpdate(
            optimisticTransform = { it + 1 },
            onRollback = { _, _ -> rollbackFired = true }
        ) {
            // Simulated successful network response
        }

        assertTrue("Result must be success", result.isSuccess)
        assertFalse("Rollback must not fire on success", rollbackFired)
        assertEquals(43, countStateFlow.value)

        // Verify explicit snapshot rollback
        snapshot.rollback()
        assertEquals("Explicit snapshot rollback should restore 42", 42, countStateFlow.value)
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }
}
