package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.core.security.DeviceIntegrityReport
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.profile.R
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
        // Root, emulator, USB debugging only: the app pins no certificate, so it must not claim to.
        assertEquals(listOf(R.string.sec_root, R.string.sec_emulator, R.string.sec_usb), titles)
    }

    @Test
    fun verdict() {
        assertEquals(R.string.sec_verdict_safe to StatusTone.Success, securityVerdict(report()))
        assertEquals(R.string.sec_verdict_note to StatusTone.Warning, securityVerdict(report(adb = true)))
        assertEquals(R.string.sec_verdict_attention to StatusTone.Danger, securityVerdict(report(rooted = true)))
        assertEquals(R.string.sec_verdict_attention to StatusTone.Danger, securityVerdict(report(emulator = true)))
    }

    @Test
    fun failedChecksAreMarked() {
        val checks = securityChecks(report(rooted = true, adb = true)).associateBy { it.title }
        assertEquals(StatusTone.Danger, checks.getValue(R.string.sec_root).tone)
        assertEquals(StatusTone.Success, checks.getValue(R.string.sec_emulator).tone)
        assertEquals(StatusTone.Warning, checks.getValue(R.string.sec_usb).tone)
    }

    @Test
    fun anUnreadableSignatureIsNotReplacedByASampleHash() {
        // The old screen showed the SHA-256 of an empty string when the real one was missing.
        assertNull(signatureLabel(""))
        assertEquals("ABAB ABAB", signatureLabel("abababab".repeat(2))?.take(9))
    }
}
