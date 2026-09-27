@file:Suppress("unused", "UNUSED_PARAMETER", "UNNECESSARY_NOT_NULL_ASSERTION", "UNUSED_VARIABLE")
package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
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
import com.sultanagung1.sista.data.model.CbtMicroSyncRequest
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
import com.sultanagung1.sista.data.model.RegisterBiometricRequest
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.SmartSuggestion
import com.sultanagung1.sista.data.model.SubmitClassAttendanceRequest
import com.sultanagung1.sista.data.model.TahfidzLogItem
import com.sultanagung1.sista.data.model.TeacherClassStudent
import com.sultanagung1.sista.data.model.TeacherClassSummary
import com.sultanagung1.sista.data.model.TeacherDirectoryItem
import com.sultanagung1.sista.data.model.UserProfile
import com.sultanagung1.sista.data.model.WeeklyDigest
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
    private val userDao: UserDao? = null
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
    }
}

class StudentRepository(
    private val apiClient: ApiClient,
    private val localStore: com.sultanagung1.sista.data.local.SulaoneLocalStore? = null
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
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(null))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(null))
        }
    }.flowOn(Dispatchers.IO)
}

class AttendanceRepository(private val apiClient: ApiClient) {

    fun submitGpsCheckin(request: GpsCheckinRequest): Flow<NetworkResult<AttendanceCheckinResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.attendanceApi.submitGpsCheckin(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
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
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
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
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Gagal memuat riwayat presensi", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)
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
            if (response.isSuccessful && body?.success == true && body.data != null) {
                emit(NetworkResult.Success(body.data))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal menyimpan presensi kelas", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat menyimpan presensi"))
        }
    }.flowOn(Dispatchers.IO)
}

