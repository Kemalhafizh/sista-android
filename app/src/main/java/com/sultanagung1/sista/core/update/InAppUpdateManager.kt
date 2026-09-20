package com.sultanagung1.sista.core.update

import android.content.Context
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.data.model.VersionCheckInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppVersionState(
    val isLoading: Boolean = false,
    val currentVersionName: String = "",
    val versionCheck: VersionCheckInfo? = null,
    val maintenanceMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Wraps the real, working GET mobile/config endpoint (MobileConfigController) —
 * no auth required, backed by an actual MobileAppConfig DB row. There is no
 * APK-hosting/download endpoint anywhere in the backend, so this intentionally
 * does not attempt an in-app byte-progress download: the only real action is
 * directing the user to version_check.store_url (the Play Store listing).
 */
class InAppUpdateManager(
    private val context: Context,
    private val apiClient: ApiClient
) {

    private val _state = MutableStateFlow(AppVersionState())
    val state: StateFlow<AppVersionState> = _state.asStateFlow()

    private fun currentBuildNumber(): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) it.longVersionCode.toInt() else @Suppress("DEPRECATION") it.versionCode
            }
        } catch (_: Exception) {
            0
        }
    }

    private fun currentVersionName(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun checkForUpdates() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        try {
            val build = currentBuildNumber()
            val response = apiClient.mobileConfigApi.getConfig(platform = "android", build = build)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                _state.value = AppVersionState(
                    isLoading = false,
                    currentVersionName = currentVersionName(),
                    versionCheck = body.versionCheck,
                    maintenanceMessage = body.maintenance?.takeIf { it.isMaintenance }?.message
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Gagal memeriksa pembaruan aplikasi.")
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Koneksi terputus.")
        }
    }
}
