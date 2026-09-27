package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.MobileConfigResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MobileConfigApiService {
    @GET("mobile/config")
    suspend fun getConfig(
        @Query("platform") platform: String = "android",
        @Query("build") build: Int
    ): Response<MobileConfigResponse>
}
