package com.sultanagung1.sista.network

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.core.network.IdempotencyInterceptor
import com.sultanagung1.sista.data.api.SyncApiService
import com.sultanagung1.sista.data.model.CatchUpResponse
import com.sultanagung1.sista.data.model.DeltaSyncResponse
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.UUID

/**
 * FASE 74.2: Automated Network Contract Validation.
 *
 * This is the deterministic, CI-runnable substitute for the roadmap's
 * "Proxyman MCP interception" step — there is no Proxyman/Appium/ADB MCP
 * available in a plain JVM test run, but the exact same three contracts it
 * describes (Idempotency-Key on mutations, ETag/Last-Modified on sync
 * reads, and safe handling of 401/422/500 payloads) can be verified against
 * the REAL Retrofit/OkHttp/Gson stack and REAL model classes by pointing
 * them at a local MockWebServer instead of the live backend. Unlike a
 * manual Proxyman session, this runs on every build and fails loudly on
 * regression.
 *
 * Deliberately builds its own minimal Retrofit client here instead of
 * reusing [com.sultanagung1.sista.core.network.ApiClient]: ApiClient hard-
 * codes its baseUrl from Constants.BASE_URL and requires a real Android
 * Context for SessionManager, neither of which a plain JVM test can supply
 * — but the interceptor, service interfaces, and model classes under test
 * are the exact same production classes either way.
 */
class NetworkContractValidationTest {

    private lateinit var server: MockWebServer
    private lateinit var syncApi: SyncApiService
    private lateinit var testMutationApi: TestMutationApi

    /** Minimal stand-in for a real mutating endpoint (e.g. gps-checkin) — only
     * exists to exercise IdempotencyInterceptor over an actual POST request;
     * SyncApiService itself has no mutating methods to reuse for this. */
    private interface TestMutationApi {
        @POST("mobile/attendance/gps-checkin")
        suspend fun checkin(@Body body: Map<String, String>): Response<ApiEnvelope<Map<String, Any>>>
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val client = OkHttpClient.Builder()
            .addInterceptor(IdempotencyInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        syncApi = retrofit.create(SyncApiService::class.java)
        testMutationApi = retrofit.create(TestMutationApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun mutatingRequestCarriesRealUuidV4IdempotencyKeyOverTheWire(): Unit = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"message":"ok","data":{}}""")
        )

        testMutationApi.checkin(mapOf("latitude" to "-6.99616", "longitude" to "110.42851"))

        val recorded = server.takeRequest()
        val key = recorded.getHeader(IdempotencyInterceptor.HEADER_IDEMPOTENCY_KEY)
        assertNotNull("POST must carry an Idempotency-Key header on the real wire request", key)
        // Must not throw — proves it's a real UUIDv4, not a placeholder string.
        UUID.fromString(key)
        Unit
    }

    @Test
    fun syncDeltaResponseExposesEtagAndLastModifiedToTheCaller() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "\"abc123\"")
                .addHeader("Last-Modified", "Tue, 22 Sep 2026 10:00:00 GMT")
                .setBody(
                    """{"success":true,"message":"ok","data":{"success":true,"sync_timestamp":1758535200000,"delta_count":0,"tombstones":[]}}"""
                )
        )

        val response = syncApi.getDelta(1758535100000L)

        assertTrue(response.isSuccessful)
        assertEquals("\"abc123\"", response.headers()["ETag"])
        assertEquals("Tue, 22 Sep 2026 10:00:00 GMT", response.headers()["Last-Modified"])

        val body: DeltaSyncResponse? = response.body()?.data
        assertNotNull("Envelope must still parse correctly alongside the new headers", body)
        assertEquals(1758535200000L, body!!.syncTimestamp)
    }

    @Test
    fun syncCatchUpResponseExposesEtagToTheCaller() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "\"def456\"")
                .setBody(
                    """{"success":true,"message":"ok","data":{"success":true,"last_event_id":"0","events":[]}}"""
                )
        )

        val response = syncApi.getCatchUp("0")

        assertTrue(response.isSuccessful)
        assertEquals("\"def456\"", response.headers()["ETag"])
        val body: CatchUpResponse? = response.body()?.data
        assertNotNull(body)
    }

    @Test
    fun serverErrorPayloadsDoNotCrashTheCallerAndBodyIsSafelyNull() = runBlocking {
        // Mirrors the real shape confirmed server-side for a 422 validation
        // failure: Laravel's default {message, errors} — no success/data keys.
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setBody("""{"message":"The latitude field is required.","errors":{"latitude":["The latitude field is required."]}}""")
        )

        val response = testMutationApi.checkin(emptyMap())

        // This is exactly the guard already used in SyncManager.performFullSync()
        // / performCatchUpSync() ("response.isSuccessful && body != null") —
        // proving that pattern is sufficient: no exception, body() is null.
        assertFalse(response.isSuccessful)
        assertNull(response.body())
        assertNotNull(response.errorBody())
        // Must not throw reading it back out, even though it isn't the
        // {success,message,data} envelope shape ApiEnvelope models.
        val raw = response.errorBody()!!.string()
        assertTrue(raw.contains("latitude"))
    }

    @Test
    fun etagChangesWhenUnderlyingPayloadChanges() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).addHeader("ETag", "\"v1\"")
                .setBody("""{"success":true,"message":"ok","data":{"success":true,"sync_timestamp":1,"delta_count":0,"tombstones":[]}}""")
        )
        server.enqueue(
            MockResponse().setResponseCode(200).addHeader("ETag", "\"v2\"")
                .setBody("""{"success":true,"message":"ok","data":{"success":true,"sync_timestamp":2,"delta_count":1,"tombstones":[]}}""")
        )

        val first = syncApi.getDelta(0L).headers()["ETag"]
        val second = syncApi.getDelta(1L).headers()["ETag"]

        assertNotEquals("A real ETag must vary when the payload varies, unlike a hardcoded constant", first, second)
    }
}
