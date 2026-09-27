package com.sultanagung1.sista.core.accessibility

import androidx.compose.ui.unit.LayoutDirection
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppLanguage(val code: String, val title: String, val nativeName: String, val layoutDirection: LayoutDirection) {
    INDONESIAN("id", "Bahasa Indonesia", "Bahasa Indonesia", LayoutDirection.Ltr),
    ENGLISH("en", "English", "English (US)", LayoutDirection.Ltr),
    ARABIC("ar", "Bahasa Arab", "العربية (RTL)", LayoutDirection.Rtl);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: INDONESIAN
        }
    }
}

class LanguageManager(
    private val sessionManager: SessionManager? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _currentLanguage = MutableStateFlow(AppLanguage.INDONESIAN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    init {
        sessionManager?.let { sm ->
            scope.launch {
                val savedCode = sm.appLanguageFlow.first()
                _currentLanguage.value = AppLanguage.fromCode(savedCode)
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        sessionManager?.let { sm ->
            scope.launch(Dispatchers.IO) {
                sm.saveLanguagePreference(language.code)
            }
        }
    }

    fun isRtl(): Boolean = _currentLanguage.value.layoutDirection == LayoutDirection.Rtl
}
