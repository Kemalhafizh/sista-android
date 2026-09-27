package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.AttendanceReportDto
import com.sultanagung1.sista.data.model.BulkAttendanceRequest
import com.sultanagung1.sista.data.model.BulkAttendanceResultDto
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionPageDto
import com.sultanagung1.sista.data.model.ClassSessionQrDto
import com.sultanagung1.sista.data.model.EndClassSessionRequest
import com.sultanagung1.sista.data.model.OverrideAttendanceRequest
import com.sultanagung1.sista.data.model.ScanClassQrRequest
import com.sultanagung1.sista.data.model.ScanQrResultDto
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.StartClassSessionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * FASE 77 ↔ backend FASE 117 (Sesi Kelas Hidup). Contract: android_implementation.md
 * → "77.0 Kontrak API". Paths sit under the existing `api/v1` base URL and the
 * backend's role groups: `teacher/…` (guru, bk), `student/…` (student, siswa),
 * `admin/class-sessions/…` (see the contract for the exact roles).
 *
 * A manual mark for one student is a bulk request with one row, so the app
 * does not need the single-student `attendance/manual` route.
 */
interface ClassSessionApiService {

    // ── Guru ────────────────────────────────────────────────────────────

    @GET("teacher/class-sessions/today")
    suspend fun getTodaySessions(): Response<ApiEnvelope<List<ClassSessionDto>>>

    @POST("teacher/class-sessions/{scheduleId}/start")
    suspend fun startSession(
        @Path("scheduleId") scheduleId: Long,
        @Body request: StartClassSessionRequest
    ): Response<ApiEnvelope<ClassSessionDto>>

    @POST("teacher/class-sessions/{sessionId}/end")
    suspend fun endSession(
        @Path("sessionId") sessionId: Long,
        @Body request: EndClassSessionRequest
    ): Response<ApiEnvelope<ClassSessionDto>>

    @GET("teacher/class-sessions/{sessionId}/qr")
    suspend fun getActiveQr(
        @Path("sessionId") sessionId: Long
    ): Response<ApiEnvelope<ClassSessionQrDto>>

    @GET("teacher/class-sessions/{sessionId}/students")
    suspend fun getSessionStudents(
        @Path("sessionId") sessionId: Long
    ): Response<ApiEnvelope<List<SessionAttendanceDto>>>

    @POST("teacher/class-sessions/{sessionId}/attendance/bulk")
    suspend fun bulkMarkAttendance(
        @Path("sessionId") sessionId: Long,
        @Body request: BulkAttendanceRequest
    ): Response<ApiEnvelope<BulkAttendanceResultDto>>

    // ── Siswa ───────────────────────────────────────────────────────────

    @GET("student/class-session/active")
    suspend fun getActiveSessionForStudent(): Response<ApiEnvelope<ActiveClassSessionDto?>>

    @POST("student/class-session/scan-qr")
    suspend fun scanQr(
        @Body request: ScanClassQrRequest
    ): Response<ApiEnvelope<ScanQrResultDto>>

    // ── Admin / Waka Kurikulum / TU ─────────────────────────────────────

    @GET("admin/class-sessions")
    suspend fun getAdminSessions(
        @Query("date") date: String?,
        @Query("status") status: String?,
        @Query("page") page: Int
    ): Response<ApiEnvelope<ClassSessionPageDto>>

    @GET("admin/class-sessions/{sessionId}/attendances")
    suspend fun getSessionAttendances(
        @Path("sessionId") sessionId: Long
    ): Response<ApiEnvelope<List<SessionAttendanceDto>>>

    @PUT("admin/class-sessions/attendances/{attendanceId}/override")
    suspend fun overrideAttendance(
        @Path("attendanceId") attendanceId: Long,
        @Body request: OverrideAttendanceRequest
    ): Response<ApiEnvelope<SessionAttendanceDto>>

    @GET("admin/class-sessions/reports")
    suspend fun getAttendanceReport(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("group_by") groupBy: String
    ): Response<ApiEnvelope<AttendanceReportDto>>
}
