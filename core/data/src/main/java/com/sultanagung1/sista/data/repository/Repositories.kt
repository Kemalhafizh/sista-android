@file:Suppress("unused", "UNUSED_PARAMETER", "UNNECESSARY_NOT_NULL_ASSERTION", "UNUSED_VARIABLE")
package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
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
import com.sultanagung1.sista.data.model.CbtQuestionItem
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
import com.sultanagung1.sista.data.model.NotificationPreferences
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

class AuthRepository(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager,
    private val userDao: UserDao? = null,
    private val widgetSnapshots: WidgetSnapshotStore? = null
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

                    emit(NetworkResult.Success(loginResponse))
                } else {
                    emit(NetworkResult.Error(loginResponse.message ?: "Data login tidak valid."))
                }
            } else {
                val errMessage = when (response.code()) {
                    401 -> "Kredensial salah. Periksa kembali email/NISN dan kata sandi."
                    404 -> "Server endpoint login tidak ditemukan (404)."
                    500 -> "Terjadi kesalahan internal pada server backend (500)."
                    else -> response.message().ifEmpty { "Gagal masuk (Kode: ${response.code()})" }
                }
                emit(NetworkResult.Error(errMessage, response.code()))
            }
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is java.net.ConnectException -> "Gagal terhubung ke server backend! Pastikan backend Laravel sudah dinyalakan ('php artisan serve')."
                is java.net.SocketTimeoutException -> "Koneksi ke backend server timeout. Pastikan server aktif dan merespons."
                else -> "Gagal terhubung ke backend: ${e.localizedMessage ?: "Koneksi terputus"}. Pastikan backend Laravel sudah dinyalakan ('php artisan serve')."
            }
            emit(NetworkResult.Error(errorMsg))
        }
    }.flowOn(Dispatchers.IO)

    fun requestBiometricChallenge(deviceId: String): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.authApi.requestBiometricChallenge(BiometricChallengeRequest(deviceId))
            if (response.isSuccessful && response.body() != null && response.body()!!.nonce.isNotBlank()) {
                emit(NetworkResult.Success(response.body()!!.nonce))
            } else {
                emit(NetworkResult.Error("Gagal mendapatkan challenge biometrik dari server (${response.code()})."))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Server tidak dapat dijangkau untuk autentikasi biometrik: ${e.localizedMessage}"))
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
                emit(NetworkResult.Error(body?.message ?: "Verifikasi biometrik gagal.", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Gagal memverifikasi biometrik."))
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
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mendaftarkan kredensial biometrik (${response.code()})."))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Gagal mendaftarkan kredensial biometrik."))
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
    private val widgetSnapshots: WidgetSnapshotStore? = null
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
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat jadwal pelajaran", response.code()))
            }
        } catch (e: Exception) {
            if (localStore?.getCachedSchedule() == null) {
                emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat memuat jadwal pelajaran."))
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
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat notifikasi", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat data kontekstual beranda" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat beranda"))
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

class CbtRepository(private val apiClient: ApiClient) {

    fun getExams(): Flow<NetworkResult<List<CbtExamItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getExams()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat daftar ujian CBT", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getExamQuestions(examId: Long): Flow<NetworkResult<List<CbtQuestionItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getExamQuestions(examId)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data.questions))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat soal ujian dari server (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat memuat soal ujian."))
        }
    }.flowOn(Dispatchers.IO)

    fun submitExam(request: CbtSubmitRequest): Flow<NetworkResult<CbtSubmitResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.submitExam(request.examId, request)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal mengirim lembar jawaban ujian", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(NetworkResult.Error("Gagal mengambil paket soal terenkripsi", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getDecryptionKey(examId: Long): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getDecryptionKey(examId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Kunci dekripsi belum dapat diakses", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun microSyncAnswers(examId: Long, answers: Map<String, String>): Flow<NetworkResult<Map<String, Any>>> = flow {
        try {
            val response = apiClient.cbtApi.microSyncAnswers(examId, CbtMicroSyncRequest(answers))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal sinkronisasi jawaban", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    // === FASE 26: Token Validation & Force Close ===

    fun validateExamToken(examId: Long, token: String): Flow<NetworkResult<CbtTokenValidationResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.validateExamToken(examId, CbtTokenValidationRequest(token))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val data = body["data"] as? Map<*, *>
                val valid = data?.get("valid") as? Boolean ?: false
                val message = data?.get("message") as? String ?: (body["message"] as? String ?: "Token valid")
                val attemptStatus = data?.get("attempt_status") as? String
                // Gson deserializes a raw Map<String, Any>'s JSON numbers as
                // Double, never Long/Int — must go through Number first.
                val studentId = (data?.get("student_id") as? Number)?.toLong()
                val maxViolations = (data?.get("max_violations") as? Number)?.toInt()
                emit(NetworkResult.Success(CbtTokenValidationResponse(
                    valid = valid,
                    message = message,
                    examId = examId,
                    attemptStatus = attemptStatus,
                    studentId = studentId,
                    maxViolations = maxViolations
                )))
            } else {
                val errorMsg = if (response.code() == 403) "Token tidak valid atau akses hanya via aplikasi Android" else "Gagal validasi token (${response.code()})"
                emit(NetworkResult.Error(errorMsg, response.code()))
            }
        } catch (e: Exception) {
            // Token gating is a server-side authority check (exam entry control) —
            // never accept a client-guessed token when the backend is unreachable.
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat memvalidasi token ujian."))
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
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat daftar ujian (${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat token ujian (${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(NetworkResult.Error(body?.message ?: "Gagal memperbarui token ujian (${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(NetworkResult.Error(body?.message ?: "Gagal mereset status siswa (${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(
                    NetworkResult.Error(
                        describeServerError(response.code(), response.errorBody()?.string(), body?.message, "Gagal menerbitkan ujian"),
                        response.code()
                    )
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat menerbitkan ujian."))
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
                emit(
                    NetworkResult.Error(
                        describeServerError(response.code(), response.errorBody()?.string(), body?.message, "Gagal mengunggah gambar"),
                        response.code()
                    )
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat mengunggah gambar."))
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        /**
         * Readable message for a non-2xx Laravel response. A 422 from
         * `$request->validate()` is `{message, errors: {field: [...]}}`, where
         * `message` only carries the FIRST error — so every distinct error line
         * is surfaced, letting a teacher fix a long exam form in one pass
         * instead of one error per submit. Validation text is already
         * Indonesian (the backend runs with APP_LOCALE=id).
         */
        fun describeServerError(code: Int, errorBody: String?, envelopeMessage: String?, fallback: String): String {
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
                401 -> "Sesi Anda berakhir. Silakan masuk kembali."
                403 -> "Akun ini tidak berwenang melakukan tindakan ini."
                413 -> "Ukuran file terlalu besar untuk diterima server."
                else -> serverMessage?.takeIf { it.isNotBlank() } ?: "$fallback (kode $code)."
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
                emit(NetworkResult.Error("Gagal melaporkan penutupan ujian", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
                emit(NetworkResult.Error("Gagal melaporkan pelanggaran", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
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
class TeacherRepository(private val apiClient: ApiClient) {

    fun getClasses(): Flow<NetworkResult<List<TeacherClassSummary>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.getTeacherClasses()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data.orEmpty()))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat daftar kelas", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
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
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat daftar siswa kelas", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan"))
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
                emit(NetworkResult.Error(body?.message ?: "Gagal menyimpan presensi kelas", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat menyimpan presensi"))
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

    fun getAnnouncements(category: String? = null): Flow<NetworkResult<List<AnnouncementItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.getAnnouncements(category)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat pengumuman sekolah (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getAnnouncementDetail(id: String): Flow<NetworkResult<AnnouncementItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.getAnnouncementDetail(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat detail pengumuman (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getNotifications(): Flow<NetworkResult<List<NotificationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.getNotifications()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat notifikasi (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getNotificationPreferences(): Flow<NetworkResult<NotificationPreferences>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationPreferencesApi.getPreferences()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat preferensi notifikasi (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * A failed save must not echo back the attempted [preferences] as if
     * they were persisted — the user would believe a toggle was saved when
     * the server never actually received it.
     */
    fun updateNotificationPreferences(preferences: NotificationPreferences): Flow<NetworkResult<NotificationPreferences>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationPreferencesApi.updatePreferences(preferences)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal menyimpan preferensi notifikasi (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
}
