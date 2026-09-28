package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AnalyticsApiService {

    @GET("analytics/student/summary")
    suspend fun getStudentAnalytics(): Response<StudentAnalyticsData>

    @GET("analytics/teacher/my-classes")
    suspend fun getTeacherClasses(): Response<List<TeacherClassOption>>

    @GET("analytics/teacher/class-performance")
    suspend fun getClassAnalytics(
        @Query("class") className: String,
        @Query("subject") subjectName: String,
        @Query("classroom_id") classroomId: Long? = null,
        @Query("subject_id") subjectId: Long? = null,
    ): Response<ClassAnalyticsData>

    /** [studentUuid] null = the first child linked to this account. */
    @GET("analytics/parent/child-progress")
    suspend fun getParentProgress(@Query("student_id") studentUuid: String? = null): Response<ParentProgressData>

    @GET("analytics/executive/kpi")
    suspend fun getExecutiveKpi(): Response<ExecutiveAnalyticsData>
}
