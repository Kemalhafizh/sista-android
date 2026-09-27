package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.CalendarMobileApiService
import com.sultanagung1.sista.data.model.AcademicCalendarEventItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CalendarRepository(private val apiService: CalendarMobileApiService) {

    fun getEvents(
        month: Int? = null,
        year: Int? = null,
        category: String? = null
    ): Flow<NetworkResult<List<AcademicCalendarEventItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getEvents(month, year, category)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Error(response.message().ifBlank { "Gagal memuat agenda kalender akademik" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan kalender"))
        }
    }.flowOn(Dispatchers.IO)
}
