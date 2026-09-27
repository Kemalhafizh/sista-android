package com.sultanagung1.sista.core.network

import com.google.gson.annotations.SerializedName

/**
 * Generic wrapper for the response shape every sistem-terpadu Laravel API
 * controller returns via ApiResponseTrait::successResponse() /
 * errorResponse(): { success, message, data }. Retrofit interfaces that
 * declare a bare payload type (e.g. Response<List<Foo>>) instead of
 * Response<ApiEnvelope<List<Foo>>> will fail Gson deserialization on every
 * real call — this was the root cause behind several screens silently
 * falling back to hardcoded mock data.
 */
data class ApiEnvelope<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)
