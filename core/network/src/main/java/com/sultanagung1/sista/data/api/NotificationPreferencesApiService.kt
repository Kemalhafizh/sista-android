package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.NotificationPreferences
import com.sultanagung1.sista.data.model.NotificationPreferencesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface NotificationPreferencesApiService {

    @GET("notifications/preferences")
    suspend fun getPreferences(): Response<NotificationPreferencesResponse>

    @PUT("notifications/preferences")
    suspend fun updatePreferences(@Body preferences: NotificationPreferences): Response<NotificationPreferencesResponse>
}
