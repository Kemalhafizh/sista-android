package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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

    @GET("me")
    suspend fun getCurrentUser(): Response<UserProfile>

    @POST("logout")
    suspend fun logout(): Response<Map<String, Any>>
}
