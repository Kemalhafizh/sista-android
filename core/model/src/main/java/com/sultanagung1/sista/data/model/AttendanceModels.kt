package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class AttendanceHistoryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("date") val date: String,
    @SerializedName("check_in") val checkIn: String?,
    @SerializedName("check_out") val checkOut: String?,
    @SerializedName("status") val status: String, // H, S, I, A
    @SerializedName("notes") val notes: String?
)
