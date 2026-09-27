package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ChildActivityEvent(
    @SerializedName("id") val id: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String
)

data class ChildActivityFeedResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ChildActivityEvent> = emptyList()
)

data class WeeklyDigest(
    @SerializedName("weekStart") val weekStart: Long,
    @SerializedName("attendancePercentage") val attendancePercentage: Float,
    @SerializedName("averageGrade") val averageGrade: Float,
    @SerializedName("ibadahScore") val ibadahScore: Int,
    @SerializedName("notes") val notes: String? = null
)

data class WeeklyDigestResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: WeeklyDigest? = null
)

data class ChildVsClassComparison(
    @SerializedName("subject") val subject: String,
    @SerializedName("childScore") val childScore: Float,
    @SerializedName("classAverage") val classAverage: Float
)

data class ChildComparisonResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ChildVsClassComparison> = emptyList()
)
