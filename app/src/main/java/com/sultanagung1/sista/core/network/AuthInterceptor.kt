package com.sultanagung1.sista.core.network

import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url.toString()

        // Normalize accidental double /api/v1/api/v1/ pathing
        val normalizedUrl = if (originalUrl.contains("/api/v1/api/v1/")) {
            originalUrl.replace("/api/v1/api/v1/", "/api/v1/").toHttpUrl()
        } else {
            originalRequest.url
        }

        val requestBuilder = originalRequest.newBuilder().url(normalizedUrl)
        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("X-App-Client", "Sulaone-Android-Native")
        requestBuilder.addHeader("X-SISTA-Platform", "android")

        val token = runBlocking {
            sessionManager.authTokenFlow.firstOrNull()
        }

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

