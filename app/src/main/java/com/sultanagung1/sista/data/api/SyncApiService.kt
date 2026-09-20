package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.CatchUpResponse
import com.sultanagung1.sista.data.model.DeltaSyncResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * FASE 61: Sync & Consistency REST API Service.
 */
interface SyncApiService {

    @GET("sync/delta")
    suspend fun getDelta(
        @Query("since") sinceTimestamp: Long
    ): Response<DeltaSyncResponse>

    @GET("sync/catch-up")
    suspend fun getCatchUp(
        @Query("last_event_id") lastEventId: String = "0"
    ): Response<CatchUpResponse>
}
