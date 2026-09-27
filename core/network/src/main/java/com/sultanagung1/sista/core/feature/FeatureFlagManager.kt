package com.sultanagung1.sista.core.feature

import android.content.Context
import android.util.Log
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeatureFlagManager(
    private val context: Context,
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager
) {
    private val TAG = "FeatureFlagManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _flagsState = MutableStateFlow(FeatureFlagState())
    val flagsState: StateFlow<FeatureFlagState> = _flagsState.asStateFlow()

    init {
        fetchRemoteFlags()
    }

    fun isFeatureEnabled(key: FeatureFlagKey): Boolean {
        return _flagsState.value.flags[key.keyName] ?: key.defaultEnabled
    }

    fun setFeatureOverride(key: FeatureFlagKey, enabled: Boolean) {
        val updated = _flagsState.value.flags.toMutableMap()
        updated[key.keyName] = enabled
        _flagsState.value = FeatureFlagState(updated)
    }

    fun fetchRemoteFlags() {
        scope.launch {
            try {
                // Simulate or call backend remote config
                val currentFlags = FeatureFlagKey.values().associate { it.keyName to it.defaultEnabled }
                _flagsState.value = FeatureFlagState(currentFlags)
                Log.d(TAG, "Feature flags refreshed successfully: ${currentFlags.size} flags loaded.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh remote feature flags: ${e.message}")
            }
        }
    }
}
