package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * FASE 77.3: a real, scannable QR code for [payload].
 *
 * Always black on white, whatever the app theme: a camera reads dark modules
 * on a light background, and dark mode or AMOLED black must not invert it.
 * The quiet zone is part of the drawn square.
 */
@Composable
fun SulaoneQrCode(
    payload: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Kode QR",
    dimmed: Boolean = false
) {
    val matrix = remember(payload) { QrMatrixEncoder.encode(payload) }
    val moduleColor = if (dimmed) Color.Black.copy(alpha = 0.25f) else Color.Black
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .semantics { this.contentDescription = contentDescription }
    ) {
        if (matrix != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cells = matrix.size + 2 * QrMatrixEncoder.QUIET_ZONE_MODULES
                val cell = size.minDimension / cells
                val origin = QrMatrixEncoder.QUIET_ZONE_MODULES * cell
                // Draw slightly oversized cells so adjacent modules do not show hairline gaps.
                val drawn = Size(cell + 0.5f, cell + 0.5f)
                for (y in 0 until matrix.size) {
                    for (x in 0 until matrix.size) {
                        if (matrix[x, y]) {
                            drawRect(moduleColor, topLeft = Offset(origin + x * cell, origin + y * cell), size = drawn)
                        }
                    }
                }
            }
        }
    }
}
