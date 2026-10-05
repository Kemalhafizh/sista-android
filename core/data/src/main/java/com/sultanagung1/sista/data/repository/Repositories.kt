@file:Suppress("unused", "UNUSED_PARAMETER", "UNNECESSARY_NOT_NULL_ASSERTION", "UNUSED_VARIABLE")
package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.core.widget.WidgetSnapshotStore
import com.sultanagung1.sista.core.widget.WidgetSnapshots
import com.sultanagung1.sista.core.widget.withAttendance
import com.sultanagung1.sista.core.widget.withBilling
import com.sultanagung1.sista.data.local.dao.UserDao
import com.sultanagung1.sista.data.local.entity.UserEntity
import com.sultanagung1.sista.data.model.AdminDashboardData
import com.sultanagung1.sista.data.model.AiChatMessage
import com.sultanagung1.sista.data.model.AiMessageRequest
import com.sultanagung1.sista.data.model.AiTutorSessionData
import com.sultanagung1.sista.data.model.AiTutorSessionRequest
import com.sultanagung1.sista.data.model.AnnouncementItem
import com.sultanagung1.sista.data.model.AttendanceCheckinResponse
import com.sultanagung1.sista.data.model.AttendanceHistoryItem
import com.sultanagung1.sista.data.model.AttendanceRecordedResponse
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.model.BiometricChallengeRequest
import com.sultanagung1.sista.data.model.BiometricVerifyRequest
import com.sultanagung1.sista.data.model.BiometricVerifyResponse
import com.sultanagung1.sista.data.model.CbtExamItem
import com.sultanagung1.sista.data.model.CbtForceCloseRequest
import com.sultanagung1.sista.data.model.CbtForceCloseResponse
import com.sultanagung1.sista.data.model.CbtImageAttachment
import com.sultanagung1.sista.data.model.CbtMicroSyncRequest
import com.sultanagung1.sista.data.model.CbtQuestionImageUpload
import com.sultanagung1.sista.data.model.CbtResetStudentData
import com.sultanagung1.sista.data.model.CbtResetStudentRequest
import com.sultanagung1.sista.data.model.CbtSubmitRequest
import com.sultanagung1.sista.data.model.CbtSubmitResponse
import com.sultanagung1.sista.data.model.CbtTokenInfoResponse
import com.sultanagung1.sista.data.model.CbtTokenValidationRequest
import com.sultanagung1.sista.data.model.CbtTokenValidationResponse
import com.sultanagung1.sista.data.model.ChatAttachment
import com.sultanagung1.sista.data.model.ChatMessage
import com.sultanagung1.sista.data.model.ChildActivityEvent
import com.sultanagung1.sista.data.model.ChildAttendanceLog
import com.sultanagung1.sista.data.model.ChildGradeItem
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.ChildVsClassComparison
import com.sultanagung1.sista.data.model.ContextualHomePayload
import com.sultanagung1.sista.data.model.ConversationItem
import com.sultanagung1.sista.data.model.DeviceTokenRegisterRequest
import com.sultanagung1.sista.data.model.DynamicQrResponse
import com.sultanagung1.sista.data.model.EmergencyBroadcastData
import com.sultanagung1.sista.data.model.EmergencyBroadcastRequest
import com.sultanagung1.sista.data.model.EssayFeedbackResponse
import com.sultanagung1.sista.data.model.EssaySubmissionRequest
import com.sultanagung1.sista.data.model.GpsCheckinRequest
import com.sultanagung1.sista.data.model.GradeEntry
import com.sultanagung1.sista.data.model.LoginRequest
import com.sultanagung1.sista.data.model.LoginResponse
import com.sultanagung1.sista.data.model.MutabaahLogItem
import com.sultanagung1.sista.data.model.NotificationItem
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.data.model.ParentMessageDto
import com.sultanagung1.sista.data.model.PaymentVaResponse
import com.sultanagung1.sista.data.model.PendingApprovalItem
import com.sultanagung1.sista.data.model.RegisterBiometricRequest
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.SchoolKpiSummary
import com.sultanagung1.sista.data.model.SmartSuggestion
import com.sultanagung1.sista.data.model.SubmitClassAttendanceRequest
import com.sultanagung1.sista.data.model.TahfidzLogItem
import com.sultanagung1.sista.data.model.TeacherCbtExamItem
import com.sultanagung1.sista.data.model.TeacherClassStudent
import com.sultanagung1.sista.data.model.TeacherCreateExamRequest
import com.sultanagung1.sista.data.model.TeacherCreatedExam
import com.sultanagung1.sista.data.model.TeacherClassSummary
import com.sultanagung1.sista.data.model.TeacherDirectoryItem
import com.sultanagung1.sista.data.model.UserProfile
import com.sultanagung1.sista.data.model.WeeklyDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import com.sultanagung1.sista.data.model.LockedExamStudent
import com.sultanagung1.sista.data.model.CbtExamQuestionsResponse
import com.sultanagung1.sista.data.model.CbtExamMeta

