package com.sultanagung1.sista.core.accessibility

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * The app's language (id, en, ar), the same three the web offers.
 *
 * On Android 13+ the system keeps the choice per app (it also shows under
 * Settings > Apps > SISTA > Language) and recreates the screens itself. On
 * Android 8–12 the choice is kept here and applied by wrapping the activity's
 * context ([wrap]) and recreating it. Strings then come from
 * res/values (Indonesian), values-en and values-ar of each module, and Compose
 * lays the screen out right-to-left for Arabic on its own.
 *
 * [isPendingSync] marks a choice made on this phone that the server
 * (users.preferred_locale, also read by the web) has not stored yet.
 */
object AppLocale {

    private const val PREFS = "app_locale"
    private const val KEY_TAG = "tag"
    private const val KEY_PENDING = "pending_sync"

    private val _changes = MutableStateFlow<AppLanguage?>(null)

    /**
     * The language last set in this process. MainActivity watches it and, on
     * Android 8–12, recreates itself so the new strings and direction show.
     */
    val changes: StateFlow<AppLanguage?> = _changes.asStateFlow()

    fun current(context: Context): AppLanguage {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val system = context.getSystemService(LocaleManager::class.java)?.applicationLocales
            if (system != null && !system.isEmpty) return fromTag(system[0].toLanguageTag())
        }
        return prefs(context).getString(KEY_TAG, null)?.let(::fromTag) ?: AppLanguage.INDONESIAN
    }

    /**
     * Saves [language] and applies it. [byUser] = picked on this phone, so it
     * must still be sent to the server; false = it came from the server.
     * Returns true when the caller must recreate the activity (Android 8–12).
     */
    fun set(context: Context, language: AppLanguage, byUser: Boolean): Boolean {
        val changed = current(context) != language
        prefs(context).edit()
            .putString(KEY_TAG, language.code)
            .putBoolean(KEY_PENDING, byUser || isPendingSync(context))
            .apply()
        _changes.value = language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                LocaleList.forLanguageTags(localeOf(language).toLanguageTag())
            return false
        }
        return changed
    }

    /** What to do when the account's saved language ([server]) is known. */
    fun decide(pending: Boolean, current: AppLanguage, server: String?): SyncDecision = when {
        pending -> SyncDecision.Push(current)
        server.isNullOrBlank() -> SyncDecision.Nothing
        fromTag(server) != current -> SyncDecision.Adopt(fromTag(server))
        else -> SyncDecision.Nothing
    }

    /** Applies the saved language to an activity context on Android 8–12 (call from attachBaseContext). */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val locale = localeOf(current(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    fun isPendingSync(context: Context): Boolean = prefs(context).getBoolean(KEY_PENDING, false)

    fun markSynced(context: Context) {
        prefs(context).edit().putBoolean(KEY_PENDING, false).apply()
    }

    /** The value for the Accept-Language header, e.g. "ar". */
    fun headerValue(context: Context): String = current(context).code

    fun fromTag(tag: String): AppLanguage = AppLanguage.fromCode(tag.substringBefore('-').substringBefore('_').let { if (it == "in") "id" else it })

    /**
     * The locale the app runs in. Arabic keeps Latin digits ("ar-u-nu-latn"),
     * like the web, so every formatted number ("%d", dates) reads 18, not ١٨.
     * Resources still resolve from values-ar.
     */
    fun localeOf(language: AppLanguage): Locale =
        if (language == AppLanguage.ARABIC) Locale.forLanguageTag("ar-u-nu-latn") else Locale.forLanguageTag(language.code)

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/**
 * Reconciling this phone with the account: a choice made here and not yet
 * saved wins and is sent ([Push]); otherwise the account's choice (made on
 * the web, or on another phone) is applied here ([Adopt]).
 */
sealed interface SyncDecision {
    data class Push(val language: AppLanguage) : SyncDecision
    data class Adopt(val language: AppLanguage) : SyncDecision
    data object Nothing : SyncDecision
}
