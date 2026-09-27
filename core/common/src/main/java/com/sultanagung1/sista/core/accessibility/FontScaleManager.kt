package com.sultanagung1.sista.core.accessibility

import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FontScaleManager(
    private val sessionManager: SessionManager? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _fontScale = MutableStateFlow(1.0f) // 0.75f, 1.0f, 1.25f, 1.5f, 2.0f
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _isDyslexicFriendly = MutableStateFlow(false)
    val isDyslexicFriendly: StateFlow<Boolean> = _isDyslexicFriendly.asStateFlow()

    init {
        sessionManager?.let { sm ->
            scope.launch {
                val savedScale = sm.fontScaleFlow.first()
                val savedDyslexic = sm.isDyslexicModeFlow.first()
                _fontScale.value = savedScale
                _isDyslexicFriendly.value = savedDyslexic
            }
        }
    }

    fun setFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.75f, 2.0f)
        _fontScale.value = clamped
        sessionManager?.let { sm ->
            scope.launch(Dispatchers.IO) {
                sm.saveFontScalePreference(clamped)
            }
        }
    }

    fun setDyslexicFriendly(enabled: Boolean) {
        _isDyslexicFriendly.value = enabled
        sessionManager?.let { sm ->
            scope.launch(Dispatchers.IO) {
                sm.saveDyslexicModePreference(enabled)
            }
        }
    }

    companion object {
        const val MIN_FONT_SCALE = 0.75f
        const val MAX_APP_FONT_SCALE = 2.0f
        const val MAX_EFFECTIVE_FONT_SCALE = 2.5f

        /**
         * Calculates effective font scale with WCAG safety net capping at 2.5f.
         * Prevents UI breaking/text clipping on devices with both huge OS font scale and app font scale.
         */
        fun calculateEffectiveFontScale(systemFontScale: Float, appFontScale: Float): Float {
            val clampedAppScale = appFontScale.coerceIn(MIN_FONT_SCALE, MAX_APP_FONT_SCALE)
            return minOf(systemFontScale * clampedAppScale, MAX_EFFECTIVE_FONT_SCALE)
        }
    }
}

