package com.sultanagung1.sista.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * FASE 61.1: Automatic Idempotency Key Injection.
 *
 * Ensures all mutating HTTP requests (POST, PUT, PATCH, DELETE) carry an
 * "Idempotency-Key" header (UUIDv4). If already present or set via [activeKey],
 * the existing key is preserved for strict deduplication at the backend.
 */
class IdempotencyInterceptor : Interceptor {

    companion object {
        const val HEADER_IDEMPOTENCY_KEY = "Idempotency-Key"

        /**
         * ThreadLocal storage for offline replay queue or explicit callers
         * wanting to specify a stable Idempotency-Key without modifying Retrofit interfaces.
         */
        val activeKey: ThreadLocal<String?> = ThreadLocal()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.method.uppercase()

        // Only inject for state-mutating requests
        if (method in listOf("POST", "PUT", "PATCH", "DELETE")) {
            val existingHeader = request.header(HEADER_IDEMPOTENCY_KEY)
            val threadLocalKey = activeKey.get()

            val effectiveKey = when {
                !existingHeader.isNullOrBlank() -> existingHeader
                !threadLocalKey.isNullOrBlank() -> threadLocalKey
                else -> UUID.randomUUID().toString()
            }

            val newRequest = request.newBuilder()
                .header(HEADER_IDEMPOTENCY_KEY, effectiveKey)
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(request)
    }
}
