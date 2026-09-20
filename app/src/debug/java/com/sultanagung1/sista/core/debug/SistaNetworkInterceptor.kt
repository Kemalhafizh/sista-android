package com.sultanagung1.sista.core.debug

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class SistaNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isEnabled.get()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        requestCount.incrementAndGet()

        val startTime = System.currentTimeMillis()
        
        // Log Request
        val url = request.url
        val method = request.method
        val idempotencyKey = request.header("X-Idempotency-Key")
        val isWebSocket = request.header("Upgrade")?.equals("websocket", ignoreCase = true) == true
        
        val requestBody = request.body
        val requestContentLength = requestBody?.contentLength() ?: 0L
        if (requestContentLength > 0) {
            bytesSent.addAndGet(requestContentLength)
        }

        val requestLog = buildString {
            append("--> $method $url\n")
            request.headers.forEach { (name, value) ->
                if (name.equals("Authorization", ignoreCase = true)) {
                    append("$name: [REDACTED]\n")
                } else {
                    append("$name: $value\n")
                }
            }
        }
        Log.d(TAG, requestLog)

        if (idempotencyKey != null) {
            Log.d(TAG, "[SYNC] Idempotency: $idempotencyKey")
        }
        if (isWebSocket) {
            Log.d(TAG, "[WS] WebSocket upgrade to: $url")
        }

        if (requestBody != null) {
            try {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val bodyString = buffer.readUtf8()
                if (bodyString.contains("is_deleted")) {
                    Log.d(TAG, "[TOMBSTONE] Soft-delete detected")
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "<-- HTTP FAILED: $e")
            throw e
        }

        val duration = System.currentTimeMillis() - startTime
        val responseBody = response.body
        val responseContentLength = responseBody?.contentLength() ?: 0L
        if (responseContentLength > 0) {
            bytesReceived.addAndGet(responseContentLength)
        }

        val responseLog = "<-- ${response.code} ${response.message} $url (${duration}ms, $responseContentLength-byte body)"
        when (response.code) {
            in 200..299 -> Log.i(TAG, responseLog)
            in 400..499 -> Log.w(TAG, responseLog)
            in 500..599 -> Log.e(TAG, responseLog)
            else -> Log.d(TAG, responseLog)
        }

        return response
    }

    companion object {
        private const val TAG = "SISTA-NET"
        val isEnabled = AtomicBoolean(true)
        val requestCount = AtomicInteger(0)
        val bytesSent = AtomicLong(0L)
        val bytesReceived = AtomicLong(0L)
    }
}
