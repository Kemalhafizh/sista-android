package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class NotificationPreferences(
    @SerializedName("examNotifsEnabled") val examNotifsEnabled: Boolean = true,
    @SerializedName("assignmentNotifsEnabled") val assignmentNotifsEnabled: Boolean = true,
    @SerializedName("infoNotifsEnabled") val infoNotifsEnabled: Boolean = true,
    @SerializedName("prayerNotifsEnabled") val prayerNotifsEnabled: Boolean = true,
    @SerializedName("digestMode") val digestMode: String = "realtime"
)

data class NotificationPreferencesResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: NotificationPreferences? = null
)
