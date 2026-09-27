package com.sultanagung1.sista.core.lite

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LiteModeManager(private val context: Context) {

    private val _isLiteMode = MutableStateFlow(checkIfDeviceIsLowRam())
    val isLiteMode: StateFlow<Boolean> = _isLiteMode.asStateFlow()

    private val _isOfflineDownloadEnabled = MutableStateFlow(true)
    val isOfflineDownloadEnabled: StateFlow<Boolean> = _isOfflineDownloadEnabled.asStateFlow()

    private val _videoQuality = MutableStateFlow("360p (Hemat Kuota)")
    val videoQuality: StateFlow<String> = _videoQuality.asStateFlow()

    fun setLiteMode(enabled: Boolean) {
        _isLiteMode.value = enabled
    }

    fun setVideoQuality(quality: String) {
        _videoQuality.value = quality
    }

    fun setOfflineDownload(enabled: Boolean) {
        _isOfflineDownloadEnabled.value = enabled
    }

    fun getDeviceRamGb(): Double {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 4.0
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
    }

    private fun checkIfDeviceIsLowRam(): Boolean {
        return getDeviceRamGb() <= 3.0
    }
}
