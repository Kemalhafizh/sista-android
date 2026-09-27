package com.sultanagung1.sista.core.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.storage.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Has no UI of its own — its only job is to open/close the shared Reverb
 * WebSocket connection as the session's login state changes, since nothing
 * else in the app was calling ReverbWebSocketManager.connect()/disconnect(),
 * and to surface app-wide events (FASE 71.4 emergency alerts) that any role's
 * screen should react to regardless of which ViewModel is on screen.
 * Instantiate once via hiltViewModel() near the navigation root so it shares
 * the same Hilt-provided ReverbWebSocketManager singleton that feature
 * ViewModels (e.g. ChatViewModel, ParentViewModel) listen to.
 */
@HiltViewModel
class WebSocketSessionViewModel @Inject constructor(
    private val webSocketManager: ReverbWebSocketManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _emergencyAlert = MutableStateFlow<WebSocketEvent.EmergencyAlertTriggered?>(null)
    val emergencyAlert: StateFlow<WebSocketEvent.EmergencyAlertTriggered?> = _emergencyAlert.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isLoggedInFlow.collectLatest { isLoggedIn ->
                if (isLoggedIn) {
                    val userId = sessionManager.userIdFlow.first()
                    webSocketManager.connect(userId)
                } else {
                    webSocketManager.disconnect()
                }
            }
        }
        viewModelScope.launch {
            webSocketManager.events
                .filterIsInstance<WebSocketEvent.EmergencyAlertTriggered>()
                .collectLatest { _emergencyAlert.value = it }
        }
    }

    fun dismissEmergencyAlert() {
        _emergencyAlert.value = null
    }

    override fun onCleared() {
        webSocketManager.disconnect()
        super.onCleared()
    }
}
