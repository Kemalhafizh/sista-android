package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.AcademicCalendarEventItem
import com.sultanagung1.sista.data.model.SchoolOpsApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarMobileApiService {
    @GET("academic-calendar")
    suspend fun getEvents(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
        @Query("category") category: String? = null
    ): Response<SchoolOpsApiResponse<List<AcademicCalendarEventItem>>>
}
