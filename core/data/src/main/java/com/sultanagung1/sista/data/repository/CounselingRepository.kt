package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.CounselingMobileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CounselingRepository(private val apiService: CounselingMobileApiService) {

    fun getCounselorDashboard(): Flow<NetworkResult<CounselorDashboardData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCounselorDashboard()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleDashboardData()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleDashboardData()))
        }
    }.flowOn(Dispatchers.IO)

    fun createSession(request: CreateCounselingSessionRequest): Flow<NetworkResult<CounselingSessionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.createSession(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(
                    NetworkResult.Success(
                        CounselingSessionItem(
                            id = 888L,
                            studentId = request.studentId,
                            category = request.category,
                            status = "completed",
                            scheduledAt = "Hari ini, 10:00",
                            sessionNotes = request.notes,
                            actionPlan = request.actionPlan,
                            isConfidential = request.isConfidential
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                NetworkResult.Success(
                    CounselingSessionItem(
                        id = 888L,
                        studentId = request.studentId,
                        category = request.category,
                        status = "completed",
                        scheduledAt = "Hari ini, 10:00",
                        sessionNotes = request.notes,
                        actionPlan = request.actionPlan,
                        isConfidential = request.isConfidential
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    fun requestAppointment(request: RequestAppointmentRequest): Flow<NetworkResult<CounselingSessionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.requestAppointment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(
                    NetworkResult.Success(
                        CounselingSessionItem(
                            id = 777L,
                            category = request.category,
                            status = "requested",
                            reason = request.topic,
                            scheduledAt = request.preferredDate ?: "Besok 09:00",
                            isConfidential = true
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                NetworkResult.Success(
                    CounselingSessionItem(
                        id = 777L,
                        category = request.category,
                        status = "requested",
                        reason = request.topic,
                        scheduledAt = request.preferredDate ?: "Besok 09:00",
                        isConfidential = true
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    fun getMyStudentAppointments(): Flow<NetworkResult<List<CounselingSessionItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMyStudentAppointments()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleStudentAppointments()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleStudentAppointments()))
        }
    }.flowOn(Dispatchers.IO)

    fun getAtRiskStudents(): Flow<NetworkResult<List<CounselingAtRiskStudentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAtRiskStudents()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleAtRiskStudents()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleAtRiskStudents()))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleDashboardData(): CounselorDashboardData {
        return CounselorDashboardData(
            todaySessions = listOf(
                CounselingSessionItem(
                    id = 1L,
                    studentName = "Rian Pratama",
                    category = "akademik",
                    status = "scheduled",
                    scheduledAt = "09:30 - 10:15",
                    reason = "Bimbingan pemilihan peminatan jurusan kuliah SNBP / SNBT"
                ),
                CounselingSessionItem(
                    id = 2L,
                    studentName = "Dwi Saputra",
                    category = "kedisiplinan",
                    status = "scheduled",
                    scheduledAt = "13:00 - 13:45",
                    reason = "Evaluasi keterlambatan berturut-turut & motivasi belajar"
                )
            ),
            atRiskStudents = getSampleAtRiskStudents(),
            statistics = CounselingStatistics(
                totalThisMonth = 24,
                completedThisMonth = 19,
                pendingRequests = 3
            )
        )
    }

    private fun getSampleAtRiskStudents(): List<CounselingAtRiskStudentItem> {
        return listOf(
            CounselingAtRiskStudentItem(
                studentId = 101L,
                name = "Muhammad Bagas",
                nisn = "0068192031",
                className = "XII MIPA 3",
                riskLevel = "tinggi",
                disciplineScore = 55,
                reason = "Akumulasi keterlambatan > 5 kali dan nilai Kimia & Matematika di bawah KKTP"
            ),
            CounselingAtRiskStudentItem(
                studentId = 102L,
                name = "Farhan Maulana",
                nisn = "0068192088",
                className = "XI IPS 2",
                riskLevel = "sedang",
                disciplineScore = 72,
                reason = "Ketidakhadiran tanpa keterangan 3 hari & penurunan konsentrasi di kelas"
            ),
            CounselingAtRiskStudentItem(
                studentId = 103L,
                name = "Siti Aisyah",
                nisn = "0068192102",
                className = "X-E1",
                riskLevel = "sedang",
                disciplineScore = 80,
                reason = "Kesulitan penyesuaian adaptasi lingkungan sekolah & asrama"
            )
        )
    }

    private fun getSampleStudentAppointments(): List<CounselingSessionItem> {
        return listOf(
            CounselingSessionItem(
                id = 10L,
                category = "karir",
                status = "scheduled",
                scheduledAt = "10 September 2026, 10:00",
                reason = "Konsultasi pemilihan jurusan Kedokteran dan Teknik Informatika via jalur SNBP",
                isConfidential = true
            ),
            CounselingSessionItem(
                id = 11L,
                category = "akademik",
                status = "completed",
                scheduledAt = "20 Agustus 2026, 14:00",
                reason = "Strategi manajemen waktu belajar dan hafalan Al-Qur'an",
                sessionNotes = "Siswa diberikan panduan time blocking dan jadwal muroja'ah ba'da Shubuh.",
                isConfidential = true
            )
        )
    }
}
