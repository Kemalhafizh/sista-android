package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {
    @GET("mobile/conversations")
    suspend fun getConversations(): Response<List<ConversationItem>>

    @GET("mobile/conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: String,
        @Query("page") page: Int = 1
    ): Response<List<ChatMessage>>

    @POST("mobile/messages/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ChatMessage>

    @POST("mobile/conversations/{id}/read")
    suspend fun markAsRead(
        @Path("id") conversationId: String
    ): Response<Map<String, Any>>
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
