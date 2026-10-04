package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.SchoolIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The profile page's data: who is signed in (GET me) and which school this is
 * (GET mobile/config → school). Both come from the server; when a call fails
 * the page says so instead of showing a stand-in identity.
 */
class ProfileRepository(private val apiClient: ApiClient) {

    suspend fun me(): NetworkResult<MeProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.authApi.getCurrentUser()
            val profile = response.body()?.data
            if (response.isSuccessful && profile != null) {
                NetworkResult.Success(profile)
            } else {
                NetworkResult.Error(
                    serverMessageOf(response.errorBody()?.string()) ?: "Profil belum bisa dimuat (kode ${response.code()}).",
                    response.code(),
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus.")
        }
    }

    /** Null when the server cannot be reached; the page then shows only what the app knows. */
    suspend fun school(): SchoolIdentity? = withContext(Dispatchers.IO) {
        try {
            // Only the school block is read here; the version check is InAppUpdateManager's.
            apiClient.mobileConfigApi.getConfig(platform = "android", build = 0).body()?.school
        } catch (_: Exception) {
            null
        }
    }
}
