package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.core.security.DeviceIntegrityReport
import com.sultanagung1.sista.core.ui.theme.StatusTone

/** One check this phone actually ran, worded for the person holding it. */
data class SecurityCheck(val title: String, val detail: String, val passed: Boolean, val tone: StatusTone)

/**
 * Only what DeviceIntegrityChecker measures. There used to be a "TLS 1.3 &
 * Certificate Pinning" row that always passed, but the app pins no
 * certificate, so it isn't listed.
 */
fun securityChecks(report: DeviceIntegrityReport): List<SecurityCheck> = listOf(
    SecurityCheck(
        "Akses root",
        if (report.isRooted) "Terdeteksi tanda root. Data akun di HP ini bisa dibaca aplikasi lain." else "Tidak ditemukan tanda root.",
        passed = !report.isRooted,
        tone = if (report.isRooted) StatusTone.Danger else StatusTone.Success,
    ),
    SecurityCheck(
        "Emulator",
        if (report.isEmulator) "Aplikasi berjalan di emulator. Ujian CBT tidak bisa dikerjakan di sini." else "Berjalan di HP fisik.",
        passed = !report.isEmulator,
        tone = if (report.isEmulator) StatusTone.Danger else StatusTone.Success,
    ),
    SecurityCheck(
        "USB debugging",
        if (report.isUsbDebuggingEnabled) "Aktif. Matikan di Opsi pengembang bila tidak sedang dipakai." else "Nonaktif.",
        passed = !report.isUsbDebuggingEnabled,
        tone = if (report.isUsbDebuggingEnabled) StatusTone.Warning else StatusTone.Success,
    ),
)

/** Root or an emulator makes the phone unsafe; debugging alone is a warning. */
fun securityVerdict(report: DeviceIntegrityReport): Pair<String, StatusTone> = when {
    !report.isSecure -> "Perlu perhatian" to StatusTone.Danger
    report.isUsbDebuggingEnabled -> "Aman, ada catatan" to StatusTone.Warning
    else -> "Aman" to StatusTone.Success
}

/** The signing certificate's SHA-256, grouped for reading; null when it couldn't be read. */
fun signatureLabel(hash: String): String? =
    hash.trim().uppercase().takeIf { it.length >= 16 && it.all { c -> c.isLetterOrDigit() || c == ':' } }
        ?.replace(":", "")
        ?.chunked(4)
        ?.joinToString(" ")
