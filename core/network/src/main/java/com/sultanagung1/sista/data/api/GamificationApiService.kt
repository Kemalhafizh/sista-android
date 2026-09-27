package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.BadgeCollectionResponse
import com.sultanagung1.sista.data.model.GamificationProfileResponse
import com.sultanagung1.sista.data.model.LeaderboardResponse
import com.sultanagung1.sista.data.model.XpHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GamificationApiService {

    @GET("gamification/profile")
    suspend fun getProfile(): Response<GamificationProfileResponse>

    @GET("gamification/leaderboard")
    suspend fun getLeaderboard(
        @Query("scope") scope: String? = null,
        @Query("period") period: String? = null
    ): Response<LeaderboardResponse>

    @GET("gamification/badges")
    suspend fun getBadges(): Response<BadgeCollectionResponse>

    @GET("gamification/history")
    suspend fun getHistory(): Response<XpHistoryResponse>
}
