package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.TeachingJournalMobileApiService
import com.sultanagung1.sista.data.model.StoreJournalRequest
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.model.TeachingJournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TeachingJournalRepository(private val apiService: TeachingJournalMobileApiService) {

    fun getTeacherSchedule(): Flow<NetworkResult<List<TeacherScheduleSlot>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherSchedule()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data ?: emptyList<TeacherScheduleSlot>()))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat jadwal mengajar", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan jadwal mengajar"))
        }
    }.flowOn(Dispatchers.IO)

    fun getTeacherJournals(): Flow<NetworkResult<List<TeachingJournalEntry>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherJournals()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data ?: emptyList<TeachingJournalEntry>()))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal memuat daftar jurnal mengajar", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan jurnal mengajar"))
        }
    }.flowOn(Dispatchers.IO)

    fun storeJournal(request: StoreJournalRequest): Flow<NetworkResult<TeachingJournalEntry>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.storeJournal(request)
            val body = response.body()
            val data = body?.data
            if (response.isSuccessful && body?.success == true && data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(body?.message ?: "Gagal menyimpan jurnal mengajar", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat simpan jurnal"))
        }
    }.flowOn(Dispatchers.IO)
}
