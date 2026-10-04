package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ChildActivityEvent(
    @SerializedName("id") val id: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    /**
     * What happened, in a code that never changes with the language: present,
     * sick, permit, absent, attendance, grade, assessment, reward, violation,
     * loan, return, tahfidz. The title is translated, so never read it.
     */
    @SerializedName("code") val code: String? = null,
)

data class ChildActivityFeedResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ChildActivityEvent> = emptyList()
)

/** This week so far; a number is null when the school recorded nothing for it. */
data class WeeklyDigest(
    @SerializedName("weekStart") val weekStart: Long,
    @SerializedName("attendancePercentage") val attendancePercentage: Float? = null,
    @SerializedName("averageGrade") val averageGrade: Float? = null,
    @SerializedName("ibadahScore") val ibadahScore: Int? = null,
    @SerializedName("highlights") val highlights: List<String> = emptyList()
)

data class WeeklyDigestResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: WeeklyDigest? = null
)

data class ChildVsClassComparison(
    @SerializedName("subject") val subject: String,
    @SerializedName("childScore") val childScore: Float,
    /** Null when no classmate has a grade in this subject yet. */
    @SerializedName("classAverage") val classAverage: Float? = null
)

data class ChildComparisonResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ChildVsClassComparison> = emptyList()
)
