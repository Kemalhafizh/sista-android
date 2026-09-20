package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speedY: Float,
    val speedX: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    colors: List<Color> = listOf(Gold500, Emerald600, AccentPurple, AccentCyan, AccentRose),
    onFinished: (() -> Unit)? = null
) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.2f,
                size = Random.nextFloat() * 12f + 6f,
                color = colors.random(),
                speedY = Random.nextFloat() * 0.8f + 0.4f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(2500))
        onFinished?.invoke()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val pVal = progress.value
        particles.forEach { p ->
            val currentX = (p.x + p.speedX * pVal) * size.width
            val currentY = (p.y + p.speedY * pVal) * size.height
            val alpha = (1f - pVal).coerceIn(0f, 1f)

            drawRect(
                color = p.color.copy(alpha = alpha),
                topLeft = Offset(currentX, currentY),
                size = Size(p.size, p.size)
            )
        }
    }
}
