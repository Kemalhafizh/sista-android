package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class GpsCheckinRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float,
    @SerializedName("is_mock") val isMock: Boolean,
    @SerializedName("device_fingerprint") val deviceFingerprint: String
)

data class AttendanceCheckinResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("checkin_time") val checkinTime: String,
    @SerializedName("status_type") val statusType: String // HADIR, TERLAMBAT
)

data class DynamicQrResponse(
    @SerializedName("qr_payload") val qrPayload: String,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int,
    @SerializedName("timestamp") val timestamp: Long
)

data class AttendanceHistoryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("date") val date: String,
    @SerializedName("check_in") val checkIn: String?,
    @SerializedName("check_out") val checkOut: String?,
    @SerializedName("status") val status: String, // H, S, I, A
    @SerializedName("notes") val notes: String?
)

data class FaceEnrollRequest(
    @SerializedName("embedding_vector") val embeddingVector: List<Float>,
    @SerializedName("biometric_device_id") val biometricDeviceId: String? = null
)
