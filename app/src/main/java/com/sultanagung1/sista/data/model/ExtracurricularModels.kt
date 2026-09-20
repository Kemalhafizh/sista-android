package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class EkskulItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String, // "Rohis Sultan Agung", "Paskibraka", "Pramuka Inti", "Klub Robotik", "Basket Putra/Putri"
    @SerializedName("category") val category: String, // "Keislaman", "Kepemimpinan", "Olahraga", "Sains & IT", "Seni Budaya"
    @SerializedName("coach_name") val coachName: String,
    @SerializedName("training_schedule") val trainingSchedule: String, // "Jumat 15:30 - 17:00 WIB"
    @SerializedName("location") val location: String, // "Lapangan Utama / Lab Komputer"
    @SerializedName("description") val description: String,
    @SerializedName("member_count") val memberCount: Int = 35,
    @SerializedName("is_registered") val isRegistered: Boolean = false
)

data class OsisPostItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String, // "Pengurus OSIS 2026/2027"
    @SerializedName("category") val category: String, // "Class Meeting", "Pensi Sultan Agung Fest", "Pemilu OSIS", "Sosial & Dakwah"
    @SerializedName("date") val date: String,
    @SerializedName("content") val content: String,
    @SerializedName("likes_count") val likesCount: Int = 128
)

data class EkskulAttendanceRequest(
    @SerializedName("ekskul_id") val ekskulId: Long,
    @SerializedName("qr_payload") val qrPayload: String,
    @SerializedName("student_id") val studentId: String
)
