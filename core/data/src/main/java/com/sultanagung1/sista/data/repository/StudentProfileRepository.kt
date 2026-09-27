package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.StudentProfileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class StudentProfileRepository(private val apiService: StudentProfileApiService) {

    fun getStudentProfile(studentId: Long? = null): Flow<NetworkResult<StudentProfile360Data>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = if (studentId != null && studentId > 0) {
                apiService.getStudentProfile(studentId)
            } else {
                apiService.getMyProfile()
            }
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error("Gagal memuat profil siswa", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)
}
