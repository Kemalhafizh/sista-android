package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches MutabaahLogResource.php exactly: one row per recorded date, not a
 * fixed checklist template (the backend has no such template endpoint).
 */
data class MutabaahLogItem(
    @SerializedName("id") val id: Long,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("category") val category: String,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("date") val date: String,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("sholat_subuh") val sholatSubuh: Boolean = false,
    @SerializedName("sholat_dzuhur") val sholatDzuhur: Boolean = false,
    @SerializedName("sholat_ashar") val sholatAshar: Boolean = false,
    @SerializedName("sholat_maghrib") val sholatMaghrib: Boolean = false,
    @SerializedName("sholat_isya") val sholatIsya: Boolean = false,
    @SerializedName("sholat_dhuha") val sholatDhuha: Boolean = false,
    @SerializedName("sholat_tahajud") val sholatTahajud: Boolean = false,
    @SerializedName("tadarus_pages") val tadarusPages: Int = 0,
    @SerializedName("puasa_sunnah") val puasaSunnah: Boolean = false,
    @SerializedName("sedekah") val sedekah: Boolean = false,
    @SerializedName("dzikir_pagi") val dzikirPagi: Boolean = false,
    @SerializedName("dzikir_sore") val dzikirSore: Boolean = false,
    @SerializedName("catatan_harian") val catatanHarian: String? = null
)

/**
 * Matches TahfidzLogResource.php — real Qur'an memorization (setoran) log,
 * distinct from the audio-recording+grading workflow TahsinRecorderScreen
 * implies; there is no backend support for that workflow yet.
 */
data class TahfidzLogItem(
    @SerializedName("id") val id: Long,
    @SerializedName("surah_name") val surahName: String,
    @SerializedName("surah_number") val surahNumber: Int? = null,
    @SerializedName("from_ayat") val fromAyat: Int? = null,
    @SerializedName("to_ayat") val toAyat: Int? = null,
    @SerializedName("type") val type: String, // ziyadah | murajaah
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("date") val date: String,
    @SerializedName("teacher") val teacher: String
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
