@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.sultanagung1.sista.core.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Modifier helper to seamlessly apply [SharedTransitionScope.sharedElement]
 * when both [LocalSharedTransitionScope] and [LocalNavAnimatedVisibilityScope] are active.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sulaoneSharedElement(
    key: Any,
    sharedTransitionScope: SharedTransitionScope? = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope? = LocalNavAnimatedVisibilityScope.current,
    zIndexInOverlay: Float = 0f
): Modifier {
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@sulaoneSharedElement.sharedElement(
                state = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = zIndexInOverlay
            )
        }
    } else {
        this
    }
}

/**
 * Modifier helper to seamlessly apply [SharedTransitionScope.sharedBounds]
 * for container transforms (e.g. Cards, Panels, Bento blocks).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sulaoneSharedBounds(
    key: Any,
    sharedTransitionScope: SharedTransitionScope? = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope? = LocalNavAnimatedVisibilityScope.current,
    zIndexInOverlay: Float = 0f
): Modifier {
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@sulaoneSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = zIndexInOverlay
            )
        }
    } else {
        this
    }
}

object SulaoneNavTransitions {
    val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(220))
    }

    val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(220))
    }

    val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(220))
    }

    val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(220))
    }

    /**
     * Crossfade transition for Bottom Navigation tab switching (FASE 53.4 / 54.2).
     * Instantaneous and smooth without jarring horizontal slides.
     */
    val tabEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(durationMillis = 150, easing = LinearEasing))
    }

    val tabExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(durationMillis = 150, easing = LinearEasing))
    }

    /**
     * Sub-page navigation entry transition with smooth slide and morph (FASE 54.2).
     */
    val subPageEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(220))
    }

    val subPageExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(200))
    }

    /**
     * Modal and Dialog entry with centered scale up and smooth fade (FASE 54.2).
     */
    val modalEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(220))
    }

    val modalExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.85f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(200))
    }

    /**
     * Predictive back gesture preview with subtle shrink animation (Android 14+ / FASE 54.2).
     */
    val predictiveBackPopEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(220))
    }

    val predictiveBackPopExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(220))
    }
}

object MotionSpecs {
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SpringGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
}

enum class ButtonState { Pressed, Idle }

fun Modifier.springPressable(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(
        targetValue = if (buttonState == ButtonState.Pressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "spring_press_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}
