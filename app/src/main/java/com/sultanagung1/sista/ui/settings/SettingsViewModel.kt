package com.sultanagung1.sista.ui.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.sultanagung1.sista.core.accessibility.*
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val languageManager: LanguageManager,
    val fontScaleManager: FontScaleManager,
    val themeManager: ThemeManager
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = languageManager.currentLanguage
    val currentTheme: StateFlow<AppThemeMode> = themeManager.themeMode
    val fontScale: StateFlow<Float> = fontScaleManager.fontScale
    val isDyslexicFriendly: StateFlow<Boolean> = fontScaleManager.isDyslexicFriendly
    val isHighContrast: StateFlow<Boolean> = themeManager.isHighContrast

    fun setLanguage(lang: AppLanguage) {
        languageManager.setLanguage(lang)
    }

    fun setTheme(theme: AppThemeMode) {
        themeManager.setThemeMode(theme)
    }

    fun setFontScale(scale: Float) {
        fontScaleManager.setFontScale(scale)
    }

    fun setDyslexicFriendly(enabled: Boolean) {
        fontScaleManager.setDyslexicFriendly(enabled)
    }

    fun setHighContrast(enabled: Boolean) {
        themeManager.setHighContrast(enabled)
    }
}
