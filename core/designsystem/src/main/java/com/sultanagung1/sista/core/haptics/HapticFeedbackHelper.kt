package com.sultanagung1.sista.core.haptics

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class HapticFeedbackHelper(private val view: View) {
    fun tapLight() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun tapMedium() {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun tapHeavy() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun success() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun errorWarning() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}

@Composable
fun rememberHapticFeedbackHelper(): HapticFeedbackHelper {
    val view = LocalView.current
    return remember(view) { HapticFeedbackHelper(view) }
}
