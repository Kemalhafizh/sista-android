package com.sultanagung1.sista.data.repository

import android.content.Context
import com.sultanagung1.sista.core.accessibility.AppLocale
import com.sultanagung1.sista.core.accessibility.SyncDecision
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.data.model.LocaleRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Keeps the app's language and the account's (users.preferred_locale, which
 * the web reads) the same. Called after signing in, when the profile loads
 * and when the language is changed in the app.
 */
class LocaleSync(private val apiClient: ApiClient, private val context: Context) {

    /** [serverLocale] = the account's language from login or GET me. */
    suspend fun reconcile(serverLocale: String?) {
        val decision = AppLocale.decide(AppLocale.isPendingSync(context), AppLocale.current(context), serverLocale)
        when (decision) {
            is SyncDecision.Push -> push(decision.language.code)
            is SyncDecision.Adopt -> withContext(Dispatchers.Main) { AppLocale.set(context, decision.language, byUser = false) }
            SyncDecision.Nothing -> Unit
        }
    }

    /** Sends the language picked on this phone; it stays pending (and is sent later) when that fails. */
    suspend fun push(code: String = AppLocale.current(context).code): Boolean = withContext(Dispatchers.IO) {
        val saved = try {
            apiClient.authApi.updateLocale(LocaleRequest(code)).isSuccessful
        } catch (_: Exception) {
            false
        }
        if (saved) AppLocale.markSynced(context)
        saved
    }
}
