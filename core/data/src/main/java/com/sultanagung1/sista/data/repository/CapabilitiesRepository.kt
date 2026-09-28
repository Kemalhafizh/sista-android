package com.sultanagung1.sista.data.repository

import com.google.gson.Gson
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.api.CapabilitiesApiService
import com.sultanagung1.sista.data.model.Capabilities
import com.sultanagung1.sista.data.model.CapabilityState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The one source of "what may this account use" for the whole app.
 *
 * [refresh] asks the server; when it cannot be reached the list saved last
 * time is used (marked [CapabilityState.Ready.fromCache]). A 401 means the
 * session is over. Nothing here grants access: the server judges every call,
 * the list only decides what the app offers.
 */
class CapabilitiesRepository(
    private val api: CapabilitiesApiService,
    private val sessionManager: SessionManager,
    private val gson: Gson = Gson(),
) {
    private val _state = MutableStateFlow<CapabilityState>(CapabilityState.Loading)
    val state: StateFlow<CapabilityState> = _state.asStateFlow()

    private val mutex = Mutex()

    suspend fun refresh(): CapabilityState = mutex.withLock {
        val next = withContext(Dispatchers.IO) { load() }
        _state.value = next
        next
    }

    /** Signed out: forget the list so the next account starts clean. */
    fun reset() {
        _state.value = CapabilityState.SignedOut
    }

    private suspend fun load(): CapabilityState {
        val fromServer = try {
            val response = api.getCapabilities()
            val body = response.body()?.data
            when {
                response.code() == 401 -> return CapabilityState.SignedOut
                response.isSuccessful && body != null -> body
                else -> null
            }
        } catch (_: Exception) {
            null
        }

        if (fromServer != null) {
            sessionManager.saveCapabilities(gson.toJson(fromServer), fromServer.role)
            return CapabilityState.Ready(fromServer)
        }

        val saved = sessionManager.readCapabilitiesJson()?.let { json ->
            runCatching { gson.fromJson(json, Capabilities::class.java) }.getOrNull()
        }
        return if (saved != null) {
            CapabilityState.Ready(saved, fromCache = true)
        } else {
            CapabilityState.Failed("Daftar fitur belum bisa dimuat. Periksa koneksi internet, lalu coba lagi.")
        }
    }
}
