package com.sultanagung1.sista.core.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import com.sultanagung1.sista.feature.cbt.R
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Reported to the server by [name] (log-violation), shown by [titleRes]. */
enum class ExamViolationType(@StringRes val titleRes: Int, val severity: String) {
    SCREENSHOT_ATTEMPT(R.string.cbt_v_screenshot, "HIGH"),
    WINDOW_FOCUS_LOST(R.string.cbt_v_focus_lost, "CRITICAL"),
    MULTI_WINDOW_SPLIT(R.string.cbt_v_split, "HIGH"),
    PICTURE_IN_PICTURE(R.string.cbt_v_pip, "HIGH"),
    OVERLAY_APPS_DETECTED(R.string.cbt_v_overlay, "MEDIUM"),
    ROOT_DETECTED(R.string.cbt_v_root, "CRITICAL"),
    EMULATOR_DETECTED(R.string.cbt_v_emulator, "CRITICAL"),
    USB_DEBUGGING_ACTIVE(R.string.cbt_v_usb_debug, "HIGH"),
}

data class ExamViolationRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ExamViolationType,
    val timestamp: Long = System.currentTimeMillis(),
    val violationNumber: Int
)

data class AntiCheatState(
    val isSecureModeActive: Boolean = false,
    val violationCount: Int = 0,
    /** The exam's own limit (exams.max_violations); null when the server didn't send one. */
    val maxViolationsAllowed: Int? = null,
    val isExamLocked: Boolean = false,
    /** The violation the student hasn't acknowledged yet. */
    val pendingWarning: ExamViolationRecord? = null,
    val violationHistory: List<ExamViolationRecord> = emptyList(),
    val isRooted: Boolean = false,
    val isEmulator: Boolean = false,
    val isUsbDebugging: Boolean = false,
    /** Null when the system didn't report it. */
    val batteryLevel: Int? = null,
    val isCharging: Boolean = false
)

/**
 * [maxViolationsAllowed] is the backend's per-exam exams.max_violations
 * (CbtTokenValidationResponse.maxViolations). The backend is the authority:
 * it blocks the attempt through log-violation at that count, and nothing on
 * the device can lift that block. Without a limit from the server the app
 * never locks on its own; it only warns and reports, and the server's
 * is_blocked answer locks the screen (see CbtViewModel.recordViolation).
 */
class CbtAntiCheatEngine(private val context: Context, maxViolationsAllowed: Int? = null) {

    private val _antiCheatState = MutableStateFlow(AntiCheatState(maxViolationsAllowed = maxViolationsAllowed))
    val antiCheatState: StateFlow<AntiCheatState> = _antiCheatState.asStateFlow()

    /**
     * Activate hardware-level security (FLAG_SECURE) and initialize anti-cheat guard.
     */
    fun activateExamSecurity(activity: Activity) {
        // 1. Hardware Window Protection (FLAG_SECURE blocks screenshot, screen record, HDMI mirror)
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 2. Scan hardware and OS integrity
        val isRoot = RootDetector.isDeviceRooted(context)
        val isEmu = EmulatorDetector.isEmulator()
        val isUsbDebug = isUsbDebuggingEnabled()
        val (battery, charging) = getBatteryStatus()

        _antiCheatState.value = _antiCheatState.value.copy(
            isSecureModeActive = true,
            isRooted = isRoot,
            isEmulator = isEmu,
            isUsbDebugging = isUsbDebug,
            batteryLevel = battery,
            isCharging = charging
        )

        if (isRoot) {
            recordViolation(ExamViolationType.ROOT_DETECTED)
        }
        if (isEmu) {
            recordViolation(ExamViolationType.EMULATOR_DETECTED)
        }
        if (isUsbDebug) {
            recordViolation(ExamViolationType.USB_DEBUGGING_ACTIVE)
        }
    }

    /**
     * Deactivate hardware-level security after exam completion.
     */
    fun deactivateExamSecurity(activity: Activity) {
        try {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } catch (_: Exception) {}
        _antiCheatState.value = _antiCheatState.value.copy(isSecureModeActive = false)
    }

    /**
     * Check if app is in multi-window or picture-in-picture mode.
     */
    fun verifyMultiWindowMode(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (activity.isInMultiWindowMode) {
                recordViolation(ExamViolationType.MULTI_WINDOW_SPLIT)
                return false
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity.isInPictureInPictureMode) {
                recordViolation(ExamViolationType.PICTURE_IN_PICTURE)
                return false
            }
        }
        return true
    }

    /**
     * Called when the exam window loses focus (e.g., student pulls down notification bar, switches apps).
     */
    fun onWindowFocusLost() {
        val currentState = _antiCheatState.value
        if (!currentState.isSecureModeActive || currentState.isExamLocked) return

        recordViolation(ExamViolationType.WINDOW_FOCUS_LOST)
    }

    /** Record a violation; the screen locks once the exam's own limit is reached. */
    fun recordViolation(type: ExamViolationType) {
        val current = _antiCheatState.value
        val newViolationCount = current.violationCount + 1
        val record = ExamViolationRecord(type = type, violationNumber = newViolationCount)
        val limit = current.maxViolationsAllowed

        _antiCheatState.value = current.copy(
            violationCount = newViolationCount,
            isExamLocked = current.isExamLocked || (limit != null && newViolationCount >= limit),
            pendingWarning = record,
            violationHistory = current.violationHistory + record
        )
    }

    /** The server blocked the attempt (log-violation answered is_blocked). */
    fun lockByServer() {
        _antiCheatState.value = _antiCheatState.value.copy(isExamLocked = true)
    }

    /** Dismiss the warning dialog; a locked exam keeps its lock screen. */
    fun dismissWarningDialog() {
        _antiCheatState.value = _antiCheatState.value.copy(pendingWarning = null)
    }

    private fun isUsbDebuggingEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
        } catch (_: Exception) {
            false
        }
    }

    private fun getBatteryStatus(): Pair<Int?, Boolean> {
        val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else null

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        return Pair(batteryPct, isCharging)
    }
}
