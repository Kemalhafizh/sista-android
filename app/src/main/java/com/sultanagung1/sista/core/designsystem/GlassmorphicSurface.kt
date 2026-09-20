package com.sultanagung1.sista.core.designsystem

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp, // Preserved for API backward compatibility
    tintColor: Color = MaterialTheme.colorScheme.surface,
    tintAlpha: Float = 0.82f,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    elevation: Dp = 3.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = tintColor.copy(alpha = tintAlpha),
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = elevation,
        content = content
    )
}
