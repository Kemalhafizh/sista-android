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
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    // id/en/ar saved on the account (also used by the web).
    @SerializedName("preferred_locale") val preferredLocale: String? = null
)

data class LocaleRequest(@SerializedName("locale") val locale: String)

data class LocaleData(@SerializedName("locale") val locale: String? = null)

data class BiometricChallengeRequest(
    @SerializedName("device_id") val deviceId: String
)

data class BiometricChallengeResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("nonce") val nonce: String = "",
    @SerializedName("expires_in") val expiresIn: Int = 0
)

data class BiometricVerifyRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("signature") val signature: String
)

// NOTE: MobileAuthController::biometricVerify returns token/user as TOP-LEVEL
// fields (not nested under "data" like the password-login response), so this
// needs its own shape rather than reusing LoginResponse/LoginData.
data class BiometricVerifyResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String = "",
    @SerializedName("user") val user: UserProfile? = null
)

data class RegisterBiometricRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("public_key") val publicKey: String,
    @SerializedName("biometric_type") val biometricType: String = "fingerprint"
)

data class RegisterBiometricResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)
