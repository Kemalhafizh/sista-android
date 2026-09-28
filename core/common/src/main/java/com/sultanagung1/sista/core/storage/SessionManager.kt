package com.sultanagung1.sista.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sulaone_session_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_IDENTIFIER = stringPreferencesKey("user_identifier") // NISN or NIP
        val KEY_USER_CLASSROOM = stringPreferencesKey("user_classroom")
        val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_SENSITIVE_PROTECTION_ENABLED = booleanPreferencesKey("sensitive_protection_enabled")
        val KEY_REMEMBERED_IDENTIFIER = stringPreferencesKey("remembered_identifier")
        val KEY_REMEMBERED_USER_ID = stringPreferencesKey("remembered_user_id")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
        val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        val KEY_DYSLEXIC_MODE = booleanPreferencesKey("dyslexic_mode")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val KEY_REFRESH_RATE_MODE = stringPreferencesKey("refresh_rate_mode")
        val KEY_FAVORITE_MODULE_IDS = stringSetPreferencesKey("favorite_module_ids")
        // Last capability list the server sent (JSON), for offline starts.
        val KEY_CAPABILITIES = stringPreferencesKey("capabilities_json")
    }

    val authTokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_AUTH_TOKEN] }

    val userIdFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_USER_ID] }

    val userRoleFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        // FASE 76.3: backend aliases (orang_tua, kepala_sekolah, siswa) folded
        // into the app's role vocabulary for the screens that still read it.
        // What an account may open comes from the server (CapabilitiesRepository).
        .map { preferences -> com.sultanagung1.sista.ui.navigation.UserRoles.normalize(preferences[KEY_USER_ROLE]) }

    val userNameFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_USER_NAME] }

    val userIdentifierFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_USER_IDENTIFIER] }

    val userClassroomFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_USER_CLASSROOM] }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_IS_LOGGED_IN] ?: false }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_BIOMETRIC_ENABLED] ?: false }

    val isSensitiveProtectionEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_SENSITIVE_PROTECTION_ENABLED] ?: true }

    val rememberedIdentifierFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_REMEMBERED_IDENTIFIER] }

    /** Survives logout (unlike [userIdFlow]) so biometric quick-login keeps working after sign-out. */
    val rememberedUserIdFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_REMEMBERED_USER_ID] }

    val appLanguageFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_APP_LANGUAGE] ?: "id" }

    val appThemeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_APP_THEME] ?: "SYSTEM" }

    val fontScaleFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_FONT_SCALE] ?: 1.0f }

    val isDyslexicModeFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_DYSLEXIC_MODE] ?: false }

    val isHighContrastFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_HIGH_CONTRAST] ?: false }

    suspend fun saveAuthSession(
        token: String,
        role: String,
        name: String,
        email: String,
        identifier: String,
        userId: String? = null,
        classroom: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
            preferences[KEY_USER_ROLE] = role
            preferences[KEY_USER_NAME] = name
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_IDENTIFIER] = identifier
            preferences[KEY_REMEMBERED_IDENTIFIER] = identifier
            preferences[KEY_IS_LOGGED_IN] = true
            if (!userId.isNullOrBlank()) {
                preferences[KEY_USER_ID] = userId
                preferences[KEY_REMEMBERED_USER_ID] = userId
            }
            if (!classroom.isNullOrBlank()) {
                preferences[KEY_USER_CLASSROOM] = classroom
            }
        }
    }

    /**
     * Stable per-install identifier used for biometric device registration
     * (backend correlates it with a registered public key). Generated once
     * and persisted — not derived from ANDROID_ID to avoid cross-app tracking.
     */
    suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data.first()[KEY_DEVICE_ID]
        if (!existing.isNullOrBlank()) return existing
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { preferences ->
            preferences[KEY_DEVICE_ID] = generated
        }
        return generated
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setSensitiveProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SENSITIVE_PROTECTION_ENABLED] = enabled
        }
    }

    suspend fun saveRememberedIdentifier(identifier: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMEMBERED_IDENTIFIER] = identifier
        }
    }

    suspend fun saveLanguagePreference(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = languageCode
        }
    }

    suspend fun saveThemePreference(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = themeMode
        }
    }

    suspend fun saveFontScalePreference(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_SCALE] = scale
        }
    }

    suspend fun saveDyslexicModePreference(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DYSLEXIC_MODE] = enabled
        }
    }

    suspend fun saveHighContrastPreference(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIGH_CONTRAST] = enabled
        }
    }

    val refreshRateModeFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_REFRESH_RATE_MODE] }

    suspend fun saveRefreshRateMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REFRESH_RATE_MODE] = mode
        }
    }

    val favoriteModuleIdsFlow: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_FAVORITE_MODULE_IDS] ?: emptySet() }

    suspend fun toggleFavoriteModule(moduleId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_FAVORITE_MODULE_IDS] ?: emptySet()
            preferences[KEY_FAVORITE_MODULE_IDS] = if (moduleId in current) current - moduleId else current + moduleId
        }
    }

    /** The saved capability list, or null (never saved / signed out). */
    suspend fun readCapabilitiesJson(): String? = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .first()[KEY_CAPABILITIES]

    /**
     * Save what the server said this account may use, and the role it
     * reported with it: the server's role replaces whatever was stored at
     * login, so the app never runs on a stale or guessed role.
     */
    suspend fun saveCapabilities(json: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAPABILITIES] = json
            if (role.isNotBlank()) preferences[KEY_USER_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_CAPABILITIES)
            preferences.remove(KEY_AUTH_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_ROLE)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_IDENTIFIER)
            preferences[KEY_IS_LOGGED_IN] = false
        }
    }
}
