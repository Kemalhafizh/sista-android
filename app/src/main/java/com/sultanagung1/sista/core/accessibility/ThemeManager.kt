package com.sultanagung1.sista.core.accessibility

import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppThemeMode(val title: String, val description: String) {
    SYSTEM("Sistem Bawaan", "Mengikuti setelan tema perangkat HP"),
    LIGHT("Terang (Emerald)", "Warna hijau zamrud bersih & cerah"),
    DARK("Gelap (Islami)", "Warna gelap nyaman untuk malam hari"),
    AMOLED_BLACK("AMOLED Murni", "Hitam pekat murni (Super hemat baterai)"),
    HIGH_CONTRAST("Kontras Tinggi", "Garis tebal & kontras maksimal ramah disabilitas");

    companion object {
        fun fromString(name: String): AppThemeMode {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}

class ThemeManager(
    private val sessionManager: SessionManager? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    init {
        sessionManager?.let { sm ->
            scope.launch {
                sm.appThemeFlow.collect { savedTheme ->
                    _themeMode.value = AppThemeMode.fromString(savedTheme)
                }
            }
            scope.launch {
                sm.isHighContrastFlow.collect { savedHighContrast ->
                    _isHighContrast.value = savedHighContrast
                }
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        val newHighContrast = (mode == AppThemeMode.HIGH_CONTRAST)
        _isHighContrast.value = newHighContrast
        sessionManager?.let { sm ->
            scope.launch(Dispatchers.IO) {
                sm.saveThemePreference(mode.name)
                sm.saveHighContrastPreference(newHighContrast)
            }
        }
    }

    fun toggleDarkLight(isCurrentlyDark: Boolean) {
        val nextMode = if (isCurrentlyDark) AppThemeMode.LIGHT else AppThemeMode.DARK
        setThemeMode(nextMode)
    }

    fun setHighContrast(enabled: Boolean) {
        _isHighContrast.value = enabled
        if (enabled) {
            _themeMode.value = AppThemeMode.HIGH_CONTRAST
        } else if (_themeMode.value == AppThemeMode.HIGH_CONTRAST) {
            _themeMode.value = AppThemeMode.SYSTEM
        }
        sessionManager?.let { sm ->
            scope.launch(Dispatchers.IO) {
                sm.saveHighContrastPreference(enabled)
                sm.saveThemePreference(_themeMode.value.name)
            }
        }
    }
}