class AuthRepository(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager,
    private val userDao: UserDao? = null,
    private val widgetSnapshots: WidgetSnapshotStore? = null,
    private val localeSync: LocaleSync? = null,
    private val messages: FallbackMessages,
) {
    fun getLocalUser(): Flow<UserProfile?> {
        return userDao?.getLoggedInUser()?.map { it?.toUserProfile() } ?: flow { emit(null) }
    }

    fun login(identifier: String, pass: String): Flow<NetworkResult<LoginResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.authApi.login(LoginRequest(email = identifier, password = pass))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val token = loginResponse.data?.token ?: ""
                val user = loginResponse.data?.user
                if (token.isNotEmpty() && user != null) {
                    sessionManager.saveAuthSession(
                        token = token,
                        role = user.role,
                        name = user.name,
                        email = user.email,
                        identifier = user.nisn ?: user.nip ?: user.email,
                        userId = user.id?.toString(),
                        classroom = user.classroom
                    )
                    // Simpan data profil ke Database Room (SQLite)
                    userDao?.insertUser(UserEntity.fromUserProfile(user))
                    // Home-screen widgets start empty for the new session.
                    widgetSnapshots?.startNewSession()
                    // A language picked on the login screen is saved on the
                    // account; otherwise the account's language is applied.
                    localeSync?.reconcile(user.preferredLocale)

                    emit(NetworkResult.Success(loginResponse))
                } else {
                    emit(NetworkResult.Error(loginResponse.message ?: messages.get(R.string.login_incomplete_response)))
                }
            } else {
                // The server answers in the app's language (Accept-Language).
                emit(NetworkResult.Error(messages.failure(response, R.string.login_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun requestBiometricChallenge(deviceId: String): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.authApi.requestBiometricChallenge(BiometricChallengeRequest(deviceId))
            if (response.isSuccessful && response.body() != null && response.body()!!.nonce.isNotBlank()) {
                emit(NetworkResult.Success(response.body()!!.nonce))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.biometric_challenge_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun verifyBiometric(deviceId: String, userId: String, signature: String): Flow<NetworkResult<BiometricVerifyResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.authApi.verifyBiometric(BiometricVerifyRequest(deviceId, userId, signature))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.token.isNotBlank()) {
                body.user?.let { user ->
                    sessionManager.saveAuthSession(
                        token = body.token,
                        role = user.role,
                        name = user.name,
                        email = user.email,
                        identifier = user.nisn ?: user.nip ?: user.email,
                        userId = user.id?.toString() ?: userId,
                        classroom = user.classroom
                    )
                }
                widgetSnapshots?.startNewSession()
                emit(NetworkResult.Success(body))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.biometric_verify_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun registerBiometric(deviceId: String, publicKeyPem: String, biometricType: String = "fingerprint"): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.authApi.registerBiometric(
                RegisterBiometricRequest(deviceId, publicKeyPem, biometricType)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                emit(NetworkResult.Success(Unit))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: messages.failure(response, R.string.biometric_register_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun logout() {
        try {
            apiClient.authApi.logout()
        } catch (_: Exception) {}
        sessionManager.clearSession()
        userDao?.deleteAllUsers()
        widgetSnapshots?.clear()
    }
}

class StudentRepository(
    private val apiClient: ApiClient,
    private val localStore: com.sultanagung1.sista.data.local.SulaoneLocalStore? = null,
    private val widgetSnapshots: WidgetSnapshotStore? = null,
    private val messages: FallbackMessages,
) {

    fun getSchedule(): Flow<NetworkResult<List<ScheduleItem>>> = flow {
        emit(NetworkResult.Loading)
        // 1. Emit cached schedule instantly if available
        localStore?.getCachedSchedule()?.let { cached ->
            emit(NetworkResult.Success(cached))
        }

        try {
            val response = apiClient.studentApi.getSchedule()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                localStore?.saveSchedule(data)
                widgetSnapshots?.update { it.copy(schedule = WidgetSnapshots.schedule(data, DateUtils.nowMillis())) }
                emit(NetworkResult.Success(data))
            } else if (localStore?.getCachedSchedule() == null) {
                emit(NetworkResult.Error(messages.failure(response, R.string.schedule_load_failed), response.code()))
            }
        } catch (e: Exception) {
            if (localStore?.getCachedSchedule() == null) {
                emit(NetworkResult.Error(messages.connection(e)))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun getGrades(): Flow<NetworkResult<List<GradeEntry>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.getGrades()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat nilai (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getBillings(): Flow<NetworkResult<List<BillingInvoice>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.getBillings()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                widgetSnapshots?.update { it.withBilling(WidgetSnapshots.studentBilling(data, DateUtils.nowMillis())) }
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat tagihan SPP", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun requestPaymentVa(billingId: Long, bank: String): Flow<NetworkResult<PaymentVaResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.requestPaymentVa(billingId, mapOf("bank" to bank))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal generate Virtual Account", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getMutabaah(): Flow<NetworkResult<List<MutabaahLogItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.getMutabaah()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat riwayat mutaba'ah", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getTahfidzHistory(): Flow<NetworkResult<List<TahfidzLogItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.getTahfidzHistory()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat riwayat setoran tahfidz", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getUnreadNotificationCount(): Flow<NetworkResult<Int>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.getNotificationsSummary()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data.count { it.readAt == null }))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.notifications_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getContextualHome(): Flow<NetworkResult<ContextualHomePayload?>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.contextualHomeApi.getContextualHome()
            if (response.isSuccessful) {
                // A successful response with no data field is a real "nothing
                // contextual today" state — Success(null) is honest here.
                // A non-2xx response is a real failure and must not be
                // repainted as an empty success (that's how the Home screen
                // ended up permanently showing a fabricated 14-day streak
                // card on any backend error).
                emit(NetworkResult.Success(response.body()?.data))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.home_context_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)
}

class AttendanceRepository(
    private val apiClient: ApiClient,
    private val widgetSnapshots: WidgetSnapshotStore? = null
) {

    fun submitGpsCheckin(request: GpsCheckinRequest): Flow<NetworkResult<AttendanceCheckinResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.attendanceApi.submitGpsCheckin(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                // The response only says the GPS check passed (it is also 200 for
                // accounts without a student row, where nothing is recorded), so
                // the widget re-reads the real attendance row instead of assuming "Hadir".
                recordLatestAttendance()
                emit(NetworkResult.Success(body))
            } else {
                emit(NetworkResult.Error(response.message().ifEmpty { "Presensi gagal. Anda di luar radius sekolah." }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getDynamicQr(): Flow<NetworkResult<DynamicQrResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.attendanceApi.getDynamicQr()
            val body = response.body()
            if (response.isSuccessful && body != null && !body.qrToken.isNullOrBlank()) {
                emit(NetworkResult.Success(body))
            } else {
                emit(NetworkResult.Error("Gagal memuat QR presensi dinamis", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getAttendanceHistory(): Flow<NetworkResult<List<AttendanceHistoryItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.attendanceApi.getAttendanceHistory()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                recordAttendanceSnapshot(data)
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat riwayat presensi", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun recordLatestAttendance() {
        if (widgetSnapshots == null) return
        try {
            val response = apiClient.attendanceApi.getAttendanceHistory()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) recordAttendanceSnapshot(data)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // The check-in itself succeeded; a failed widget refresh must not hide that.
        }
    }

    private suspend fun recordAttendanceSnapshot(history: List<AttendanceHistoryItem>) {
        val latest = WidgetSnapshots.studentAttendance(history, DateUtils.nowMillis()) ?: return
        widgetSnapshots?.update { it.withAttendance(latest) }
    }
}

class CbtRepository(private val apiClient: ApiClient, private val messages: FallbackMessages) {

    fun getExams(): Flow<NetworkResult<List<CbtExamItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getExams()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.cbt_exams_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /** The questions and this student's clock ([CbtExamMeta.remainingSeconds]). */
    fun getExamQuestions(examId: Long): Flow<NetworkResult<CbtExamQuestionsResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getExamQuestions(examId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.cbt_questions_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * A failure without an HTTP code means the request never reached the
     * server; CbtViewModel queues those for replay instead of losing the answers.
     */
    fun submitExam(request: CbtSubmitRequest): Flow<NetworkResult<CbtSubmitResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.submitExam(request.examId, request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.cbt_submit_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    // === FASE 25: CBT Enterprise Maximization ===

    fun getEncryptedPayload(examId: Long): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getEncryptedPayload(examId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.cbt_payload_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getDecryptionKey(examId: Long): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getDecryptionKey(examId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.cbt_payload_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun microSyncAnswers(examId: Long, answers: Map<String, String>): Flow<NetworkResult<Map<String, Any>>> = flow {
        try {
            val response = apiClient.cbtApi.microSyncAnswers(examId, CbtMicroSyncRequest(answers))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.cbt_sync_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    // === FASE 26: Token Validation & Force Close ===

    /**
     * A refused token comes back as an error with the server's reason (wrong
     * token, expired, another class, already finished, locked...), never a
     * generic "invalid token". Token gating is a server-side authority check:
     * a client-guessed token is never accepted when the server is unreachable.
     */
    fun validateExamToken(examId: Long, token: String): Flow<NetworkResult<CbtTokenValidationResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.validateExamToken(examId, CbtTokenValidationRequest(token))
            val body = response.body()
            val data = body?.data
            if (response.isSuccessful && data != null && data.valid) {
                emit(NetworkResult.Success(data))
            } else {
                val refused = data?.message?.takeIf { it.isNotBlank() } ?: body?.message
                emit(NetworkResult.Error(refused ?: messages.failure(response, R.string.cbt_token_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getTeacherProctorExams(): Flow<NetworkResult<List<TeacherCbtExamItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getTeacherProctorExams()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.proctor_exams_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getProctorToken(examId: Long): Flow<NetworkResult<CbtTokenInfoResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getProctorToken(examId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.proctor_token_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun regenerateProctorToken(examId: Long): Flow<NetworkResult<CbtTokenInfoResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.regenerateProctorToken(examId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.proctor_token_renew_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun resetStudentAttempt(examId: Long, studentId: Long): Flow<NetworkResult<CbtResetStudentData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.resetStudentAttempt(examId, CbtResetStudentRequest(studentId))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.proctor_reset_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /** Students the anti-cheat locked out of [examId], for the operator to let back in. */
    fun getLockedStudents(examId: Long): Flow<NetworkResult<List<LockedExamStudent>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getLockedStudents(examId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.proctor_locked_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    // === Teacher manual exam authoring (ApiTeacherController) ===

    fun createTeacherExam(request: TeacherCreateExamRequest): Flow<NetworkResult<TeacherCreatedExam>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.createTeacherExam(request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(failureOf(response, body?.message, R.string.exam_publish_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadExamImage(attachment: CbtImageAttachment): Flow<NetworkResult<CbtQuestionImageUpload>> = flow {
        emit(NetworkResult.Loading)
        try {
            val part = MultipartBody.Part.createFormData(
                "image",
                attachment.fileName,
                attachment.bytes.toRequestBody(attachment.mimeType.toMediaTypeOrNull())
            )
            val response = apiClient.cbtApi.uploadExamImage(part)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                emit(NetworkResult.Success(body.data!!))
            } else {
                emit(NetworkResult.Error(failureOf(response, body?.message, R.string.exam_image_upload_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /** The server's own words for a failed [response], else the app's text for its status in the user's language. */
    private fun failureOf(response: retrofit2.Response<*>, envelopeMessage: String?, @androidx.annotation.StringRes fallback: Int): String =
        describeServerError(response.code(), response.errorBody()?.string(), envelopeMessage)
            ?: messages.get(
                when (response.code()) {
                    401 -> R.string.error_session_expired
                    403 -> R.string.error_forbidden
                    413 -> R.string.error_file_too_large
                    else -> fallback
                },
                response.code(),
            )

    companion object {
        /** Laravel's untranslated default for a failed authorization. */
        private const val LARAVEL_UNAUTHORIZED = "This action is unauthorized."

        /**
         * What the server said about a non-2xx response, or null when it said
         * nothing a user can read. A 422 from `$request->validate()` is
         * `{message, errors: {field: [...]}}`, where `message` only carries the
         * FIRST error, so every distinct error line is surfaced, letting a
         * teacher fix a long exam form in one pass. The server writes them in
         * the user's language (Accept-Language).
         */
        fun describeServerError(code: Int, errorBody: String?, envelopeMessage: String?): String? {
            val json = errorBody?.takeIf { it.isNotBlank() }?.let {
                runCatching { com.google.gson.JsonParser.parseString(it).asJsonObject }.getOrNull()
            }
            val fieldErrors = json?.get("errors")
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.entrySet()
                ?.flatMap { entry ->
                    val value = entry.value
                    if (value.isJsonArray) value.asJsonArray.mapNotNull { e -> e.takeIf { it.isJsonPrimitive }?.asString } else emptyList()
                }
                ?.distinct()
                .orEmpty()
            if (fieldErrors.isNotEmpty()) return fieldErrors.joinToString("\n")

            val serverMessage = envelopeMessage
                ?: json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString
            return when (code) {
                // A session error or a file the web server refused never has a readable body.
                401, 413 -> null
                else -> serverMessage?.takeIf { it.isNotBlank() && it != LARAVEL_UNAUTHORIZED }
            }
        }
    }

    fun forceCloseExam(examId: Long, reason: String, answersSnapshot: Map<String, String>): Flow<NetworkResult<CbtForceCloseResponse>> = flow {
        try {
            val response = apiClient.cbtApi.forceCloseExam(examId, CbtForceCloseRequest(reason, answersSnapshot))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(CbtForceCloseResponse(
                    status = "force_closed",
                    answersSaved = true,
                    forceCloseReason = reason
                )))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.cbt_report_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * FASE 72.2: fire-and-forget liveness ping while an exam is active — a
     * dropped heartbeat is not itself a violation (network blip, brief
     * background), so failures are swallowed rather than surfaced as an
     * error the student would need to dismiss.
     */
    suspend fun sendHeartbeat(examId: Long) {
        try {
            apiClient.cbtApi.sendHeartbeat(examId)
        } catch (e: Exception) {
            // Best-effort — next tick retries.
        }
    }

    /**
     * Reports a client-detected anti-cheat violation (screenshot attempt,
     * root/emulator/USB-debug detected, window focus lost) to the backend so
     * it reaches the proctor's live dashboard in real time via
     * StudentCheatedEvent — CbtAntiCheatEngine only tracked these locally
     * before, so a proctor never saw anything short of an app-minimize/
     * split-screen (which already goes through forceCloseExam()).
     */
    fun logViolation(examId: Long, type: String): Flow<NetworkResult<Boolean>> = flow {
        try {
            val response = apiClient.cbtApi.logViolation(examId, type)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!["data"] as? Map<*, *>
                val isBlocked = data?.get("is_blocked") as? Boolean ?: false
                emit(NetworkResult.Success(isBlocked))
            } else {
                emit(NetworkResult.Error(messages.failure(response, R.string.cbt_report_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)
}

class AiRepository(private val apiClient: ApiClient) {

    fun startTutorSession(subjectName: String, topic: String): Flow<NetworkResult<AiTutorSessionData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.aiApi.startTutorSession(AiTutorSessionRequest(subjectName, topic))
            val session = response.body()?.session
            if (response.isSuccessful && session != null) {
                emit(NetworkResult.Success(session))
            } else {
                emit(NetworkResult.Error("Gagal memulai sesi AI Tutor", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi AI terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun sendMessage(sessionId: Long, message: String): Flow<NetworkResult<AiChatMessage>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.aiApi.sendTutorMessage(sessionId, AiMessageRequest(message))
            val reply = response.body()?.data
            if (response.isSuccessful && reply != null) {
                emit(NetworkResult.Success(reply.aiResponse.toChatMessage()))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mengirim pertanyaan ke AI", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi AI terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getTutorSuggestions(): Flow<NetworkResult<List<String>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.aiApi.getTutorSuggestions()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat saran pertanyaan", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun submitEssay(title: String, subject: String, text: String): Flow<NetworkResult<EssayFeedbackResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.aiApi.submitEssay(EssaySubmissionRequest(title, subject, text))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memeriksa esai dengan AI", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi AI terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getSmartSuggestions(): Flow<NetworkResult<List<SmartSuggestion>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.contextualHomeApi.getSmartSuggestions()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(emptyList()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(emptyList()))
        }
    }.flowOn(Dispatchers.IO)
}

/**
 * Real teacher/guru data — no fallback of any kind. "Today's schedule" and
 * "recent journals" are intentionally NOT owned here; they come from
 * [TeachingJournalRepository] (teacher/schedule, teacher/journals), which
 * TeacherViewModel composes alongside these calls instead of duplicating them.
 */
class TeacherRepository(private val apiClient: ApiClient, private val messages: FallbackMessages) {

    fun getClasses(): Flow<NetworkResult<List<TeacherClassSummary>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.getTeacherClasses()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.teacher_classes_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassStudents(classroomId: Long): Flow<NetworkResult<List<TeacherClassStudent>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.getClassStudents(classroomId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.class_students_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    fun submitClassAttendance(request: SubmitClassAttendanceRequest): Flow<NetworkResult<AttendanceRecordedResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.submitClassAttendance(request)
            val body = response.body()
            val data = body?.data
            if (response.isSuccessful && body?.success == true && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.class_attendance_save_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)
}

class ParentRepository(
    private val apiClient: ApiClient,
    private val widgetSnapshots: WidgetSnapshotStore? = null
) {

    fun getChildren(): Flow<NetworkResult<List<ParentChildItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentApi.getChildren()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat daftar anak", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildSummary(uuid: String): Flow<NetworkResult<ChildSummaryResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentApi.getChildSummary(uuid)
            val body = response.body()
            val data = body?.data
            if (response.isSuccessful && body?.success == true && data != null) {
                // The widgets follow the child the parent last opened here.
                widgetSnapshots?.update { it.withBilling(WidgetSnapshots.childBilling(uuid, data, DateUtils.nowMillis())) }
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat rangkuman anak", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildAttendanceHistory(uuid: String): Flow<NetworkResult<List<ChildAttendanceLog>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentApi.getChildAttendanceHistory(uuid)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                WidgetSnapshots.childAttendance(uuid, body.data.orEmpty(), DateUtils.nowMillis())?.let { latest ->
                    widgetSnapshots?.update { it.withAttendance(latest) }
                }
                emit(NetworkResult.Success(body.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat riwayat presensi anak", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildGrades(uuid: String): Flow<NetworkResult<List<ChildGradeItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentApi.getChildGrades(uuid)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat nilai anak", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildFeed(childUuid: String): Flow<NetworkResult<List<ChildActivityEvent>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentExperienceApi.getChildFeed(childUuid)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(response.body()?.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat aktivitas anak" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat aktivitas anak"))
        }
    }.flowOn(Dispatchers.IO)

    fun getWeeklyDigest(childUuid: String): Flow<NetworkResult<WeeklyDigest?>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentExperienceApi.getWeeklyDigest(childUuid)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(response.body()?.data))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat ringkasan mingguan" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat ringkasan mingguan"))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildComparison(childUuid: String): Flow<NetworkResult<List<ChildVsClassComparison>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentExperienceApi.getChildComparison(childUuid)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(response.body()?.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat perbandingan nilai anak" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat perbandingan nilai anak"))
        }
    }.flowOn(Dispatchers.IO)
}

class AdminRepository(private val apiClient: ApiClient) {

    fun getDashboard(): Flow<NetworkResult<AdminDashboardData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.getAdminDashboard()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat dashboard eksekutif", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    // FASE 71.4 follow-up: real KPI (SPP ratio per cohort, CBT server usage)
    // — replaces the "Dummy or expanded KPI details" backend placeholder.
    fun getSchoolKpi(): Flow<NetworkResult<SchoolKpiSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.getSchoolKpi()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat KPI sekolah", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    // FASE 71.4 follow-up: real pending-approvals list.
    fun getPendingApprovals(): Flow<NetworkResult<List<PendingApprovalItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.getPendingApprovals()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar persetujuan", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Decides the current step of a request. Success carries the server's own
     * outcome ("diteruskan ke langkah berikutnya" / "disetujui" / "ditolak");
     * a refusal carries its reason (e.g. the step belongs to another role).
     */
    fun processApproval(id: String, action: String, notes: String? = null): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.processApproval(id, action, notes?.takeIf { it.isNotBlank() })
            if (response.isSuccessful) {
                val message = response.body()?.get("message") as? String
                emit(NetworkResult.Success(message ?: if (action == "approve") "Pengajuan disetujui." else "Pengajuan ditolak."))
            } else {
                emit(NetworkResult.Error(
                    serverMessageOf(response.errorBody()?.string()) ?: "Gagal memproses persetujuan (Kode: ${response.code()}).",
                    response.code(),
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    // FASE 71.4 "Tombol Siaran Darurat"
    fun broadcastEmergency(title: String, message: String, location: String?): Flow<NetworkResult<EmergencyBroadcastData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.broadcastEmergency(EmergencyBroadcastRequest(title, message, location))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mengirim siaran darurat.", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}

/**
 * NOTE ON BACKEND STATE: the backend has no "conversation" entity — both
 * sides of a parent<->teacher thread read/write the same flat ParentMessage
 * log, exposed via two symmetric endpoint pairs (parent/messages,
 * teacher/messages) so each role only ever sees the recipient set its own
 * role middleware allows. This repository picks the pair based on the
 * logged-in user's role and groups the flat log into conversations
 * client-side either way.
 */
class ChatRepository(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager
) {

    private suspend fun myUserId(): String = sessionManager.userIdFlow.first().orEmpty()

    private suspend fun isTeacherRole(): Boolean {
        val role = sessionManager.userRoleFlow.first().orEmpty()
        return role.equals("guru", true) || role.equals("teacher", true) || role.equals("bk", true)
    }

    private suspend fun fetchMessages(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<ParentMessageDto>>> =
        if (isTeacherRole()) apiClient.chatApi.getTeacherMessages() else apiClient.chatApi.getParentMessages()

    fun getConversations(): Flow<NetworkResult<List<ConversationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val myId = myUserId()
            val response = fetchMessages()
            val messages = response.body()?.data
            if (response.isSuccessful && messages != null) {
                // Backend orders by latest() — groupBy preserves scan order, so each
                // group's key first appears at its own most recent message, which
                // means the resulting groups are already ordered newest-first.
                val conversations = messages
                    .groupBy { conversationKey(it, myId) }
                    .map { (key, groupMessages) ->
                        val latest = groupMessages.first()
                        val isRecipientMe = latest.recipientId == myId
                        ConversationItem(
                            id = key,
                            recipientId = if (isRecipientMe) latest.senderId else latest.recipientId,
                            recipientName = if (isRecipientMe) latest.senderName else latest.recipientName,
                            recipientRole = if (isRecipientMe) latest.senderRole else latest.recipientRole,
                            studentId = latest.studentId,
                            studentUuid = latest.studentUuid.orEmpty(),
                            lastMessage = latest.message.ifBlank {
                                when (latest.attachmentType) {
                                    "pdf" -> "📎 Dokumen PDF"
                                    "image" -> "📷 Gambar"
                                    else -> ""
                                }
                            },
                            lastMessageTime = latest.createdAtHuman ?: latest.createdAt.orEmpty(),
                            unreadCount = groupMessages.count { it.recipientId == myId && !it.isRead },
                            isOnline = false
                        )
                    }
                emit(NetworkResult.Success(conversations))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar percakapan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /** conversationId is the synthetic "{otherPartyId}_{studentId}" key from getConversations(). */
    fun getMessages(conversationId: String): Flow<NetworkResult<List<ChatMessage>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val myId = myUserId()
            val response = fetchMessages()
            val messages = response.body()?.data
            if (response.isSuccessful && messages != null) {
                val thread = messages
                    .filter { conversationKey(it, myId) == conversationId }
                    .sortedBy { it.createdAt }
                    .map { it.toChatMessage(conversationId, myId) }
                emit(NetworkResult.Success(thread))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat pesan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * recipientId is only meaningful for the parent side of the API (the
     * backend needs to know which teacher to notify); a teacher/BK reply
     * derives its recipient server-side from the student's registered
     * parent_id, so recipientId is ignored on that path. [attachment], when
     * present, is uploaded as a real multipart file — the server stores it
     * and returns a real attachment_url, not a magic string in the message
     * text.
     */
    fun sendMessage(
        recipientId: String,
        studentUuid: String,
        text: String,
        attachment: ChatAttachment? = null
    ): Flow<NetworkResult<ChatMessage>> = flow {
        emit(NetworkResult.Loading)
        try {
            val myId = myUserId()
            val messagePart = text.toRequestBody("text/plain".toMediaTypeOrNull())
            val studentUuidPart = studentUuid.toRequestBody("text/plain".toMediaTypeOrNull())
            val attachmentPart = attachment?.let {
                MultipartBody.Part.createFormData(
                    "attachment", it.fileName, it.bytes.toRequestBody(it.mimeType.toMediaTypeOrNull())
                )
            }
            val response = if (isTeacherRole()) {
                apiClient.chatApi.sendTeacherMessage(studentUuidPart, messagePart, attachmentPart)
            } else {
                val recipientPart = recipientId.toRequestBody("text/plain".toMediaTypeOrNull())
                apiClient.chatApi.sendParentMessage(recipientPart, studentUuidPart, messagePart, attachmentPart)
            }
            val sent = response.body()?.data
            if (response.isSuccessful && sent != null) {
                emit(NetworkResult.Success(sent.toChatMessage(conversationKey(sent, myId), myId)))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mengirim pesan (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /** Real: GET evaluations/teachers, the only endpoint returning actual guru/bk id+name pairs. */
    fun getTeacherDirectory(): Flow<NetworkResult<List<TeacherDirectoryItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.chatApi.getTeacherDirectory()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat daftar guru (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    private fun conversationKey(m: ParentMessageDto, myId: String): String {
        val otherPartyId = if (m.senderId == myId) m.recipientId else m.senderId
        return "${otherPartyId}_${m.studentId}"
    }

    private fun ParentMessageDto.toChatMessage(conversationId: String, myId: String) = ChatMessage(
        id = id,
        conversationId = conversationId,
        studentId = studentId,
        senderId = senderId,
        senderName = senderName,
        text = message,
        timestamp = createdAtHuman ?: createdAt.orEmpty(),
        isMe = senderId == myId,
        status = if (isRead) "read" else "sent",
        attachmentUrl = attachmentUrl,
        attachmentType = attachmentType
    )
}

class NotificationRepository(private val apiClient: ApiClient) {

    fun registerDeviceToken(deviceId: String, fcmToken: String): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val req = DeviceTokenRegisterRequest(deviceId = deviceId, fcmToken = fcmToken)
            val response = apiClient.notificationApi.registerDevice(req)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                // A failed registration must surface as a failure — silently
                // claiming success here previously meant a device could go
                // an entire app lifetime never actually reachable by push
                // notifications, with no way to notice or retry.
                emit(NetworkResult.Error("Gagal mendaftarkan perangkat untuk notifikasi push (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat mendaftarkan perangkat."))
        }
    }.flowOn(Dispatchers.IO)

    /** Announcements meant for this account, pinned first. */
    fun getAnnouncements(category: String? = null): Flow<NetworkResult<List<AnnouncementItem>>> =
        enveloped("pengumuman") { apiClient.notificationApi.getAnnouncements(category) }

    /** Opening one records it as read on the server. */
    fun getAnnouncementDetail(id: String): Flow<NetworkResult<AnnouncementItem>> =
        enveloped("pengumuman") { apiClient.notificationApi.getAnnouncementDetail(id) }

    /** Confirms reading an announcement that asks for it. */
    fun acknowledgeAnnouncement(id: String): Flow<NetworkResult<AnnouncementItem>> =
        enveloped("konfirmasi pengumuman") { apiClient.notificationApi.acknowledgeAnnouncement(id) }

    fun getNotifications(): Flow<NetworkResult<List<NotificationItem>>> =
        enveloped("notifikasi") { apiClient.notificationApi.getNotifications() }

    /** For the bell on every home. */
    fun getUnreadCount(): Flow<NetworkResult<Int>> =
        enveloped("jumlah notifikasi") { apiClient.notificationApi.getUnreadCount() }.mapSuccess { it.count }

    fun markNotificationRead(id: String): Flow<NetworkResult<NotificationItem>> =
        enveloped("notifikasi") { apiClient.notificationApi.markNotificationRead(id) }

    fun markAllNotificationsRead(): Flow<NetworkResult<Int>> =
        enveloped("notifikasi") { apiClient.notificationApi.markAllNotificationsRead() }.mapSuccess { it["updated"] ?: 0 }

    /** Succeeds with Unit: the server sends no body worth keeping. */
    fun deleteNotification(id: String): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.deleteNotification(id)
            if (response.isSuccessful) emit(NetworkResult.Success(Unit)) else emit(errorOf(response, "notifikasi"))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    private fun <T : Any> enveloped(what: String, call: suspend () -> Response<ApiEnvelope<T>>): Flow<NetworkResult<T>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = call()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) emit(NetworkResult.Success(data)) else emit(errorOf(response, what))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    private fun errorOf(response: Response<*>, what: String): NetworkResult.Error {
        val message = serverMessageOf(response.errorBody()?.string())
            ?: if (response.code() == 404) "Data $what tidak ditemukan." else "Data $what belum bisa dimuat (kode ${response.code()})."
        return NetworkResult.Error(message, response.code())
    }

    private fun <T, R> Flow<NetworkResult<T>>.mapSuccess(transform: (T) -> R): Flow<NetworkResult<R>> = map { result ->
        when (result) {
            is NetworkResult.Success -> NetworkResult.Success(transform(result.data))
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }
}
