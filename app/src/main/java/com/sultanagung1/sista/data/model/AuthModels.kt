package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String, // Accepts NISN / NIP / Email
    @SerializedName("password") val password: String,
    @SerializedName("device_name") val deviceName: String = "Android Device"
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LoginData? = null
)

data class LoginData(
    @SerializedName("token") val token: String = "",
    @SerializedName("user") val user: UserProfile? = null
)

data class UserProfile(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("role") val role: String = "student",
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("nisn") val nisn: String? = null,
    @SerializedName("nip") val nip: String? = null,
    @SerializedName("classroom") val classroom: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class BiometricChallengeRequest(
    @SerializedName("device_id") val deviceId: String
)

data class BiometricChallengeResponse(
    @SerializedName("challenge") val challenge: String
)

data class BiometricVerifyRequest(
    @SerializedName("challenge") val challenge: String,
    @SerializedName("signature") val signature: String
)
