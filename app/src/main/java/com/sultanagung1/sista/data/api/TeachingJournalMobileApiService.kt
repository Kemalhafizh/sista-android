package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.JournalScheduleItem
import com.sultanagung1.sista.data.model.JournalSubmitRequest
import com.sultanagung1.sista.data.model.SchoolOpsApiResponse
import com.sultanagung1.sista.data.model.SchoolTeachingJournalItem
import com.sultanagung1.sista.data.model.StoreTeachingJournalRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TeachingJournalMobileApiService {

    @GET("teaching-journals/my")
    suspend fun getTeacherJournals(): Response<SchoolOpsApiResponse<List<SchoolTeachingJournalItem>>>

    @POST("teaching-journals")
    suspend fun storeTeacherJournal(
        @Body request: StoreTeachingJournalRequest
    ): Response<SchoolOpsApiResponse<SchoolTeachingJournalItem>>

    @GET("journal/schedule")
    suspend fun getSchedule(): Response<List<JournalScheduleItem>>

    @POST("journal/submit")
    suspend fun submitJournal(
        @Body request: JournalSubmitRequest
    ): Response<Unit>
}
