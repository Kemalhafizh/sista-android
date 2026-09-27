package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.ChildActivityFeedResponse
import com.sultanagung1.sista.data.model.ChildComparisonResponse
import com.sultanagung1.sista.data.model.WeeklyDigestResponse
import retrofit2.Response
import retrofit2.http.GET

interface ParentExperienceApiService {

    @GET("parent/feed")
    suspend fun getChildFeed(): Response<ChildActivityFeedResponse>

    @GET("parent/weekly-digest")
    suspend fun getWeeklyDigest(): Response<WeeklyDigestResponse>

    @GET("parent/comparison")
    suspend fun getChildComparison(): Response<ChildComparisonResponse>
}
