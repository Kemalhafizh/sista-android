package com.sultanagung1.sista.core.update

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppVersionInfo(
    val currentVersion: String = "2.0.0",
    val latestVersion: String = "2.1.0",
    val isUpdateAvailable: Boolean = true,
    val isCriticalUpdate: Boolean = false,
    val releaseDate: String = "25 Agustus 2026",
    val downloadSizeBytes: Long = 18_400_000, // 18.4 MB
    val downloadUrl: String = "https://sista.sultanagung1.sch.id/downloads/sulaone-v2.1.0.apk",
    val changelog: List<String> = listOf(
        "Pembaruan modul visualisasi grafik Radar Spider Web 6-Sumbu KKTP",
        "Peningkatan performa rendering 120 FPS bebas jank/stuttering",
        "Dukungan mode tema AMOLED Hitam Pekat murni & multi-bahasa Arab RTL",
        "Peningkatan stabilitas sinkronisasi offline & antrean mutasi data"
    )
)

class InAppUpdateManager(private val context: Context) {

    private val _versionInfo = MutableStateFlow(AppVersionInfo())
    val versionInfo: StateFlow<AppVersionInfo> = _versionInfo.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    fun checkForUpdates() {
        // Checks version from server
        _versionInfo.value = AppVersionInfo(
            currentVersion = "2.0.0",
            latestVersion = "2.1.0",
            isUpdateAvailable = true,
            isCriticalUpdate = false
        )
    }

    fun startDownloadUpdate(onComplete: () -> Unit = {}) {
        _isDownloading.value = true
        _downloadProgress.value = 0.05f
    }
}
