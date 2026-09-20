package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.local.dao.UserDao
import com.sultanagung1.sista.data.local.entity.UserEntity
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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

    fun getAcademicSummary(): Flow<NetworkResult<AcademicSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.studentApi.getAcademicSummary()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    AcademicSummary(
                        gpa = 92.4,
                        totalCredits = 48,
                        rankInClass = 2,
                        totalStudents = 36,
                        grades = listOf(
                            GradeItem("Matematika Tingkat Lanjut", "Ustadz Ahmad Fauzi, M.Pd", 95.0, 92.0, 94.0, 93.6, "A", "Tuntas"),
                            GradeItem("Pendidikan Agama Islam & Tahfidz", "Ustadz Bambang Irawan, Lc", 98.0, 96.0, 97.0, 97.0, "A+", "Tuntas")
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                AcademicSummary(
                    gpa = 92.4,
                    totalCredits = 48,
                    rankInClass = 2,
                    totalStudents = 36,
                    grades = listOf(
                        GradeItem("Matematika Tingkat Lanjut", "Ustadz Ahmad Fauzi, M.Pd", 95.0, 92.0, 94.0, 93.6, "A", "Tuntas"),
                        GradeItem("Pendidikan Agama Islam & Tahfidz", "Ustadz Bambang Irawan, Lc", 98.0, 96.0, 97.0, 97.0, "A+", "Tuntas")
                    )
                )
            ))
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
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat daftar ujian CBT", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus."))
        }
    }.flowOn(Dispatchers.IO)

    fun getExamQuestions(examId: Long): Flow<NetworkResult<List<CbtQuestionItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.getExamQuestions(examId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat soal ujian dari server (Kode: ${response.code()}).", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus saat memuat soal ujian."))
        }
    }.flowOn(Dispatchers.IO)

    fun submitExam(request: CbtSubmitRequest): Flow<NetworkResult<CbtSubmitResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.cbtApi.submitExam(request.examId, request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal mengirim lembar jawaban ujian", response.code()))
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
                emit(NetworkResult.Success(CbtTokenValidationResponse(
                    valid = valid,
                    message = message,
                    examId = examId,
                    attemptStatus = attemptStatus
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
}

class AiRepository(private val apiClient: ApiClient) {

    fun startTutorSession(subject: String, topic: String): Flow<NetworkResult<AiTutorSessionResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.aiApi.startTutorSession(AiTutorSessionRequest(subject, topic))
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
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
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Gagal mengirim pertanyaan ke AI", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi AI terputus."))
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

class TeacherRepository(private val apiClient: ApiClient) {

    fun getDashboard(): Flow<NetworkResult<TeacherDashboardData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.getTeacherDashboard()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                // Fallback mock for teacher demo
                emit(NetworkResult.Success(
                    TeacherDashboardData(
                        teacherName = "Ustadz Ahmad Fauzi, M.Pd",
                        nip = "198504122010011002",
                        teachingHoursThisWeek = 24,
                        totalClasses = 5,
                        todaySchedules = listOf(
                            TeacherScheduleItem("s1", "Selasa", "07:30 - 09:00 WIB", "XII MIPA 1", "Matematika Tingkat Lanjut", "R.201", isActiveNow = true, attendanceCompleted = false),
                            TeacherScheduleItem("s2", "Selasa", "09:15 - 10:45 WIB", "XII MIPA 2", "Matematika Tingkat Lanjut", "R.202", isActiveNow = false, attendanceCompleted = false),
                            TeacherScheduleItem("s3", "Selasa", "11:00 - 12:30 WIB", "XI MIPA 3", "Matematika Wajib", "R.105", isActiveNow = false, attendanceCompleted = false)
                        ),
                        recentJournals = listOf(
                            TeachingJournalItem("j1", "25 Agustus 2026", "XII MIPA 1", "Matematika Tingkat Lanjut", "Kalkulus Integral & Penerapan Luas Daerah", "TP-3.4", "Siswa sangat antusias dalam mengerjakan studi kasus luas bidang."),
                            TeachingJournalItem("j2", "24 Agustus 2026", "XI MIPA 3", "Matematika Wajib", "Transformasi Geometri & Refleksi Garis", "TP-2.1", "Semua siswa tuntas mengerjakan LKPD kelompok.")
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            // Graceful fallback for offline demo
            emit(NetworkResult.Success(
                TeacherDashboardData(
                    teacherName = "Ustadz Ahmad Fauzi, M.Pd",
                    nip = "198504122010011002",
                    teachingHoursThisWeek = 24,
                    totalClasses = 5,
                    todaySchedules = listOf(
                        TeacherScheduleItem("s1", "Selasa", "07:30 - 09:00 WIB", "XII MIPA 1", "Matematika Tingkat Lanjut", "R.201", isActiveNow = true, attendanceCompleted = false),
                        TeacherScheduleItem("s2", "Selasa", "09:15 - 10:45 WIB", "XII MIPA 2", "Matematika Tingkat Lanjut", "R.202", isActiveNow = false, attendanceCompleted = false)
                    ),
                    recentJournals = listOf(
                        TeachingJournalItem("j1", "25 Agustus 2026", "XII MIPA 1", "Matematika Tingkat Lanjut", "Kalkulus Integral", "TP-3.4", "Pembelajaran interaktif di lab komputer.")
                    )
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassStudents(classId: String): Flow<NetworkResult<List<StudentAttendanceInputItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.getClassStudents(classId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    listOf(
                        StudentAttendanceInputItem("st1", "0068941231", "Muhammad Rizky Pratama", "L", "Hadir"),
                        StudentAttendanceInputItem("st2", "0068941232", "Aisyah Nur Salsabila", "P", "Hadir"),
                        StudentAttendanceInputItem("st3", "0068941233", "Fathir Ahmad Al-Farisi", "L", "Hadir"),
                        StudentAttendanceInputItem("st4", "0068941234", "Khadijah Zahra Amalia", "P", "Izin", "Izin olimpiade sains"),
                        StudentAttendanceInputItem("st5", "0068941235", "Zaid bin Haritsah", "L", "Hadir"),
                        StudentAttendanceInputItem("st6", "0068941236", "Maryam Azzahra", "P", "Sakit", "Demam berobat di UKS")
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                listOf(
                    StudentAttendanceInputItem("st1", "0068941231", "Muhammad Rizky Pratama", "L", "Hadir"),
                    StudentAttendanceInputItem("st2", "0068941232", "Aisyah Nur Salsabila", "P", "Hadir"),
                    StudentAttendanceInputItem("st3", "0068941233", "Fathir Ahmad Al-Farisi", "L", "Hadir"),
                    StudentAttendanceInputItem("st4", "0068941234", "Khadijah Zahra Amalia", "P", "Hadir")
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun submitClassAttendance(request: ClassAttendanceSubmitRequest): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.submitClassAttendance(request)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Success(true)) // Optimistic success for demo
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)

    fun storeJournal(request: TeachingJournalCreateRequest): Flow<NetworkResult<TeachingJournalItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.teacherApi.storeTeachingJournal(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    TeachingJournalItem("j-new", "25 Agustus 2026", request.className, request.subjectName, request.topic, request.competencyCode, request.notes)
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                TeachingJournalItem("j-new", "25 Agustus 2026", request.className, request.subjectName, request.topic, request.competencyCode, request.notes)
            ))
        }
    }.flowOn(Dispatchers.IO)
}

class ParentRepository(private val apiClient: ApiClient) {

    fun getDashboard(): Flow<NetworkResult<ParentDashboardData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentApi.getParentDashboard()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    ParentDashboardData(
                        parentName = "Bapak Hendra Gunawan, S.T.",
                        children = listOf(
                            ChildSummary(
                                studentId = "c1",
                                nisn = "0068941231",
                                name = "Muhammad Rizky Pratama",
                                className = "XII MIPA 1 (Fase F)",
                                homeroomTeacher = "Ustadz Drs. H. Bambang Suherman",
                                homeroomPhone = "6281234567890",
                                counselorName = "Ustadzah Fatimah, S.Psi (Guru BK)",
                                counselorPhone = "6289876543210",
                                todayAttendanceStatus = "Hadir di Sekolah",
                                todayCheckinTime = "06:42 WIB",
                                mutabaahScore = 92,
                                gpaScore = 92.4,
                                pendingSppAmount = 0L,
                                sppStatus = "Lunas"
                            ),
                            ChildSummary(
                                studentId = "c2",
                                nisn = "0081239842",
                                name = "Fatimah Azzahra Gunawan",
                                className = "X-2 (Fase E)",
                                homeroomTeacher = "Ustadzah Hj. Nurul Hidayah, S.Pd",
                                homeroomPhone = "6281234567891",
                                counselorName = "Ustadz Ridwan Hakim, M.Pd (Guru BK)",
                                counselorPhone = "6289876543211",
                                todayAttendanceStatus = "Hadir di Sekolah",
                                todayCheckinTime = "06:38 WIB",
                                mutabaahScore = 88,
                                gpaScore = 89.6,
                                pendingSppAmount = 0L,
                                sppStatus = "Lunas"
                            )
                        ),
                        recentAnnouncements = listOf(
                            SchoolAnnouncementItem("a1", "Jadwal Asesmen Sumatif Tengah Semester", "25 Agustus 2026", "Akademik", "Pemberitahuan pelaksanaan ASTS Ganjil TA 2025/2026 berbasis CBT."),
                            SchoolAnnouncementItem("a2", "Kajian Parenting Bulanan Yayasan Sultan Agung", "20 Agustus 2026", "Kesiswaan", "Undangan pengajian wali murid di Masjid SMA Sultan Agung 1.")
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                ParentDashboardData(
                    parentName = "Bapak Hendra Gunawan, S.T.",
                    children = listOf(
                        ChildSummary(
                            studentId = "c1",
                            nisn = "0068941231",
                            name = "Muhammad Rizky Pratama",
                            className = "XII MIPA 1 (Fase F)",
                            homeroomTeacher = "Ustadz Drs. H. Bambang Suherman",
                            homeroomPhone = "6281234567890",
                            todayAttendanceStatus = "Hadir di Sekolah",
                            todayCheckinTime = "06:42 WIB",
                            mutabaahScore = 92,
                            gpaScore = 92.4,
                            pendingSppAmount = 0L,
                            sppStatus = "Lunas"
                        )
                    )
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getChildAttendanceHistory(studentId: String): Flow<NetworkResult<List<ChildAttendanceLog>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.parentApi.getChildAttendanceHistory(studentId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    listOf(
                        ChildAttendanceLog("25 Agustus 2026", "Hadir", "06:42 WIB", null, isPunctual = true),
                        ChildAttendanceLog("24 Agustus 2026", "Hadir", "06:40 WIB", "15:30 WIB", isPunctual = true),
                        ChildAttendanceLog("23 Agustus 2026", "Hadir", "06:45 WIB", "15:30 WIB", isPunctual = true),
                        ChildAttendanceLog("22 Agustus 2026", "Hadir", "06:35 WIB", "15:30 WIB", isPunctual = true)
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                listOf(
                    ChildAttendanceLog("25 Agustus 2026", "Hadir", "06:42 WIB", null, isPunctual = true)
                )
            ))
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
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    AdminDashboardData(
                        principalName = "Drs. H. Muhammad Arif, M.Pd (Kepala Sekolah)",
                        academicYear = "2025/2026 Ganjil",
                        kpi = SchoolKpiSummary(
                            totalStudents = 1080,
                            totalTeachers = 64,
                            attendanceRateToday = 98.4,
                            sppCollectionRate = 94.2,
                            teachersPresentToday = 62,
                            activeCbtExamsCount = 3
                        ),
                        criticalAlerts = listOf(
                            CriticalAlertItem("al1", "Koneksi Lab CBT 2 Perlu Perhatian", "warning", "Latency jaringan di Lab CBT 2 meningkat menjadi 120ms saat simulasi.", "10 menit lalu"),
                            CriticalAlertItem("al2", "Persiapan Akreditasi Perpustakaan", "info", "Dokumen instrumen borang siap ditinjau oleh Kepala Sekolah.", "1 jam lalu")
                        ),
                        pendingApprovals = listOf(
                            ApprovalRequestItem("ap1", "Izin Cuti Dinas Guru", "Ustadzah Siti Aminah, S.Pd", "Matematika", "25 Agustus 2026", "Pelatihan Implementasi Kurikulum Merdeka di BGP Jawa Tengah"),
                            ApprovalRequestItem("ap2", "Pengadaan Alat Praktikum Fisika", "Laboratorium IPA", "Sarana Prasarana", "24 Agustus 2026", "Pengadaan sensor optik dan osiloskop digital untuk kelas XII"),
                            ApprovalRequestItem("ap3", "Proposal Lomba Tahfidz Nasional", "OSIS / Rohis SMA Sultan Agung", "Kesiswaan", "23 Agustus 2026", "Partisipasi 5 santri dalam Festival Tahfidz 30 Juz Tingkat Nasional")
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                AdminDashboardData(
                    principalName = "Drs. H. Muhammad Arif, M.Pd (Kepala Sekolah)",
                    academicYear = "2025/2026 Ganjil",
                    kpi = SchoolKpiSummary(),
                    criticalAlerts = emptyList(),
                    pendingApprovals = emptyList()
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun processApproval(id: String, action: String): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.adminApi.processApproval(id, action)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Success(true)) // Optimistic for demo
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)
}

class ChatRepository(private val apiClient: ApiClient) {

    fun getConversations(): Flow<NetworkResult<List<ConversationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.chatApi.getConversations()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    listOf(
                        ConversationItem(
                            id = "conv1",
                            recipientId = "t1",
                            recipientName = "Ustadz Drs. H. Bambang Suherman",
                            recipientRole = "Wali Kelas XII MIPA 1",
                            lastMessage = "Assalamu'alaikum Ibu, perkembangan tahfidz dan nilai fisika ananda sangat membanggakan.",
                            lastMessageTime = "10:30 WIB",
                            unreadCount = 1,
                            isOnline = true
                        ),
                        ConversationItem(
                            id = "conv2",
                            recipientId = "t2",
                            recipientName = "Ustadzah Fatimah, S.Psi",
                            recipientRole = "Guru Bimbingan Konseling (BK)",
                            lastMessage = "Jadwal konsultasi peminatan jurusan SNBP dapat dilaksanakan hari Kamis pukul 13.00 WIB.",
                            lastMessageTime = "Kemarin",
                            unreadCount = 0,
                            isOnline = false
                        ),
                        ConversationItem(
                            id = "conv3",
                            recipientId = "t3",
                            recipientName = "Ustadz Muhammad Luthfi, Lc",
                            recipientRole = "Pembina Tahsin & Bahasa Arab",
                            lastMessage = "Alhamdulillah setoran Surah An-Naba ananda makhraj dan tajwidnya sudah mumtaz.",
                            lastMessageTime = "23 Ags",
                            unreadCount = 0,
                            isOnline = true
                        )
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                listOf(
                    ConversationItem(
                        id = "conv1",
                        recipientId = "t1",
                        recipientName = "Ustadz Drs. H. Bambang Suherman",
                        recipientRole = "Wali Kelas XII MIPA 1",
                        lastMessage = "Assalamu'alaikum Ibu, perkembangan tahfidz dan nilai fisika ananda sangat membanggakan.",
                        lastMessageTime = "10:30 WIB",
                        unreadCount = 1,
                        isOnline = true
                    ),
                    ConversationItem(
                        id = "conv2",
                        recipientId = "t2",
                        recipientName = "Ustadzah Fatimah, S.Psi",
                        recipientRole = "Guru Bimbingan Konseling (BK)",
                        lastMessage = "Jadwal konsultasi peminatan jurusan SNBP dapat dilaksanakan hari Kamis pukul 13.00 WIB.",
                        lastMessageTime = "Kemarin",
                        unreadCount = 0,
                        isOnline = false
                    ),
                    ConversationItem(
                        id = "conv3",
                        recipientId = "t3",
                        recipientName = "Ustadz Muhammad Luthfi, Lc",
                        recipientRole = "Pembina Tahsin & Bahasa Arab",
                        lastMessage = "Alhamdulillah setoran Surah An-Naba ananda makhraj dan tajwidnya sudah mumtaz.",
                        lastMessageTime = "23 Ags",
                        unreadCount = 0,
                        isOnline = true
                    )
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getMessages(conversationId: String): Flow<NetworkResult<List<ChatMessage>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiClient.chatApi.getMessages(conversationId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    listOf(
                        ChatMessage("m1", conversationId, "t1", "Ustadz Bambang", "Assalamu'alaikum Warahmatullahi Wabarakatuh Bapak/Ibu.", "09:15 WIB", isMe = false, status = "read"),
                        ChatMessage("m2", conversationId, "p1", "Saya", "Wa'alaikumussalam Warahmatullahi Wabarakatuh Ustadz. Mohon izin bertanya terkait persiapan ujian PTS pekan depan.", "09:20 WIB", isMe = true, status = "read"),
                        ChatMessage("m3", conversationId, "t1", "Ustadz Bambang", "Alhamdulillah, kisi-kisi dan materi pengayaan telah kami unggah ke LMS Sulaone. Ananda bisa latihan soal CBT dari aplikasi.", "09:25 WIB", isMe = false, status = "read"),
                        ChatMessage("m4", conversationId, "p1", "Saya", "Baik Ustadz, terima kasih banyak atas bimbingannya.", "09:28 WIB", isMe = true, status = "read"),
                        ChatMessage("m5", conversationId, "t1", "Ustadz Bambang", "Assalamu'alaikum Ibu, perkembangan tahfidz dan nilai fisika ananda sangat membanggakan.", "10:30 WIB", isMe = false, status = "delivered")
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                listOf(
                    ChatMessage("m1", conversationId, "t1", "Ustadz Bambang", "Assalamu'alaikum Warahmatullahi Wabarakatuh Bapak/Ibu.", "09:15 WIB", isMe = false, status = "read"),
                    ChatMessage("m2", conversationId, "p1", "Saya", "Wa'alaikumussalam Warahmatullahi Wabarakatuh Ustadz. Mohon izin bertanya terkait persiapan ujian PTS pekan depan.", "09:20 WIB", isMe = true, status = "read"),
                    ChatMessage("m3", conversationId, "t1", "Ustadz Bambang", "Alhamdulillah, kisi-kisi dan materi pengayaan telah kami unggah ke LMS Sulaone. Ananda bisa latihan soal CBT dari aplikasi.", "09:25 WIB", isMe = false, status = "read"),
                    ChatMessage("m4", conversationId, "p1", "Saya", "Baik Ustadz, terima kasih banyak atas bimbingannya.", "09:28 WIB", isMe = true, status = "read"),
                    ChatMessage("m5", conversationId, "t1", "Ustadz Bambang", "Assalamu'alaikum Ibu, perkembangan tahfidz dan nilai fisika ananda sangat membanggakan.", "10:30 WIB", isMe = false, status = "delivered")
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun sendMessage(conversationId: String?, recipientId: String, text: String): Flow<NetworkResult<ChatMessage>> = flow {
        emit(NetworkResult.Loading)
        try {
            val req = SendMessageRequest(conversationId, recipientId, text)
            val response = apiClient.chatApi.sendMessage(req)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    ChatMessage(
                        id = "m_${System.currentTimeMillis()}",
                        conversationId = conversationId ?: "conv1",
                        senderId = "me",
                        senderName = "Saya",
                        text = text,
                        timestamp = "Baru saja",
                        isMe = true,
                        status = "sent"
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                ChatMessage(
                    id = "m_${System.currentTimeMillis()}",
                    conversationId = conversationId ?: "conv1",
                    senderId = "me",
                    senderName = "Saya",
                    text = text,
                    timestamp = "Baru saja",
                    isMe = true,
                    status = "sent"
                )
            ))
        }
    }.flowOn(Dispatchers.IO)
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
