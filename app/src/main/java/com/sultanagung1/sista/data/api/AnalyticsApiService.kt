package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AnalyticsApiService {

    @GET("analytics/student/summary")
    suspend fun getStudentAnalytics(): Response<StudentAnalyticsData>

    @GET("analytics/teacher/class-performance")
    suspend fun getClassAnalytics(
        @Query("class") className: String,
        @Query("subject") subjectName: String
    ): Response<ClassAnalyticsData>

    @GET("analytics/parent/child-progress")
    suspend fun getParentProgress(): Response<ParentProgressData>

    @GET("analytics/executive/kpi")
    suspend fun getExecutiveKpi(): Response<ExecutiveAnalyticsData>
}
