package com.sultanagung1.sista.ui.navigation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.CapabilityState
import com.sultanagung1.sista.data.repository.CapabilitiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Keeps the capability list in step with the session: loaded when an
 * account signs in, refreshed when the app comes back to the foreground (a
 * role changed by the school takes effect without reinstalling), and dropped
 * on sign-out.
 */
@HiltViewModel
class CapabilitiesViewModel @Inject constructor(
    private val repository: CapabilitiesRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    val state: StateFlow<CapabilityState> = repository.state

    private var lastRefresh = 0L

    init {
        viewModelScope.launch {
            sessionManager.isLoggedInFlow.distinctUntilChanged().collect { loggedIn ->
                if (loggedIn) refreshNow() else repository.reset()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    suspend fun refreshNow(): CapabilityState {
        lastRefresh = SystemClock.elapsedRealtime()
        return repository.refresh()
    }

    /** Back from the background: refresh, at most once a minute. */
    fun onForeground() {
        if (state.value == CapabilityState.SignedOut) return
        if (SystemClock.elapsedRealtime() - lastRefresh < FOREGROUND_REFRESH_MS) return
        refresh()
    }

    private companion object {
        const val FOREGROUND_REFRESH_MS = 60_000L
    }
}
