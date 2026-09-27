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

/**
 * `GET mobile/attendance/dynamic-qr` — raw JSON, no envelope
 * (GeofenceAttendanceService::generateDynamicTotpQr): `{success, qr_token,
 * expires_in_seconds, timestamp}`. [qrToken] is what the gate scanner sends to
 * `mobile/attendance/verify-qr`. The model used to expect `qr_payload` and a
 * numeric `timestamp`; the server sends `qr_token` and an ISO-8601 string, so
 * Gson failed on every response and the screen could only show an error.
 */
data class DynamicQrResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("qr_token") val qrToken: String? = null,
    /** Seconds until the 30 s slice rolls over (1–30). */
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int = 0,
    @SerializedName("timestamp") val timestamp: String? = null
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
