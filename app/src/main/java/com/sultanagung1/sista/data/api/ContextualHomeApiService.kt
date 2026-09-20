package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.ContextualHomeResponse
import com.sultanagung1.sista.data.model.SmartSuggestionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContextualHomeApiService {

    @GET("home/contextual")
    suspend fun getContextualHome(): Response<ContextualHomeResponse>

    @GET("home/suggestions")
    suspend fun getSmartSuggestions(): Response<SmartSuggestionsResponse>

    @POST("home/suggestions/{id}/dismiss")
    suspend fun dismissSuggestion(@Path("id") id: String): Response<Map<String, Any>>
}
