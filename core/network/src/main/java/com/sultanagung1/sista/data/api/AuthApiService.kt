package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("mobile/auth/biometric/challenge")
    suspend fun requestBiometricChallenge(
        @Body request: BiometricChallengeRequest
    ): Response<BiometricChallengeResponse>

    @POST("mobile/auth/biometric/verify")
    suspend fun verifyBiometric(
        @Body request: BiometricVerifyRequest
    ): Response<BiometricVerifyResponse>

    @POST("mobile/auth/biometric/register")
    suspend fun registerBiometric(
        @Body request: RegisterBiometricRequest
    ): Response<RegisterBiometricResponse>

    // The profile page's identity card (ApiAuthController::me).
    @GET("me")
    suspend fun getCurrentUser(): Response<ApiEnvelope<MeProfile>>

    // Saves the app's language on the account (users.preferred_locale, read by the web too).
    @PUT("me/locale")
    suspend fun updateLocale(@Body request: LocaleRequest): Response<ApiEnvelope<LocaleData>>

    @POST("logout")
    suspend fun logout(): Response<Map<String, Any>>
}
