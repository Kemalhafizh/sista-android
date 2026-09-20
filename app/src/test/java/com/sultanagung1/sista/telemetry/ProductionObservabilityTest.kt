package com.sultanagung1.sista.telemetry

import com.sultanagung1.sista.core.telemetry.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ProductionObservabilityTest {

    @Before
    fun setUp() {
        AppStartupTracker.resetForTesting()
    }

    @Test
    fun testBreadcrumbBufferRingCapacityAndFifoEviction() {
        val buffer = BreadcrumbBuffer(capacity = 3)
        assertEquals(0, buffer.size())

        buffer.add(Breadcrumb(category = BreadcrumbCategory.NAVIGATION, message = "Nav 1"))
        buffer.add(Breadcrumb(category = BreadcrumbCategory.NAVIGATION, message = "Nav 2"))
        buffer.add(Breadcrumb(category = BreadcrumbCategory.NAVIGATION, message = "Nav 3"))
        assertEquals(3, buffer.size())

        // Add 4th item -> should evict "Nav 1"
        buffer.add(Breadcrumb(category = BreadcrumbCategory.NETWORK, message = "Net 4"))
        assertEquals(3, buffer.size())

        val snapshot = buffer.getSnapshot()
        assertEquals("Nav 2", snapshot[0].message)
        assertEquals("Nav 3", snapshot[1].message)
        assertEquals("Net 4", snapshot[2].message)

        buffer.clear()
        assertEquals(0, buffer.size())
    }

    @Test
    fun testTelemetrySanitizerRedactsSensitiveData() {
        // 1. URL Query Params
        val sensitiveUrl = "https://sultanagung1.sch.id/api/v1/auth?password=supersecret&token=jwt12345&nisn=0071829102&role=student"
        val sanitizedUrl = TelemetrySanitizer.sanitizeUrl(sensitiveUrl)
        assertFalse(sanitizedUrl.contains("supersecret"))
        assertFalse(sanitizedUrl.contains("jwt12345"))
        assertFalse(sanitizedUrl.contains("0071829102"))
        assertTrue(sanitizedUrl.contains("password=[REDACTED]"))
        assertTrue(sanitizedUrl.contains("token=[REDACTED]"))
        assertTrue(sanitizedUrl.contains("nisn=[REDACTED]"))
        assertTrue(sanitizedUrl.contains("role=student"))

        // 2. Bearer Header
        val rawHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val sanitizedBearer = TelemetrySanitizer.sanitizeString(rawHeader)
        assertEquals("Bearer [REDACTED]", sanitizedBearer)

        // 3. Map of Headers
        val headers = mapOf(
            "Authorization" to "Bearer secretToken",
            "Cookie" to "session_id=987123",
            "Content-Type" to "application/json"
        )
        val sanitizedHeaders = TelemetrySanitizer.sanitizeHeaders(headers)
        assertEquals("[REDACTED]", sanitizedHeaders["Authorization"])
        assertEquals("[REDACTED]", sanitizedHeaders["Cookie"])
        assertEquals("application/json", sanitizedHeaders["Content-Type"])

        // 4. Sensitive keys check
        assertTrue(TelemetrySanitizer.isSensitiveKey("authorization"))
        assertTrue(TelemetrySanitizer.isSensitiveKey("X-CSRF-TOKEN"))
        assertTrue(TelemetrySanitizer.isSensitiveKey("student_pin"))
        assertFalse(TelemetrySanitizer.isSensitiveKey("academic_year"))
    }

    @Test
    fun testCrashReportSerializationAndDeserialization() {
        val report = CrashReport(
            reportId = "test-uuid-123",
            timestampMs = 1716000000000L,
            exceptionType = "java.lang.NullPointerException",
            exceptionMessage = "Attempt to invoke virtual method on a null object reference",
            stackTrace = "java.lang.NullPointerException at com.sultanagung1.sista.Test.run(Test.kt:12)",
            isFatal = true,
            deviceInfo = mapOf("model" to "POCO F5", "manufacturer" to "Xiaomi", "ram_mb" to "7800"),
            appInfo = mapOf("version_name" to "2.0", "package" to "com.sultanagung1.sista"),
            userContext = mapOf("user_role" to "teacher", "user_id" to "19850412"),
            breadcrumbs = listOf("[INFO][NAVIGATION] Screen: Home", "[WARN][NETWORK] Slow request 2100ms"),
            isSent = false
        )

        val jsonStr = report.toJsonString()
        assertTrue(jsonStr.contains("test-uuid-123"))
        assertTrue(jsonStr.contains("NullPointerException"))
        assertTrue(jsonStr.contains("POCO F5"))

        // Roundtrip deserialization
        val deserialized = CrashReport.fromJsonString(jsonStr)
        assertEquals(report.reportId, deserialized.reportId)
        assertEquals(report.exceptionType, deserialized.exceptionType)
        assertEquals(report.exceptionMessage, deserialized.exceptionMessage)
        assertEquals(report.deviceInfo["model"], deserialized.deviceInfo["model"])
        assertEquals(report.userContext["user_role"], deserialized.userContext["user_role"])
        assertEquals(2, deserialized.breadcrumbs.size)
    }

    @Test
    fun testAppStartupTrackerCalculatesColdStart() {
        assertFalse(AppStartupTracker.isCompleted())
        assertEquals(-1L, AppStartupTracker.getColdStartDurationMs())

        AppStartupTracker.recordAppStart()
        // Simulate small delay
        Thread.sleep(15)
        AppStartupTracker.recordFirstDraw()

        assertTrue(AppStartupTracker.isCompleted())
        val duration = AppStartupTracker.getColdStartDurationMs()
        assertTrue("Cold start duration should be positive, was: $duration", duration >= 1L)
    }

    @Test
    fun testSulaoneTelemetryHubBreadcrumbAndUserContext() {
        val hub = SulaoneTelemetryHub.instance
        hub.logBreadcrumb(
            category = BreadcrumbCategory.USER_ACTION,
            message = "User clicked attendance submit button",
            level = BreadcrumbLevel.INFO,
            data = mapOf("button_id" to "btn_checkin")
        )

        val recent = hub.getRecentBreadcrumbs()
        assertTrue(recent.isNotEmpty())
        val latest = recent.last()
        assertEquals(BreadcrumbCategory.USER_ACTION, latest.category)
        assertTrue(latest.message.contains("attendance submit button"))
        assertEquals("btn_checkin", latest.data["button_id"])

        // Test non-fatal exception record
        val simulatedException = IllegalStateException("Mock test exception")
        hub.recordException(simulatedException, isFatal = false, extraData = mapOf("test_tag" to "unit_test"))

        val afterException = hub.getRecentBreadcrumbs()
        val exceptionBreadcrumb = afterException.last()
        assertEquals(BreadcrumbCategory.SYSTEM, exceptionBreadcrumb.category)
        assertEquals(BreadcrumbLevel.ERROR, exceptionBreadcrumb.level)
        assertTrue(exceptionBreadcrumb.message.contains("IllegalStateException"))
    }

    @Test
    fun testNetworkPerformanceInterceptorWithMockChain() {
        val hub = SulaoneTelemetryHub.instance
        val interceptor = NetworkPerformanceInterceptor(hub)

        val mockRequest = Request.Builder()
            .url("https://sultanagung1.sch.id/api/v1/schedule/today?token=secret123")
            .get()
            .build()

        val mockChain = object : Interceptor.Chain {
            override fun request(): Request = mockRequest
            override fun proceed(request: Request): Response {
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw UnsupportedOperationException()
            override fun connectTimeoutMillis(): Int = 5000
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 5000
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 5000
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        val response = interceptor.intercept(mockChain)
        assertEquals(200, response.code)

        val breadcrumbs = hub.getRecentBreadcrumbs()
        val netBc = breadcrumbs.find { it.category == BreadcrumbCategory.NETWORK }
        assertNotNull("Network breadcrumb should be recorded", netBc)
        assertTrue(netBc!!.message.contains("HTTP 200"))
        assertEquals("/api/v1/schedule/today", netBc.data["path"])
    }

    @Test
    fun testCrashReportRepositoryExportSummary() {
        val report = CrashReport(
            reportId = "ticket-9988",
            timestampMs = System.currentTimeMillis(),
            exceptionType = "ArithmeticException",
            exceptionMessage = "/ by zero in calculation",
            stackTrace = "java.lang.ArithmeticException: / by zero\n\tat com.sultanagung1.sista.MathHelper.div(MathHelper.kt:5)",
            isFatal = true,
            deviceInfo = mapOf("model" to "POCO F5", "battery" to "85%"),
            appInfo = mapOf("version" to "2.0"),
            userContext = mapOf("role" to "student"),
            breadcrumbs = listOf("[NAVIGATION] Home -> Profile")
        )

        // Temporary directory repo test
        val tempDir = Files.createTempDirectory("crash_test").toFile()
        val mockContext = object : android.content.ContextWrapper(null) {
            override fun getFilesDir(): File = tempDir
        }

        val repo = CrashReportRepository(mockContext)
        val saved = repo.saveCrashReport(report)
        assertTrue(saved)

        val retrieved = repo.getLatestCrashReport()
        assertNotNull(retrieved)
        assertEquals("ticket-9988", retrieved!!.reportId)

        val summary = repo.exportReportSummary(retrieved)
        assertTrue(summary.contains("LAPORAN DIAGNOSTIK KENDALA"))
        assertTrue(summary.contains("ArithmeticException"))
        assertTrue(summary.contains("POCO F5"))

        repo.clearAllReports()
        assertEquals(0, repo.getAllCrashReports().size)
        tempDir.deleteRecursively()
    }
}
