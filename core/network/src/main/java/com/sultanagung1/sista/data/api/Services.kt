package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface StudentApiService {

    @GET("student/schedule")
    suspend fun getSchedule(): Response<ApiEnvelope<List<ScheduleItem>>>

    @GET("student/grades")
    suspend fun getGrades(): Response<ApiEnvelope<List<GradeEntry>>>

    @GET("student/billings")
    suspend fun getBillings(): Response<ApiEnvelope<List<BillingInvoice>>>

    @POST("student/billings/{id}/pay")
    suspend fun requestPaymentVa(
        @Path("id") billingId: Long,
        @Body body: Map<String, String>
    ): Response<ApiEnvelope<PaymentVaResponse>>

    @GET("student/mutabaah")
    suspend fun getMutabaah(): Response<ApiEnvelope<List<MutabaahLogItem>>>

    @GET("student/tahfidz")
    suspend fun getTahfidzHistory(): Response<ApiEnvelope<List<TahfidzLogItem>>>

    @GET("student/notifications")
    suspend fun getNotificationsSummary(): Response<ApiEnvelope<List<NotificationSummaryItem>>>
}

interface AttendanceApiService {

    @GET("student/attendance")
    suspend fun getAttendanceHistory(): Response<ApiEnvelope<List<AttendanceHistoryItem>>>
}

interface CbtApiService {

    @GET("student/cbt/exams")
    suspend fun getExams(): Response<ApiEnvelope<List<CbtExamItem>>>

    @GET("student/cbt/exams/{id}/questions")
    suspend fun getExamQuestions(
        @Path("id") examId: Long
    ): Response<ApiEnvelope<CbtExamQuestionsResponse>>

    @POST("student/cbt/exams/{id}/submit")
    suspend fun submitExam(
        @Path("id") examId: Long,
        @Body request: CbtSubmitRequest
    ): Response<ApiEnvelope<CbtSubmitResponse>>

    // === FASE 25: CBT Enterprise Maximization ===

    @GET("student/cbt/exams/{id}/encrypted-payload")
    suspend fun getEncryptedPayload(
        @Path("id") examId: Long
    ): Response<Map<String, Any>>

    @GET("student/cbt/exams/{id}/decryption-key")
    suspend fun getDecryptionKey(
        @Path("id") examId: Long
    ): Response<Map<String, Any>>

    @POST("student/cbt/exams/{id}/micro-sync")
    suspend fun microSyncAnswers(
        @Path("id") examId: Long,
        @Body request: CbtMicroSyncRequest
    ): Response<Map<String, Any>>

    @Multipart
    @POST("student/cbt/exams/{id}/upload-essay")
    suspend fun uploadEssay(
        @Path("id") examId: Long,
        @Part("question_id") questionId: okhttp3.RequestBody,
        @Part file: okhttp3.MultipartBody.Part,
        @Part("student_notes") studentNotes: okhttp3.RequestBody?
    ): Response<Map<String, Any>>

    // === FASE 26: Token Validation & Force Close ===

    @POST("student/cbt/exams/{id}/validate-token")
    suspend fun validateExamToken(
        @Path("id") examId: Long,
        @Body request: CbtTokenValidationRequest
    ): Response<ApiEnvelope<CbtTokenValidationResponse>>

    @POST("student/cbt/exams/{id}/force-close")
    suspend fun forceCloseExam(
        @Path("id") examId: Long,
        @Body request: CbtForceCloseRequest
    ): Response<Map<String, Any>>

    // === FASE 72.2: Live Proctoring — liveness heartbeat ===

    @POST("student/cbt/exams/{id}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("id") examId: Long
    ): Response<Map<String, Any>>

    @FormUrlEncoded
    @POST("student/cbt/exams/{id}/log-violation")
    suspend fun logViolation(
        @Path("id") examId: Long,
        @Field("type") type: String
    ): Response<Map<String, Any>>

    // === FASE 87: Teacher Proctor — Live Token Distribution & Student Reset ===

    @GET("teacher/cbt/exams")
    suspend fun getTeacherProctorExams(): Response<CbtApiEnvelope<List<TeacherCbtExamItem>>>

    @GET("teacher/cbt/exams/{id}/token")
    suspend fun getProctorToken(
        @Path("id") examId: Long
    ): Response<CbtApiEnvelope<CbtTokenInfoResponse>>

    @POST("teacher/cbt/exams/{id}/token/regenerate")
    suspend fun regenerateProctorToken(
        @Path("id") examId: Long
    ): Response<CbtApiEnvelope<CbtTokenInfoResponse>>

    @GET("teacher/cbt/exams/{id}/locked-students")
    suspend fun getLockedStudents(
        @Path("id") examId: Long
    ): Response<CbtApiEnvelope<List<LockedExamStudent>>>

    @POST("teacher/cbt/exams/{id}/token/reset-student")
    suspend fun resetStudentAttempt(
        @Path("id") examId: Long,
        @Body request: CbtResetStudentRequest
    ): Response<CbtApiEnvelope<CbtResetStudentData>>

    // === Teacher manual exam authoring (ApiTeacherController) ===

    @POST("teacher/cbt/exams")
    suspend fun createTeacherExam(
        @Body request: TeacherCreateExamRequest
    ): Response<CbtApiEnvelope<TeacherCreatedExam>>

    @Multipart
    @POST("teacher/cbt/upload-image")
    suspend fun uploadExamImage(
        @Part image: MultipartBody.Part
    ): Response<CbtApiEnvelope<CbtQuestionImageUpload>>
}

interface GeneralApiService {

    @GET("announcements")
    suspend fun getAnnouncements(): Response<List<Map<String, Any>>>

    @GET("blockchain/credentials/my")
    suspend fun getMyBlockchainCredentials(): Response<List<BlockchainCredentialItem>>
}
