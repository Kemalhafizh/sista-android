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
    // Real endpoint, finally wired: PushNotificationService::registerDeviceToken()
    // always existed but had no HTTP entry point until now — this is the
    // symmetric POST to the already-real GET/DELETE mobile/devices routes,
    // returning a plain response()->json(...), not the ApiResponseTrait
    // envelope the other 3 endpoints below use.
    @POST("mobile/devices")
    suspend fun registerDevice(
        @Body request: DeviceTokenRegisterRequest
    ): Response<Map<String, Any>>

    @GET("notifications")
    suspend fun getNotifications(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<NotificationItem>>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<UnreadCount>>

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<com.sultanagung1.sista.core.network.ApiEnvelope<NotificationItem>>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<com.sultanagung1.sista.core.network.ApiEnvelope<Map<String, Int>>>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<com.sultanagung1.sista.core.network.ApiEnvelope<Any>>

    @GET("announcements")
    suspend fun getAnnouncements(
        @Query("category") category: String? = null
    ): Response<com.sultanagung1.sista.core.network.ApiEnvelope<List<AnnouncementItem>>>

    @GET("announcements/{id}")
    suspend fun getAnnouncementDetail(
        @Path("id") id: String
    ): Response<com.sultanagung1.sista.core.network.ApiEnvelope<AnnouncementItem>>

    @POST("announcements/{id}/acknowledge")
    suspend fun acknowledgeAnnouncement(
        @Path("id") id: String
    ): Response<com.sultanagung1.sista.core.network.ApiEnvelope<AnnouncementItem>>
}
