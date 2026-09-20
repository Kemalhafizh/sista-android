package com.sultanagung1.sista.core.designsystem

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class SulaoneHaptics(private val view: View) {
    fun light() = view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    fun medium() = view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    fun heavy() = view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    fun success() = view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    fun error() = view.performHapticFeedback(HapticFeedbackConstants.REJECT)
}

@Composable
fun rememberSulaoneHaptics(): SulaoneHaptics {
    val view = LocalView.current
    return remember(view) { SulaoneHaptics(view) }
}
