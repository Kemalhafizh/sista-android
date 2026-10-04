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

class TeachingJournalRepository(
    private val apiService: TeachingJournalMobileApiService,
    private val messages: FallbackMessages,
) {

    fun getTeacherSchedule(): Flow<NetworkResult<List<TeacherScheduleSlot>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeacherSchedule()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                emit(NetworkResult.Success(body.data ?: emptyList<TeacherScheduleSlot>()))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.teaching_schedule_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
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
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.journals_load_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
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
                // A refusal (403 kelas guru lain, 422 data tidak valid) comes in the
                // error body; show the server's own sentence, not a generic one.
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.journal_save_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)
}
