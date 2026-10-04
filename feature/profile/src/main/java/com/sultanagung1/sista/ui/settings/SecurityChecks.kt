package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.feature.profile.R
import androidx.annotation.StringRes
import com.sultanagung1.sista.core.security.DeviceIntegrityReport
import com.sultanagung1.sista.core.ui.theme.StatusTone

/** One check this phone actually ran, worded for the person holding it. */
data class SecurityCheck(@StringRes val title: Int, @StringRes val detail: Int, val passed: Boolean, val tone: StatusTone)

/**
 * Only what DeviceIntegrityChecker measures. There used to be a "TLS 1.3 &
 * Certificate Pinning" row that always passed, but the app pins no
 * certificate, so it isn't listed.
 */
fun securityChecks(report: DeviceIntegrityReport): List<SecurityCheck> = listOf(
    SecurityCheck(
        R.string.sec_root,
        if (report.isRooted) R.string.sec_root_found else R.string.sec_root_ok,
        passed = !report.isRooted,
        tone = if (report.isRooted) StatusTone.Danger else StatusTone.Success,
    ),
    SecurityCheck(
        R.string.sec_emulator,
        if (report.isEmulator) R.string.sec_emulator_found else R.string.sec_emulator_ok,
        passed = !report.isEmulator,
        tone = if (report.isEmulator) StatusTone.Danger else StatusTone.Success,
    ),
    SecurityCheck(
        R.string.sec_usb,
        if (report.isUsbDebuggingEnabled) R.string.sec_usb_on else R.string.sec_usb_off,
        passed = !report.isUsbDebuggingEnabled,
        tone = if (report.isUsbDebuggingEnabled) StatusTone.Warning else StatusTone.Success,
    ),
)

/** Root or an emulator makes the phone unsafe; debugging alone is a warning. */
fun securityVerdict(report: DeviceIntegrityReport): Pair<Int, StatusTone> = when {
    !report.isSecure -> R.string.sec_verdict_attention to StatusTone.Danger
    report.isUsbDebuggingEnabled -> R.string.sec_verdict_note to StatusTone.Warning
    else -> R.string.sec_verdict_safe to StatusTone.Success
}

/** The signing certificate's SHA-256, grouped for reading; null when it couldn't be read. */
fun signatureLabel(hash: String): String? =
    hash.trim().uppercase().takeIf { it.length >= 16 && it.all { c -> c.isLetterOrDigit() || c == ':' } }
        ?.replace(":", "")
        ?.chunked(4)
        ?.joinToString(" ")
