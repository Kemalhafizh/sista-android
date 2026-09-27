package com.sultanagung1.sista.core.telemetry

import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp Interceptor untuk mengukur performa jaringan (HTTP Latency, Status Code, Payload size),
 * mencatat breadcrumbs, dan mendeteksi peringatan Slow Requests (>2000ms).
 */
class NetworkPerformanceInterceptor(
    private val hub: SulaoneTelemetryHub = SulaoneTelemetryHub.instance
) : Interceptor {

    private val tag = "NetworkPerformance"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalUrl = request.url.toString()
        val sanitizedUrl = TelemetrySanitizer.sanitizeUrl(originalUrl)
        val path = request.url.encodedPath

        val startMs = System.currentTimeMillis()
        var httpMetric: HttpMetric? = null

        try {
            if (hub.firebaseSink?.isAvailable() == true) {
                httpMetric = FirebasePerformance.getInstance().newHttpMetric(sanitizedUrl, request.method)
                httpMetric.start()
            }
        } catch (e: Throwable) {
            // Safe fallback jika Firebase Perf belum siap
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            val durationMs = System.currentTimeMillis() - startMs
            httpMetric?.stop()

            hub.logBreadcrumb(
                category = BreadcrumbCategory.NETWORK,
                message = "Network Failure on ${request.method} $path: ${e.message} (${durationMs}ms)",
                level = BreadcrumbLevel.ERROR,
                data = mapOf(
                    "method" to request.method,
                    "url" to sanitizedUrl,
                    "duration_ms" to durationMs.toString(),
                    "error" to (e.message ?: "IOException")
                )
            )
            throw e
        }

        val durationMs = System.currentTimeMillis() - startMs
        val statusCode = response.code

        try {
            httpMetric?.setHttpResponseCode(statusCode)
            request.body?.contentLength()?.let { len ->
                if (len > 0) httpMetric?.setRequestPayloadSize(len)
            }
            response.body?.contentLength()?.let { len ->
                if (len > 0) httpMetric?.setResponsePayloadSize(len)
            }
            httpMetric?.stop()
        } catch (e: Throwable) {
            // Ignore Firebase metric reporting errors
        }

        // Klasifikasikan tingkat keparahan log
        val level = when {
            statusCode >= 400 -> BreadcrumbLevel.ERROR
            durationMs > SLOW_REQUEST_THRESHOLD_MS -> BreadcrumbLevel.WARN
            else -> BreadcrumbLevel.DEBUG
        }

        val logMessage = when {
            durationMs > SLOW_REQUEST_THRESHOLD_MS ->
                "⚠️ Slow HTTP Request: ${request.method} $path took ${durationMs}ms (HTTP $statusCode)"
            statusCode >= 400 ->
                "🚨 HTTP Error $statusCode on ${request.method} $path (${durationMs}ms)"
            else ->
                "HTTP $statusCode ${request.method} $path in ${durationMs}ms"
        }

        hub.logBreadcrumb(
            category = BreadcrumbCategory.NETWORK,
            message = logMessage,
            level = level,
            data = mapOf(
                "method" to request.method,
                "path" to path,
                "status_code" to statusCode.toString(),
                "duration_ms" to durationMs.toString()
            )
        )

        return response
    }

    companion object {
        const val SLOW_REQUEST_THRESHOLD_MS = 2000L
    }
}
