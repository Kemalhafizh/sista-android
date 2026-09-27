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
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat dashboard BK" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi BK"))
        }
    }.flowOn(Dispatchers.IO)

    fun createSession(request: CreateCounselingSessionRequest): Flow<NetworkResult<CounselingSessionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.createSession(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal menyimpan sesi konseling" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat menyimpan sesi konseling"))
        }
    }.flowOn(Dispatchers.IO)

    fun requestAppointment(request: RequestAppointmentRequest): Flow<NetworkResult<CounselingSessionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.requestAppointment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal mengajukan jadwal konseling" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan saat mengajukan jadwal konseling"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMyStudentAppointments(): Flow<NetworkResult<List<CounselingSessionItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMyStudentAppointments()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat riwayat konsultasi" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat riwayat konsultasi"))
        }
    }.flowOn(Dispatchers.IO)

    fun getAtRiskStudents(): Flow<NetworkResult<List<CounselingAtRiskStudentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAtRiskStudents()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat daftar siswa berisiko" }, response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat memuat daftar siswa berisiko"))
        }
    }.flowOn(Dispatchers.IO)
}
