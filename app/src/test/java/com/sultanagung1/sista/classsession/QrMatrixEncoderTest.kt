package com.sultanagung1.sista.classsession

import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.sultanagung1.sista.core.designsystem.QrMatrix
import com.sultanagung1.sista.core.designsystem.QrMatrixEncoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 77.3: the teacher's QR must actually scan. The docs used to say
 * DynamicQrScreen already drew one with ZXing; it drew an icon, and ZXing was
 * not a dependency. This renders the matrix the way SulaoneQrCode does (with
 * the quiet zone, a few pixels per module) and reads it back with a real
 * decoder.
 */
class QrMatrixEncoderTest {

    private fun render(matrix: QrMatrix, pixelsPerModule: Int = 4): BinaryBitmap {
        val cells = matrix.size + 2 * QrMatrixEncoder.QUIET_ZONE_MODULES
        val side = cells * pixelsPerModule
        val pixels = IntArray(side * side) { 0xFFFFFFFF.toInt() }
        for (y in 0 until matrix.size) for (x in 0 until matrix.size) {
            if (!matrix[x, y]) continue
            val px0 = (x + QrMatrixEncoder.QUIET_ZONE_MODULES) * pixelsPerModule
            val py0 = (y + QrMatrixEncoder.QUIET_ZONE_MODULES) * pixelsPerModule
            for (py in py0 until py0 + pixelsPerModule) for (px in px0 until px0 + pixelsPerModule) {
                pixels[py * side + px] = 0xFF000000.toInt()
            }
        }
        return BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
    }

    private fun roundTrip(text: String): String {
        val matrix = QrMatrixEncoder.encode(text)!!
        return QRCodeReader().decode(render(matrix)).text
    }

    @Test
    fun classSessionPayloadScansBack() {
        val payload = "sista-cs:v1:9f1c2b7e-4a1d-4f55-9c3e-2f7b8e1a0c11:Zk3xQ9pLm2Rt7VwY8bN4cD6fH1jK5sA0"
        assertEquals(payload, roundTrip(payload))
    }

    @Test
    fun nonAsciiTextSurvives() {
        assertEquals("Kelas X-1 — Matematika ✓", roundTrip("Kelas X-1 — Matematika ✓"))
    }

    @Test
    fun matrixIsSquareAndHasFinderPatterns() {
        val m = QrMatrixEncoder.encode("sista-cs:v1:a:b")!!
        assertTrue("QR versions are 21..177 modules", m.size in 21..177)
        // Top-left finder pattern: dark outer ring, light ring, dark centre.
        assertTrue(m[0, 0] && m[6, 0] && m[0, 6] && m[6, 6])
        assertTrue(!m[1, 1] && !m[5, 5])
        assertTrue(m[3, 3])
    }

    @Test
    fun blankOrOversizedTextGivesNoQr() {
        assertNull(QrMatrixEncoder.encode(""))
        assertNull(QrMatrixEncoder.encode("   "))
        assertNull(QrMatrixEncoder.encode("x".repeat(5_000)))
    }
}
