package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Teacher / Guru API Service
 */
interface TeacherApiService {
    @GET("mobile/teacher/dashboard")
    suspend fun getTeacherDashboard(): Response<TeacherDashboardData>

    @GET("mobile/teacher/classes")
    suspend fun getTeacherClasses(): Response<List<TeacherClassItem>>

    @GET("mobile/teacher/classes/{classId}/students")
    suspend fun getClassStudents(@Path("classId") classId: String): Response<List<StudentAttendanceInputItem>>

    @POST("mobile/teacher/attendance/submit")
    suspend fun submitClassAttendance(@Body request: ClassAttendanceSubmitRequest): Response<Map<String, Any>>

    @GET("mobile/teacher/journals")
    suspend fun getTeachingJournals(): Response<List<TeachingJournalItem>>

    @POST("mobile/teacher/journals/store")
    suspend fun storeTeachingJournal(@Body request: TeachingJournalCreateRequest): Response<TeachingJournalItem>
}

/**
 * Parent / Wali Murid API Service
 */
interface ParentApiService {
    @GET("mobile/parent/dashboard")
    suspend fun getParentDashboard(): Response<ParentDashboardData>

    @GET("mobile/parent/children")
    suspend fun getChildrenList(): Response<List<ChildSummary>>

    @GET("mobile/parent/children/{studentId}/attendance")
    suspend fun getChildAttendanceHistory(@Path("studentId") studentId: String): Response<List<ChildAttendanceLog>>

    @GET("mobile/parent/children/{studentId}/grades")
    suspend fun getChildGrades(@Path("studentId") studentId: String): Response<List<ChildGradeSubject>>
}

/**
 * Admin / Principal / Kepsek API Service
 */
interface AdminApiService {
    @GET("mobile/admin/dashboard")
    suspend fun getAdminDashboard(): Response<AdminDashboardData>

    @GET("mobile/admin/kpi")
    suspend fun getSchoolKpi(): Response<SchoolKpiSummary>

    @POST("mobile/admin/approvals/{id}/action")
    suspend fun processApproval(
        @Path("id") id: String,
        @Query("action") action: String // approve / reject
    ): Response<Map<String, Any>>
}