class ParentRepository(private val apiClient: ApiClient) {

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
            if (response.isSuccessful && body?.success == true && body.data != null) {
                emit(NetworkResult.Success(body.data))
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

    fun getChildFeed(): Flow<NetworkResult<List<ChildActivityEvent>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentExperienceApi.getChildFeed()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(emptyList()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(emptyList()))
        }
    }.flowOn(Dispatchers.IO)

    fun getWeeklyDigest(): Flow<NetworkResult<WeeklyDigest?>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentExperienceApi.getWeeklyDigest()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(null))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(null))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildComparison(): Flow<NetworkResult<List<ChildVsClassComparison>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentExperienceApi.getChildComparison()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(emptyList()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(emptyList()))
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

    fun processApproval(id: String, action: String): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.processApproval(id, action)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Error("Gagal memproses persetujuan (Kode: ${response.code()}).", response.code()))
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
            emit(NetworkResult.Success(response.isSuccessful))
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)

    fun getAnnouncements(category: String? = null): Flow<NetworkResult<List<AnnouncementItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.getAnnouncements(category)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    listOf(
                        AnnouncementItem(
                            id = "ann1",
                            title = "Pelaksanaan Penilaian Tengah Semester (PTS) Berbasis CBT Terpadu",
                            summary = "Diberitahukan kepada seluruh siswa dan wali murid bahwa pelaksanaan PTS Gasal TA 2025/2026 dimulai hari Senin depan.",
                            content = "Assalamu'alaikum Warahmatullahi Wabarakatuh.\n\nSehubungan dengan kalender akademik SMA Islam Sultan Agung 1 Semarang, kami menginformasikan pelaksanaan PTS Ganjil dengan ketentuan menggunakan aplikasi CBT Sulaone.\n\n1. Seluruh siswa wajib membawa gawai yang telah terpasang aplikasi Sulaone terbaru.\n2. Presensi ujian dimulai pukul 07.00 WIB di masing-masing ruang kelas.\n3. Tata tertib dan jadwal detail terlampir pada dokumen edaran resmi.",
                            category = "Akademik",
                            author = "Waka Kurikulum",
                            date = "25 Agustus 2026",
                            priority = "important",
                            attachmentUrl = "edaran_pts_2026.pdf"
                        ),
                        AnnouncementItem(
                            id = "ann2",
                            title = "Pemberitahuan Program Shalat Dhuha dan Tadarus Al-Qur'an Berjamaah",
                            summary = "Penguatan amalan yaumiyah pembiasaan shalat Dhuha dan tadarus 1 juz per pekan di Masjid Kampus YBWSA.",
                            content = "Assalamu'alaikum Warahmatullahi Wabarakatuh.\n\nDalam rangka meningkatkan ketakwaan dan pembentukan karakter generasi khaira ummah, seluruh siswa diharapkan hadir pukul 06.30 WIB untuk Shalat Dhuha berjamaah dan tadarus dipandu guru pendamping.",
                            category = "Ibadah",
                            author = "Koordinator Keislaman",
                            date = "24 Agustus 2026",
                            priority = "normal"
                        ),
                        AnnouncementItem(
                            id = "ann3",
                            title = "Peringatan Waspada Cuaca Ekstrem dan Protokol Keselamatan Kampus",
                            summary = "Himbauan kewaspadaan dan kepatuhan jalur evakuasi aman saat berkegiatan di lingkungan sekolah.",
                            content = "Mengingat peringatan dini BMKG terkait potensi hujan lebat disertai angin di wilayah Kota Semarang, pihak sekolah menghimbau seluruh civitas akademika untuk tidak berteduh di bawah pohon rindang dan memarkir kendaraan pada shelter tertutup.",
                            category = "Darurat",
                            author = "Tim K3 & Sarpras",
                            date = "23 Agustus 2026",
                            priority = "emergency"
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                listOf(
                    AnnouncementItem(
                        id = "ann1",
                        title = "Pelaksanaan Penilaian Tengah Semester (PTS) Berbasis CBT Terpadu",
                        summary = "Diberitahukan kepada seluruh siswa dan wali murid bahwa pelaksanaan PTS Gasal TA 2025/2026 dimulai hari Senin depan.",
                        content = "Assalamu'alaikum Warahmatullahi Wabarakatuh.\n\nSehubungan dengan kalender akademik SMA Islam Sultan Agung 1 Semarang, kami menginformasikan pelaksanaan PTS Ganjil dengan ketentuan menggunakan aplikasi CBT Sulaone.",
                        category = "Akademik",
                        author = "Waka Kurikulum",
                        date = "25 Agustus 2026",
                        priority = "important",
                        attachmentUrl = "edaran_pts_2026.pdf"
                    ),
                    AnnouncementItem(
                        id = "ann2",
                        title = "Pemberitahuan Program Shalat Dhuha dan Tadarus Al-Qur'an Berjamaah",
                        summary = "Penguatan amalan yaumiyah pembiasaan shalat Dhuha dan tadarus 1 juz per pekan di Masjid Kampus YBWSA.",
                        content = "Assalamu'alaikum Warahmatullahi Wabarakatuh.\n\nDalam rangka meningkatkan ketakwaan dan pembentukan karakter generasi khaira ummah, seluruh siswa diharapkan hadir pukul 06.30 WIB.",
                        category = "Ibadah",
                        author = "Koordinator Keislaman",
                        date = "24 Agustus 2026",
                        priority = "normal"
                    ),
                    AnnouncementItem(
                        id = "ann3",
                        title = "Peringatan Waspada Cuaca Ekstrem dan Protokol Keselamatan Kampus",
                        summary = "Himbauan kewaspadaan dan kepatuhan jalur evakuasi aman saat berkegiatan di lingkungan sekolah.",
                        content = "Pihak sekolah menghimbau seluruh civitas akademika untuk berhati-hati dan mematuhi instruksi keselamatan.",
                        category = "Darurat",
                        author = "Tim K3 & Sarpras",
                        date = "23 Agustus 2026",
                        priority = "emergency"
                    )
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getAnnouncementDetail(id: String): Flow<NetworkResult<AnnouncementItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.getAnnouncementDetail(id)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    AnnouncementItem(
                        id = id,
                        title = "Pelaksanaan Penilaian Tengah Semester (PTS) Berbasis CBT Terpadu",
                        summary = "Diberitahukan kepada seluruh siswa dan wali murid bahwa pelaksanaan PTS Gasal TA 2025/2026 dimulai hari Senin depan.",
                        content = "Assalamu'alaikum Warahmatullahi Wabarakatuh.\n\nSehubungan dengan kalender akademik SMA Islam Sultan Agung 1 Semarang, kami menginformasikan pelaksanaan PTS Ganjil dengan ketentuan menggunakan aplikasi CBT Sulaone.\n\n1. Seluruh siswa wajib membawa gawai yang telah terpasang aplikasi Sulaone terbaru.\n2. Presensi ujian dimulai pukul 07.00 WIB di masing-masing ruang kelas.\n3. Siswa wajib mematuhi kode etik ujian dan dilarang berpindah aplikasi selama ujian berlangsung.\n4. Tata tertib dan jadwal detail terlampir pada dokumen edaran resmi yayasan.\n\nWassalamu'alaikum Warahmatullahi Wabarakatuh.",
                        category = "Akademik",
                        author = "Waka Kurikulum SMA Islam Sultan Agung 1",
                        date = "25 Agustus 2026",
                        priority = "important",
                        attachmentUrl = "surat_edaran_pts_2026.pdf"
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                AnnouncementItem(
                    id = id,
                    title = "Pelaksanaan Penilaian Tengah Semester (PTS) Berbasis CBT Terpadu",
                    summary = "Diberitahukan kepada seluruh siswa dan wali murid bahwa pelaksanaan PTS Gasal TA 2025/2026 dimulai hari Senin depan.",
                    content = "Assalamu'alaikum Warahmatullahi Wabarakatuh.\n\nSehubungan dengan kalender akademik SMA Islam Sultan Agung 1 Semarang, kami menginformasikan pelaksanaan PTS Ganjil dengan ketentuan menggunakan aplikasi CBT Sulaone.",
                    category = "Akademik",
                    author = "Waka Kurikulum SMA Islam Sultan Agung 1",
                    date = "25 Agustus 2026",
                    priority = "important",
                    attachmentUrl = "surat_edaran_pts_2026.pdf"
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getNotifications(): Flow<NetworkResult<List<NotificationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationApi.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    listOf(
                        NotificationItem(
                            id = "notif_1",
                            title = "Presensi Gerbang Berhasil",
                            body = "Ahmad Kemal Hafizh berhasil tercatat tiba di Gerbang Utama SMA Islam Sultan Agung 1 pada pukul 06:42 WIB.",
                            channel = "attendance_alerts",
                            deepLinkRoute = "geofence_attendance",
                            timestamp = "06:42 WIB",
                            isRead = false
                        ),
                        NotificationItem(
                            id = "notif_2",
                            title = "Nilai Rapor Formatif Fisika Diunggah",
                            body = "Ustadz Drs. H. Bambang Suherman telah merilis nilai Capaian KKTP Bab Termodinamika (Nilai: 92).",
                            channel = "academic_updates",
                            deepLinkRoute = "grades",
                            timestamp = "09:30 WIB",
                            isRead = false
                        ),
                        NotificationItem(
                            id = "notif_3",
                            title = "Pemberitahuan Tagihan SPP September",
                            body = "Virtual Account BSI (88219324567890) untuk pembayaran SPP bulan September telah aktif.",
                            channel = "financial_reminders",
                            deepLinkRoute = "billing",
                            timestamp = "Kemarin",
                            isRead = true
                        ),
                        NotificationItem(
                            id = "notif_4",
                            title = "Pesan Masuk dari Wali Kelas",
                            body = "Ustadz Bambang: 'Assalamu'alaikum, jadwal konsultasi SNBP ananda sudah kami agendakan.'",
                            channel = "academic_updates",
                            deepLinkRoute = "chat/conv1",
                            timestamp = "Kemarin",
                            isRead = true
                        ),
                        NotificationItem(
                            id = "notif_5",
                            title = "Siaran Darurat: Kewaspadaan Cuaca Ekstrem",
                            body = "Himbauan evakuasi aman dan pemindahan kendaraan ke area tertutup sehubungan hujan lebat BMKG.",
                            channel = "emergency_broadcast",
                            deepLinkRoute = "announcement_detail/ann3",
                            timestamp = "2 hari lalu",
                            isRead = true
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                listOf(
                    NotificationItem(
                        id = "notif_1",
                        title = "Presensi Gerbang Berhasil",
                        body = "Ahmad Kemal Hafizh berhasil tercatat tiba di Gerbang Utama SMA Islam Sultan Agung 1 pada pukul 06:42 WIB.",
                        channel = "attendance_alerts",
                        deepLinkRoute = "geofence_attendance",
                        timestamp = "06:42 WIB",
                        isRead = false
                    ),
                    NotificationItem(
                        id = "notif_2",
                        title = "Nilai Rapor Formatif Fisika Diunggah",
                        body = "Ustadz Drs. H. Bambang Suherman telah merilis nilai Capaian KKTP Bab Termodinamika (Nilai: 92).",
                        channel = "academic_updates",
                        deepLinkRoute = "grades",
                        timestamp = "09:30 WIB",
                        isRead = false
                    ),
                    NotificationItem(
                        id = "notif_3",
                        title = "Pemberitahuan Tagihan SPP September",
                        body = "Virtual Account BSI (88219324567890) untuk pembayaran SPP bulan September telah aktif.",
                        channel = "financial_reminders",
                        deepLinkRoute = "billing",
                        timestamp = "Kemarin",
                        isRead = true
                    ),
                    NotificationItem(
                        id = "notif_4",
                        title = "Pesan Masuk dari Wali Kelas",
                        body = "Ustadz Bambang: 'Assalamu'alaikum, jadwal konsultasi SNBP ananda sudah kami agendakan.'",
                        channel = "academic_updates",
                        deepLinkRoute = "chat/conv1",
                        timestamp = "Kemarin",
                        isRead = true
                    ),
                    NotificationItem(
                        id = "notif_5",
                        title = "Siaran Darurat: Kewaspadaan Cuaca Ekstrem",
                        body = "Himbauan evakuasi aman dan pemindahan kendaraan ke area tertutup sehubungan hujan lebat BMKG.",
                        channel = "emergency_broadcast",
                        deepLinkRoute = "announcement_detail/ann3",
                        timestamp = "2 hari lalu",
                        isRead = true
                    )
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getNotificationPreferences(): Flow<NetworkResult<NotificationPreferences>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationPreferencesApi.getPreferences()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(NotificationPreferences()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(NotificationPreferences()))
        }
    }.flowOn(Dispatchers.IO)

    fun updateNotificationPreferences(preferences: NotificationPreferences): Flow<NetworkResult<NotificationPreferences>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.notificationPreferencesApi.updatePreferences(preferences)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(preferences))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(preferences))
        }
    }.flowOn(Dispatchers.IO)
}
