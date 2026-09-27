package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Parent<->teacher messaging. The backend has no "conversation" entity —
 * both sides read/write the same flat ParentMessage log, exposed twice:
 * GET/POST parent/messages for a parent, GET/POST teacher/messages (role
 * guru,bk) for the recipient side. ChatRepository picks which pair to call
 * based on the logged-in user's role, and groups the flat log into
 * conversations client-side either way.
 *
 * Both sends are @Multipart (not a JSON @Body) because ApiParentController /
 * ApiTeacherController's sendMessage() accepts an optional `attachment` file
 * alongside the text fields — `attachment` is nullable and Retrofit omits a
 * null @Part entirely, so a caption-only message still works.
 */
interface ChatApiService {
    @GET("parent/messages")
    suspend fun getParentMessages(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<ParentMessageDto>>>

    @Multipart
    @POST("parent/messages")
    suspend fun sendParentMessage(
        @Part("recipient_id") recipientId: RequestBody,
        @Part("student_uuid") studentUuid: RequestBody,
        @Part("message") message: RequestBody,
        @Part attachment: MultipartBody.Part?
    ): Response<com.sultanagung1.sista.core.network.ApiEnvelope<ParentMessageDto>>

    @GET("teacher/messages")
    suspend fun getTeacherMessages(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<ParentMessageDto>>>

    @Multipart
    @POST("teacher/messages")
    suspend fun sendTeacherMessage(
        @Part("student_uuid") studentUuid: RequestBody,
        @Part("message") message: RequestBody,
        @Part attachment: MultipartBody.Part?
    ): Response<com.sultanagung1.sista.core.network.ApiEnvelope<ParentMessageDto>>

    // Real endpoint (not enveloped) — used for the "new consultation" teacher picker,
    // since no dedicated "teachers available for parent consultation" endpoint exists.
    @GET("evaluations/teachers")
    suspend fun getTeacherDirectory(): Response<List<TeacherDirectoryItem>>
}

interface NotificationApiService {
    @POST("mobile/devices/register")
    suspend fun registerDevice(
        @Body request: DeviceTokenRegisterRequest
    ): Response<Map<String, Any>>

    @GET("mobile/notifications")
    suspend fun getNotifications(): Response<List<NotificationItem>>

    @GET("mobile/announcements")
    suspend fun getAnnouncements(
        @Query("category") category: String? = null
    ): Response<List<AnnouncementItem>>

    @GET("mobile/announcements/{id}")
    suspend fun getAnnouncementDetail(
        @Path("id") id: String
    ): Response<AnnouncementItem>
}
