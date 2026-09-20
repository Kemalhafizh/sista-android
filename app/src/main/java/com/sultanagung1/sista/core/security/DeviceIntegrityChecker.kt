package com.sultanagung1.sista.core.security

import android.content.Context
import android.provider.Settings

data class DeviceIntegrityReport(
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val isUsbDebuggingEnabled: Boolean,
    val signatureHash: String,
    val isSecure: Boolean
)

class DeviceIntegrityChecker(private val context: Context) {

    fun checkIntegrity(): DeviceIntegrityReport {
        val isRooted = RootDetector.isDeviceRooted(context)
        val isEmulator = EmulatorDetector.isEmulator()
        val isUsbDebugging = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
        } catch (e: Exception) {
            false
        }
        val sigHash = AppSignatureVerifier.getAppSignatureHash(context)
        val isSecure = !isRooted && !isEmulator

        return DeviceIntegrityReport(
            isRooted = isRooted,
            isEmulator = isEmulator,
            isUsbDebuggingEnabled = isUsbDebugging,
            signatureHash = sigHash,
            isSecure = isSecure
        )
    }
}
