package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Teacher / Guru API Service — matches ApiTeacherController (routes/api.php
 * prefix('teacher'), role:guru,bk). Schedule and journal endpoints live on
 * [TeachingJournalMobileApiService] since both features share the same
 * underlying data.
 */
interface TeacherApiService {
    @GET("teacher/classes")
    suspend fun getTeacherClasses(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<TeacherClassSummary>>>

    @GET("teacher/classes/{classroomId}/students")
    suspend fun getClassStudents(@Path("classroomId") classroomId: Long): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<TeacherClassStudent>>>

    @POST("teacher/attendance")
    suspend fun submitClassAttendance(@Body request: SubmitClassAttendanceRequest): Response<com.sultanagung1.sista.core.network.ApiEnvelope<AttendanceRecordedResponse>>
}

/**
 * Parent / Wali Murid API Service — matches ApiParentController (routes/api.php
 * prefix('parent'), role:parent).
 */
interface ParentApiService {
    @GET("parent/children")
    suspend fun getChildren(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<ParentChildItem>>>

    @GET("parent/child/{uuid}/summary")
    suspend fun getChildSummary(@Path("uuid") uuid: String): Response<com.sultanagung1.sista.core.network.ApiEnvelope<ChildSummaryResponse>>

    @GET("parent/child/{uuid}/attendance")
    suspend fun getChildAttendanceHistory(@Path("uuid") uuid: String): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<ChildAttendanceLog>>>

    @GET("parent/child/{uuid}/grades")
    suspend fun getChildGrades(@Path("uuid") uuid: String): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<ChildGradeItem>>>
}

/**
 * Admin / Principal / Kepsek API Service
 */
interface AdminApiService {
    @GET("mobile/admin/dashboard")
    suspend fun getAdminDashboard(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<AdminDashboardData>>

    @GET("mobile/admin/kpi")
    suspend fun getSchoolKpi(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<SchoolKpiSummary>>

    // FASE 71.4 follow-up: real list backing "Persetujuan Menunggu Tindakan"
    // — previously only the count from getAdminDashboard() existed.
    @GET("mobile/admin/approvals")
    suspend fun getPendingApprovals(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<PendingApprovalItem>>>

    @POST("mobile/admin/approvals/{id}/action")
    suspend fun processApproval(
        @Path("id") id: String,
        @Query("action") action: String // approve / reject
    ): Response<Map<String, Any>>

    // FASE 71.4 "Tombol Siaran Darurat" — pushes an EmergencyBroadcastEvent to
    // every connected mobile app over the campus.emergency WebSocket channel.
    @POST("mobile/admin/emergency-broadcast")
    suspend fun broadcastEmergency(
        @Body request: EmergencyBroadcastRequest
    ): Response<com.sultanagung1.sista.core.network.ApiEnvelope<EmergencyBroadcastData>>
}
