package com.sultanagung1.sista.core.accessibility

import android.content.Context
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The languages the app (and the web) is offered in. [nativeName] is how the
 * language calls itself, so anyone can find theirs whatever is showing now.
 */
enum class AppLanguage(val code: String, val title: String, val nativeName: String, val layoutDirection: LayoutDirection) {
    INDONESIAN("id", "Bahasa Indonesia", "Bahasa Indonesia", LayoutDirection.Ltr),
    ENGLISH("en", "English", "English", LayoutDirection.Ltr),
    ARABIC("ar", "Bahasa Arab", "العربية", LayoutDirection.Rtl);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: INDONESIAN
        }
    }
}

/** The current language as a flow for screens; storing and applying it is [AppLocale]'s job. */
class LanguageManager(private val context: Context) {

    val currentLanguage: StateFlow<AppLanguage> = AppLocale.changes
        .map { it ?: AppLocale.current(context) }
        .stateIn(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate), SharingStarted.Eagerly, AppLocale.current(context))

    /** [byUser] = picked on this phone, still to be saved on the account. */
    fun setLanguage(language: AppLanguage, byUser: Boolean = true) {
        AppLocale.set(context, language, byUser)
    }

    fun isRtl(): Boolean = currentLanguage.value.layoutDirection == LayoutDirection.Rtl
}
