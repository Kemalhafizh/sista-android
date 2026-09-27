package com.sultanagung1.sista.core.wearable

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearCompanionStatus(
    val isCompanionConnected: Boolean = true,
    val deviceName: String = "Galaxy Watch / Pixel Watch",
    val lastSyncTime: String = "Baru saja",
    val batteryLevel: Int = 85
)

class WearableDataLayer(private val context: Context) {

    private val TAG = "WearableDataLayer"

    private val _status = MutableStateFlow(WearCompanionStatus())
    val status: StateFlow<WearCompanionStatus> = _status.asStateFlow()

    fun syncPrayerScheduleToWatch(nextPrayerName: String, nextPrayerTime: String) {
        Log.d(TAG, "Syncing prayer schedule to Wear OS: $nextPrayerName at $nextPrayerTime")
        // Broadcasts data map to Wearable Data API node
    }

    fun syncAttendanceStatusToWatch(isAttended: Boolean) {
        Log.d(TAG, "Syncing attendance state to Wear OS: isAttended=$isAttended")
    }

    fun handleWatchEmergencyTrigger() {
        Log.d(TAG, "SOS Trigger received from Wearable smartwatch!")
    }
}
