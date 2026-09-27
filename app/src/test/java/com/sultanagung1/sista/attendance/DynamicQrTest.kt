package com.sultanagung1.sista.attendance

import com.google.gson.Gson
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.sultanagung1.sista.core.designsystem.QrMatrixEncoder
import com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness
import com.sultanagung1.sista.data.model.DynamicQrResponse
import com.sultanagung1.sista.ui.attendance.AttendanceUiState
import com.sultanagung1.sista.ui.attendance.QrDisplayState
import com.sultanagung1.sista.ui.attendance.dynamicQrDisplayState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The student's gate QR (DynamicQrScreen). It used to draw a QR *icon* plus
 * the first 16 characters of the token — nothing a scanner could read — and
 * its model expected `qr_payload` and a numeric `timestamp`, while the server
 * sends `qr_token` and an ISO-8601 string, so every response failed to parse.
 */
class DynamicQrTest {

    /** Exactly what GeofenceAttendanceService::generateDynamicTotpQr returns (no envelope). */
    private val backendJson = """
        {"success":true,
         "qr_token":"eyJ1Ijo0MiwiciI6InN0dWRlbnQiLCJ0cyI6NTk5MjEyMDAsInNpZyI6IjRmM2MyYjFhMGU5ZDhjN2IifQ==",
         "expires_in_seconds":18,
         "timestamp":"2026-10-05T07:10:12+07:00"}
    """.trimIndent()

    @Test
    fun parsesTheServersResponse() {
        val r = Gson().fromJson(backendJson, DynamicQrResponse::class.java)
        assertTrue(r.success)
        assertTrue(r.qrToken!!.startsWith("eyJ1Ijo0Mi"))
        assertEquals(18, r.expiresInSeconds)
        assertEquals("2026-10-05T07:10:12+07:00", r.timestamp)
    }

    @Test
    fun theDrawnQrCarriesTheExactTokenTheGateVerifies() {
        val token = Gson().fromJson(backendJson, DynamicQrResponse::class.java).qrToken!!
        val matrix = QrMatrixEncoder.encode(token)!!
        val cells = matrix.size + 2 * QrMatrixEncoder.QUIET_ZONE_MODULES
        val px = 4
        val side = cells * px
        val pixels = IntArray(side * side) { 0xFFFFFFFF.toInt() }
        for (y in 0 until matrix.size) for (x in 0 until matrix.size) if (matrix[x, y]) {
            for (dy in 0 until px) for (dx in 0 until px) {
                pixels[((y + QrMatrixEncoder.QUIET_ZONE_MODULES) * px + dy) * side + (x + QrMatrixEncoder.QUIET_ZONE_MODULES) * px + dx] = 0xFF000000.toInt()
            }
        }
        val decoded = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))).text
        assertEquals(token, decoded)
    }

    private fun state(token: String?, expiresAt: Long?, error: String? = null, loading: Boolean = false) = AttendanceUiState(
        isLoading = loading,
        dynamicQrResult = token?.let { DynamicQrResponse(success = true, qrToken = it, expiresInSeconds = 30) },
        dynamicQrExpiresAtMs = expiresAt,
        errorMessage = error
    )

    @Test
    fun displayFollowsTheServersExpiry() {
        assertEquals(QrDisplayState.Loading, dynamicQrDisplayState(state(null, null, loading = true), 0))
        assertEquals(QrDisplayState.Content("t", QrFreshness.FRESH), dynamicQrDisplayState(state("t", 10_000), 9_999))
        // A failed refresh keeps the last QR, dimmed, for a minute after it expired…
        assertEquals(QrDisplayState.Content("t", QrFreshness.STALE), dynamicQrDisplayState(state("t", 10_000, error = "Koneksi terputus."), 30_000))
        // …then stops showing a code that no longer works.
        assertTrue(dynamicQrDisplayState(state("t", 10_000, error = "Koneksi terputus."), 70_000) is QrDisplayState.Error)
        assertTrue(dynamicQrDisplayState(state("t", 10_000), 70_000) is QrDisplayState.Error)
        assertEquals(QrDisplayState.Error("Gagal memuat QR presensi dinamis"), dynamicQrDisplayState(state(null, null, error = "Gagal memuat QR presensi dinamis"), 0))
    }

    @Test
    fun screenDrawsARealQrAndNotTheToken() {
        val root = listOf(File(".."), File(".")).first { File(it, "settings.gradle").exists() }
        val src = File(root, "feature/academic/src/main/java/com/sultanagung1/sista/ui/attendance/DynamicQrScreen.kt").readText()
        assertTrue("draws the token as a QR", src.contains("SulaoneQrCode("))
        assertFalse("no QR icon standing in for the code", src.contains("Icons.Default.QrCode2"))
        assertFalse("the token is not printed as text", src.contains("qrToken.take(") || src.contains("qrPayload"))
        assertTrue("refreshes only while visible", src.contains("LifecycleStartStopEffect("))
        assertTrue("no screenshots of a live code", src.contains("SecureWindowEffect("))
        assertTrue("bright for the gate scanner", src.contains("KeepScreenAwake("))
    }
}
