package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.StoreJournalRequest
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.model.TeachingJournalEntry
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Real teacher-scoped endpoints (role:guru,bk) — distinct from the
 * FASE-97 `teaching-journals/...` stub, which falls back to hardcoded sample
 * rows when a teacher has no journals yet and never actually persists a
 * submission. `teacher/schedule` and `teacher/journals` are the genuine,
 * fully-persisted surface (Schedule + TeachingJournal Eloquent models).
 */
interface TeachingJournalMobileApiService {

    @GET("teacher/schedule")
    suspend fun getTeacherSchedule(): Response<ApiEnvelope<List<TeacherScheduleSlot>>>

    @GET("teacher/journals")
    suspend fun getTeacherJournals(): Response<ApiEnvelope<List<TeachingJournalEntry>>>

    @POST("teacher/journals")
    suspend fun storeJournal(
        @Body request: StoreJournalRequest
    ): Response<ApiEnvelope<TeachingJournalEntry>>
}