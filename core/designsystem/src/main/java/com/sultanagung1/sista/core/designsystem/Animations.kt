package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

object SulaoneMotion {
    val SpringDefault: SpringSpec<Float> = spring(dampingRatio = 0.75f, stiffness = 300f)
    val SpringBouncy: SpringSpec<Float> = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
    val SpringGentle: SpringSpec<Float> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)

    fun staggeredDelay(index: Int, delay: Int = 50): Int = index * delay
}
