package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.ChildActivityFeedResponse
import com.sultanagung1.sista.data.model.ChildComparisonResponse
import com.sultanagung1.sista.data.model.WeeklyDigestResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ParentExperienceApiService {

    // {child} is the child's uuid from parent/children; the server checks it is this parent's child.
    @GET("parent/child/{child}/feed")
    suspend fun getChildFeed(@Path("child") child: String): Response<ChildActivityFeedResponse>

    @GET("parent/child/{child}/digest")
    suspend fun getWeeklyDigest(@Path("child") child: String): Response<WeeklyDigestResponse>

    @GET("parent/child/{child}/benchmark")
    suspend fun getChildComparison(@Path("child") child: String): Response<ChildComparisonResponse>
}
