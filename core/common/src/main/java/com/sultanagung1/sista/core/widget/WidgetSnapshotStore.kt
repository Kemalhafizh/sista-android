package com.sultanagung1.sista.core.widget

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException

/** Re-renders the home-screen widgets. Implemented in :app, which owns the providers. */
fun interface WidgetRefresher {
    fun refresh()
}

/**
 * Persists the [WidgetSnapshot] the widgets render from.
 *
 * Repositories call [update] after a successful fetch, so the widgets reuse
 * data the app already loaded instead of making network calls in
 * `AppWidgetProvider.onUpdate`. SharedPreferences (not DataStore) because the
 * providers read it synchronously on the main thread.
 */
class WidgetSnapshotStore(
    context: Context,
    private val sessionManager: SessionManager,
    private val refresher: WidgetRefresher
) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): WidgetSnapshot? = read(prefs)

    /**
     * Applies [transform] to the current account's snapshot. A snapshot written
     * for another account (or another role, e.g. after the profile role
     * switcher) is discarded first. Never throws: a widget cache problem must
     * not fail the screen that triggered the fetch.
     */
    suspend fun update(transform: (WidgetSnapshot) -> WidgetSnapshot) {
        try {
            val userId = sessionManager.userIdFlow.first()
            val role = sessionManager.userRoleFlow.first()
            synchronized(this) {
                val current = read()?.takeIf { it.ownerUserId == userId && it.role == role }
                    ?: WidgetSnapshot(ownerUserId = userId, role = role)
                prefs.edit().putString(KEY_SNAPSHOT, gson.toJson(transform(current))).apply()
            }
            refresher.refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    /**
     * Login: drop everything and start an empty snapshot that already knows
     * the new account's role, so a teacher's widgets say "for student
     * accounts" instead of waiting for data that will never come.
     */
    suspend fun startNewSession() {
        update { WidgetSnapshot(ownerUserId = it.ownerUserId, role = it.role) }
    }

    /** Logout: the next account must never see the previous one's data. */
    fun clear() {
        try {
            synchronized(this) { prefs.edit().remove(KEY_SNAPSHOT).apply() }
            refresher.refresh()
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val PREFS_NAME = "sulaone_widget_snapshot"
        private const val KEY_SNAPSHOT = "snapshot_json"
        private val gson = Gson()

        /** For the widget providers, which are not Hilt-injected. */
        fun read(context: Context): WidgetSnapshot? =
            read(context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

        private fun read(prefs: SharedPreferences): WidgetSnapshot? = try {
            prefs.getString(KEY_SNAPSHOT, null)?.let { gson.fromJson(it, WidgetSnapshot::class.java) }
        } catch (_: Exception) {
            null
        }
    }
}
