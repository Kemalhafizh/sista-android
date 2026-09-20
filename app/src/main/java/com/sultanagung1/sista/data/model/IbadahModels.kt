package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class MutabaahItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // FARDHU, SUNNAH, ADAB
    var isCompleted: Boolean = false
)

data class TahsinRecordItem(
    @SerializedName("id") val id: Long,
    @SerializedName("surah_name") val surahName: String,
    @SerializedName("ayah_range") val ayahRange: String,
    @SerializedName("audio_url") val audioUrl: String,
    @SerializedName("score") val score: String? = null,
    @SerializedName("teacher_notes") val teacherNotes: String? = null,
    @SerializedName("status") val status: String // SUBMITTED, REVIEWED, PASSED
)

data class PrayerSchedule(
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val currentPrayer: String,
    val nextPrayerName: String,
    val nextPrayerCountdown: String
)
