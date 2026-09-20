package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.TeachingJournalMobileApiService
import com.sultanagung1.sista.data.model.JournalScheduleItem
import com.sultanagung1.sista.data.model.JournalSubmitRequest
import com.sultanagung1.sista.data.model.SchoolTeachingJournalItem
import com.sultanagung1.sista.data.model.StoreTeachingJournalRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TeachingJournalRepository(private val apiService: TeachingJournalMobileApiService) {

    fun getTeacherJournals(): Flow<NetworkResult<List<SchoolTeachingJournalItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherJournals()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat daftar jurnal mengajar" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan jurnal mengajar"))
        }
    }.flowOn(Dispatchers.IO)

    fun storeTeacherJournal(request: StoreTeachingJournalRequest): Flow<NetworkResult<SchoolTeachingJournalItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.storeTeacherJournal(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal menyimpan jurnal mengajar" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat simpan jurnal"))
        }
    }.flowOn(Dispatchers.IO)

    fun getSchedule(): Flow<NetworkResult<List<JournalScheduleItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getSchedule()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat jadwal mengajar" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan jadwal mengajar"))
        }
    }.flowOn(Dispatchers.IO)

    fun submitJournal(request: JournalSubmitRequest): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.submitJournal(request)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(Unit))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal submit jurnal" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi saat submit jurnal"))
        }
    }.flowOn(Dispatchers.IO)
}
