package com.sultanagung1.sista.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Tells the server which language to answer in (id, en or ar). The server
 * prefers the language saved on the account; this header covers requests
 * before signing in and the moment right after a change.
 */
class LanguageInterceptor(private val language: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("Accept-Language", language()).build())
}
