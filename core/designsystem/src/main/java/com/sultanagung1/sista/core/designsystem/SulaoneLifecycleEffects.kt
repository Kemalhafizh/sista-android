package com.sultanagung1.sista.core.designsystem

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Runs [onStart] when the screen becomes visible and [onStop] when it leaves
 * the foreground (app in background, screen off, another screen on top).
 * Screens use it to start/stop their polling so a class-session list does not
 * keep hitting the server from a pocket. If the screen is already started when
 * this enters composition, [onStart] runs straight away.
 */
@Composable
fun LifecycleStartStopEffect(onStart: () -> Unit, onStop: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart()
                Lifecycle.Event.ON_STOP -> currentOnStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentOnStop()
        }
    }
}

/**
 * FASE 77.3.5: while [enabled], the screen does not sleep and, with
 * [maxBrightness], runs at full brightness so students can scan the QR from
 * the back of the class. Both are undone when the screen leaves.
 */
@Composable
fun KeepScreenAwake(enabled: Boolean, maxBrightness: Boolean = false) {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    DisposableEffect(enabled, maxBrightness, view, activity) {
        if (!enabled) return@DisposableEffect onDispose { }
        view.keepScreenOn = true
        val window = activity?.window
        val previousBrightness = window?.attributes?.screenBrightness
        if (maxBrightness && window != null) {
            window.attributes = window.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL }
        }
        onDispose {
            view.keepScreenOn = false
            if (maxBrightness && window != null && previousBrightness != null) {
                window.attributes = window.attributes.apply { screenBrightness = previousBrightness }
            }
        }
    }
}

/**
 * Blocks screenshots, screen recording and casting of this window while
 * [enabled] (FLAG_SECURE), and lifts it when the screen leaves — for codes a
 * student must not be able to forward to a friend.
 */
@Composable
fun SecureWindowEffect(enabled: Boolean = true) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(enabled, activity) {
        val window = activity?.window
        if (!enabled || window == null) return@DisposableEffect onDispose { }
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
