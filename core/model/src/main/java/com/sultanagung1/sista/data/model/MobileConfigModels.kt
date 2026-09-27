package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/** Matches MobileConfigController::getConfig() exactly — a real, working, unauthenticated endpoint. */
data class MobileConfigResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("app_name") val appName: String? = null,
    @SerializedName("server_time") val serverTime: String? = null,
    @SerializedName("maintenance") val maintenance: MaintenanceInfo? = null,
    @SerializedName("version_check") val versionCheck: VersionCheckInfo? = null,
    @SerializedName("contact_support") val contactSupport: ContactSupportInfo? = null
)

data class MaintenanceInfo(
    @SerializedName("is_maintenance") val isMaintenance: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class VersionCheckInfo(
    @SerializedName("current_client_build") val currentClientBuild: Int = 0,
    @SerializedName("minimum_required_build") val minimumRequiredBuild: Int = 0,
    @SerializedName("latest_available_build") val latestAvailableBuild: Int = 0,
    @SerializedName("update_required") val updateRequired: Boolean = false,
    @SerializedName("update_available") val updateAvailable: Boolean = false,
    @SerializedName("store_url") val storeUrl: String? = null
)

data class ContactSupportInfo(
    @SerializedName("whatsapp") val whatsapp: String? = null,
    @SerializedName("email") val email: String? = null
)
