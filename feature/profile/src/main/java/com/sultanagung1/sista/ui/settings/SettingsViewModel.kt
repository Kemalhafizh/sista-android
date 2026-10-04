package com.sultanagung1.sista.ui.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.data.repository.LocaleSync
import kotlinx.coroutines.launch
import com.sultanagung1.sista.core.accessibility.*
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val languageManager: LanguageManager,
    val fontScaleManager: FontScaleManager,
    val themeManager: ThemeManager,
    private val localeSync: LocaleSync,
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = languageManager.currentLanguage
    val currentTheme: StateFlow<AppThemeMode> = themeManager.themeMode
    val fontScale: StateFlow<Float> = fontScaleManager.fontScale
    val isDyslexicFriendly: StateFlow<Boolean> = fontScaleManager.isDyslexicFriendly
    val isHighContrast: StateFlow<Boolean> = themeManager.isHighContrast

    /** Applies [lang] on this phone and saves it on the account (kept pending and retried if offline). */
    fun setLanguage(lang: AppLanguage) {
        if (lang == currentLanguage.value) return
        languageManager.setLanguage(lang)
        viewModelScope.launch { localeSync.push(lang.code) }
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
