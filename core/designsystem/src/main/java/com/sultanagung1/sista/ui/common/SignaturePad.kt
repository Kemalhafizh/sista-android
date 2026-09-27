package com.sultanagung1.sista.ui.common

import android.graphics.Bitmap
import android.util.Base64
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import java.io.ByteArrayOutputStream

data class SignatureStroke(
    val path: Path,
    val color: Color = Color.Black,
    val strokeWidth: Float = 6f
)

/**
 * A real touch-drawable signature pad — used by SignatureScreen and by
 * DisciplineScreen's warning-letter sign dialog, both of which previously
 * either sent a hardcoded mock string as the "signature" regardless of what
 * (if anything) the user drew, or (DisciplineScreen) didn't even offer a
 * drawable area at all, just a decorative placeholder box. [strokes] and
 * [canvasSizePx] are hoisted so the caller can pass them to
 * [captureSignatureAsBase64Png] once the user confirms.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignaturePad(
    strokes: SnapshotStateList<SignatureStroke>,
    onCanvasSized: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.Black
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged(onCanvasSized)
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentPath = Path().apply { moveTo(motionEvent.x, motionEvent.y) }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentPath?.lineTo(motionEvent.x, motionEvent.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        currentPath?.let { strokes.add(SignatureStroke(path = it, color = strokeColor)) }
                        currentPath = null
                    }
                }
                true
            }
    ) {
        strokes.forEach { stroke ->
            drawPath(
                path = stroke.path,
                color = stroke.color,
                style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        currentPath?.let { p ->
            drawPath(
                path = p,
                color = strokeColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Renders the drawn strokes onto a real bitmap and returns it as a base64
 * PNG — this is what actually gets sent as `signature_data`, instead of a
 * hardcoded placeholder string.
 */
fun captureSignatureAsBase64Png(strokes: List<SignatureStroke>, sizePx: IntSize): String? {
    if (strokes.isEmpty() || sizePx.width <= 0 || sizePx.height <= 0) return null

    val bitmap = Bitmap.createBitmap(sizePx.width, sizePx.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        color = android.graphics.Color.BLACK
        strokeWidth = 6f
    }

    strokes.forEach { stroke ->
        canvas.drawPath(stroke.path.asAndroidPath(), paint)
    }

    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    bitmap.recycle()
    return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}
