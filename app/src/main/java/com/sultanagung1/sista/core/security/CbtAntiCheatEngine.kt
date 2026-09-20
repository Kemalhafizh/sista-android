package com.sultanagung1.sista.core.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ExamViolationType(val title: String, val severity: String) {
    SCREENSHOT_ATTEMPT("Percobaan Tangkapan Layar (Screenshot)", "HIGH"),
    WINDOW_FOCUS_LOST("Beralih Aplikasi / Notifikasi Dibuka", "CRITICAL"),
    MULTI_WINDOW_SPLIT("Mode Layar Terpisah (Split Screen)", "HIGH"),
    PICTURE_IN_PICTURE("Mode Floating / Picture-in-Picture", "HIGH"),
    OVERLAY_APPS_DETECTED("Aplikasi Melayang / Pop-up Terdeteksi", "MEDIUM"),
    ROOT_DETECTED("Perangkat Di-Root (Magisk / SU)", "CRITICAL"),
    EMULATOR_DETECTED("Ujian Dijalankan di Emulator PC", "CRITICAL"),
    USB_DEBUGGING_ACTIVE("USB Debugging / Developer Options Aktif", "HIGH"),
    SUPERVISOR_UNLOCKED("Ujian Dibuka Kunci oleh Pengawas", "INFO")
}

data class ExamViolationRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ExamViolationType,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String,
    val violationNumber: Int
)

data class AntiCheatState(
    val isSecureModeActive: Boolean = false,
    val violationCount: Int = 0,
    val maxViolationsAllowed: Int = 3,
    val isExamLocked: Boolean = false,
    val currentWarningTitle: String? = null,
    val currentWarningMessage: String? = null,
    val violationHistory: List<ExamViolationRecord> = emptyList(),
    val isRooted: Boolean = false,
    val isEmulator: Boolean = false,
    val isUsbDebugging: Boolean = false,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false
)

class CbtAntiCheatEngine(private val context: Context) {

    private val _antiCheatState = MutableStateFlow(AntiCheatState())
    val antiCheatState: StateFlow<AntiCheatState> = _antiCheatState.asStateFlow()

    // Cryptographically hashed supervisor unlock PINs (SHA-256) to prevent static bytecode extraction
    private val supervisorPinHashes = setOf(
        "b7654751edc0a8c9aca430a7b60f099e5986828dcfad2f4785ecd6301ffd06f7", // SA1-Primary
        "52e1699c474ba69e0af142935c2345fc57885c668e8c41bbff85c549dc55ec1c", // SA1-Fallback
        "05853cc8379476d8bb3d350625a3bc08b91214049be1847d4e5403cc44715e31"  // SA1-Emergency
    )

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
            recordViolation(ExamViolationType.ROOT_DETECTED, "Perangkat memiliki akses SU / Magisk aktif")
        }
        if (isEmu) {
            recordViolation(ExamViolationType.EMULATOR_DETECTED, "Aplikasi terdeteksi berjalan di emulator PC / VM")
        }
        if (isUsbDebug) {
            recordViolation(ExamViolationType.USB_DEBUGGING_ACTIVE, "Opsi pengembang / ADB Debugging aktif di sistem Android")
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
                recordViolation(
                    ExamViolationType.MULTI_WINDOW_SPLIT,
                    "Siswa membagi layar menjadi 2 aplikasi bersamaan"
                )
                return false
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity.isInPictureInPictureMode) {
                recordViolation(
                    ExamViolationType.PICTURE_IN_PICTURE,
                    "Aplikasi ujian dikecilkan ke mode Picture-in-Picture"
                )
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

        recordViolation(
            ExamViolationType.WINDOW_FOCUS_LOST,
            "Siswa terdeteksi meninggalkan layar ujian atau membuka panel notifikasi/aplikasi lain"
        )
    }

    /**
     * Record violation and update exam lockdown status.
     */
    fun recordViolation(type: ExamViolationType, details: String) {
        val current = _antiCheatState.value
        val newViolationCount = current.violationCount + 1
        val newHistory = current.violationHistory + ExamViolationRecord(
            type = type,
            details = details,
            violationNumber = newViolationCount
        )

        val isLocked = newViolationCount >= current.maxViolationsAllowed

        val (warningTitle, warningMessage) = when {
            isLocked -> Pair(
                "UJIAN TERKUNCI! (PELANGGARAN MAKSIMAL)",
                "Anda telah melanggar integritas ujian sebanyak $newViolationCount kali. Lembar ujian telah dikunci otomatis. Hubungi Pengawas Guru untuk meminta PIN Buka Kunci."
            )
            newViolationCount == 2 -> Pair(
                "PERINGATAN KERAS! (2/3)",
                "Perhatian! Anda terdeteksi $details. Peringatan ke-2 dari maksimal 3. Jika terjadi 1 kali lagi, ujian akan langsung dikunci otomatis!"
            )
            else -> Pair(
                "Peringatan Integritas Ujian (1/3)",
                "Terdeteksi: $details. Dilarang membuka aplikasi lain, split screen, atau screenshot selama ujian berlangsung."
            )
        }

        _antiCheatState.value = current.copy(
            violationCount = newViolationCount,
            isExamLocked = isLocked,
            currentWarningTitle = warningTitle,
            currentWarningMessage = warningMessage,
            violationHistory = newHistory
        )
    }

    /**
     * Unlock the exam using Teacher Supervisor PIN or dynamic token.
     */
    fun unlockExamWithSupervisorPin(pin: String): Boolean {
        val cleanPin = pin.trim().uppercase()
        val hashedPin = sha256(cleanPin)
        val isValid = hashedPin in supervisorPinHashes

        if (isValid) {
            val current = _antiCheatState.value
            _antiCheatState.value = current.copy(
                isExamLocked = false,
                currentWarningTitle = null,
                currentWarningMessage = null,
                violationHistory = current.violationHistory + ExamViolationRecord(
                    type = ExamViolationType.SUPERVISOR_UNLOCKED,
                    details = "Ujian berhasil dibuka kunci oleh Pengawas",
                    violationNumber = current.violationCount
                )
            )
            return true
        }
        return false
    }

    private fun sha256(input: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Dismiss warning dialog for violation count < max.
     */
    fun dismissWarningDialog() {
        if (!_antiCheatState.value.isExamLocked) {
            _antiCheatState.value = _antiCheatState.value.copy(
                currentWarningTitle = null,
                currentWarningMessage = null
            )
        }
    }

    private fun isUsbDebuggingEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
        } catch (_: Exception) {
            false
        }
    }

    private fun getBatteryStatus(): Pair<Int, Boolean> {
        val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        return Pair(batteryPct, isCharging)
    }
}
