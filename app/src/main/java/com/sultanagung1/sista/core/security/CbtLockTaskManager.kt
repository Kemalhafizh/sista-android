package com.sultanagung1.sista.core.security

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CbtLockTaskManager(private val context: Context) {

    private val _isLockTaskActive = MutableStateFlow(false)
    val isLockTaskActive: StateFlow<Boolean> = _isLockTaskActive.asStateFlow()

    /**
     * Start Android Lock Task (Kiosk) Mode.
     * Locks navigation bar, status bar, and prevents switching to other apps.
     */
    fun startKioskMode(activity: Activity): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val isLocked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
                } else {
                    am?.isInLockTaskMode == true
                }

                if (!isLocked) {
                    activity.startLockTask()
                    _isLockTaskActive.value = true
                    Log.d("CbtLockTaskManager", "Lock Task Mode started successfully.")
                    true
                } else {
                    _isLockTaskActive.value = true
                    true
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w("CbtLockTaskManager", "Failed to start Lock Task Mode: ${e.message}")
            _isLockTaskActive.value = false
            false
        }
    }

    /**
     * Stop Android Lock Task (Kiosk) Mode.
     */
    fun stopKioskMode(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.stopLockTask()
                _isLockTaskActive.value = false
                Log.d("CbtLockTaskManager", "Lock Task Mode stopped.")
            }
        } catch (e: Exception) {
            Log.w("CbtLockTaskManager", "Failed to stop Lock Task Mode: ${e.message}")
            _isLockTaskActive.value = false
        }
    }
}
