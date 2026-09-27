package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class TahsinSubmissionItem(
    @SerializedName("id") val id: Long,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("surah_name") val surahName: String,
    @SerializedName("start_ayah") val startAyah: Int? = null,
    @SerializedName("end_ayah") val endAyah: Int? = null,
    @SerializedName("audio_url") val audioUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("status") val status: String, // "pending" | "reviewed"
    @SerializedName("annotation_count") val annotationCount: Int = 0,
    @SerializedName("reviewed_at") val reviewedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("student_id") val studentId: Long? = null,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("annotations") val annotations: List<TahsinAnnotationItem>? = null
)

data class TahsinAnnotationItem(
    @SerializedName("id") val id: Long,
    @SerializedName("timestamp_seconds") val timestampSeconds: Int,
    @SerializedName("note") val note: String,
    @SerializedName("tajwid_category") val tajwidCategory: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class TahsinAnnotateRequest(
    @SerializedName("timestamp_seconds") val timestampSeconds: Int,
    @SerializedName("note") val note: String,
    @SerializedName("tajwid_category") val tajwidCategory: String? = null
)
