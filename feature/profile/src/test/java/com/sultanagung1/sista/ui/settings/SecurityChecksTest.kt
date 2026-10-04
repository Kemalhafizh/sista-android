package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.core.security.DeviceIntegrityReport
import com.sultanagung1.sista.core.ui.theme.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SecurityChecksTest {

    private fun report(rooted: Boolean = false, emulator: Boolean = false, adb: Boolean = false, hash: String = "AB".repeat(32)) =
        DeviceIntegrityReport(rooted, emulator, adb, hash, isSecure = !rooted && !emulator)

    @Test
    fun onlyChecksThePhoneRuns() {
        val titles = securityChecks(report()).map { it.title }
        assertEquals(listOf("Akses root", "Emulator", "USB debugging"), titles)
        // The app pins no certificate, so it must not claim to.
        assertFalse(titles.any { it.contains("Pinning", ignoreCase = true) || it.contains("TLS") })
    }

    @Test
    fun verdict() {
        assertEquals("Aman" to StatusTone.Success, securityVerdict(report()))
        assertEquals("Aman, ada catatan" to StatusTone.Warning, securityVerdict(report(adb = true)))
        assertEquals("Perlu perhatian" to StatusTone.Danger, securityVerdict(report(rooted = true)))
        assertEquals("Perlu perhatian" to StatusTone.Danger, securityVerdict(report(emulator = true)))
    }

    @Test
    fun failedChecksAreMarked() {
        val checks = securityChecks(report(rooted = true, adb = true)).associateBy { it.title }
        assertEquals(StatusTone.Danger, checks.getValue("Akses root").tone)
        assertEquals(StatusTone.Success, checks.getValue("Emulator").tone)
        assertEquals(StatusTone.Warning, checks.getValue("USB debugging").tone)
    }

    @Test
    fun anUnreadableSignatureIsNotReplacedByASampleHash() {
        // The old screen showed the SHA-256 of an empty string when the real one was missing.
        assertNull(signatureLabel(""))
        assertEquals("ABAB ABAB", signatureLabel("abababab".repeat(2))?.take(9))
    }
}
