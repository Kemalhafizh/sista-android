package com.sultanagung1.sista.core.designsystem

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * FASE 77.3: text → QR modules, with no Android or Compose in it so it is
 * unit-tested by decoding the result back. [SulaoneQrCode] draws it.
 *
 * The matrix has no quiet zone; the composable adds one around it, since a
 * QR needs a light margin to be found by a camera.
 */
class QrMatrix(val size: Int, private val modules: BooleanArray) {
    /** True for a dark module at column [x], row [y]. */
    operator fun get(x: Int, y: Int): Boolean = modules[y * size + x]
}

object QrMatrixEncoder {

    /** Modules of light margin to draw on each side (the QR standard asks for 4). */
    const val QUIET_ZONE_MODULES = 4

    /**
     * Medium error correction: survives some glare or a finger over a corner
     * on a projected or tilted phone screen, while keeping modules large.
     * Returns null for blank text or text too long for a QR code.
     */
    fun encode(text: String, level: ErrorCorrectionLevel = ErrorCorrectionLevel.M): QrMatrix? {
        if (text.isBlank()) return null
        val bits = try {
            QRCodeWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                0,
                0,
                mapOf(
                    EncodeHintType.ERROR_CORRECTION to level,
                    EncodeHintType.MARGIN to 0,
                    EncodeHintType.CHARACTER_SET to "UTF-8"
                )
            )
        } catch (e: WriterException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }
        val size = bits.width
        val modules = BooleanArray(size * size) { i -> bits[i % size, i / size] }
        return QrMatrix(size, modules)
    }
}
