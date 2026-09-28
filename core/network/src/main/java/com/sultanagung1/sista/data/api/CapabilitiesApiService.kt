package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.Capabilities
import retrofit2.Response
import retrofit2.http.GET

interface CapabilitiesApiService {

    /** The features this account may use (backend MobileCapabilities). */
    @GET("me/capabilities")
    suspend fun getCapabilities(): Response<ApiEnvelope<Capabilities>>
}
