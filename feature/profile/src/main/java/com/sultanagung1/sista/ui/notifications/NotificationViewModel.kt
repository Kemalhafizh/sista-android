package com.sultanagung1.sista.ui.notifications

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.notification.NotificationChannelManager
import com.sultanagung1.sista.data.model.NotificationChannelType
import com.sultanagung1.sista.data.model.NotificationItem
import com.sultanagung1.sista.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val selectedFilter: String = "Semua",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val testDispatchMessage: String? = null,
    val preferences: com.sultanagung1.sista.data.model.NotificationPreferences = com.sultanagung1.sista.data.model.NotificationPreferences()
)


@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val channelManager: NotificationChannelManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        loadPreferences()
    }

    fun loadPreferences() {
        viewModelScope.launch {
            try {
                notificationRepository.getNotificationPreferences().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.update { it.copy(preferences = result.data) }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun updatePreferences(newPref: com.sultanagung1.sista.data.model.NotificationPreferences) {
        _uiState.update { it.copy(preferences = newPref) }
        viewModelScope.launch {
            try {
                notificationRepository.updateNotificationPreferences(newPref).collect {}
            } catch (_: Exception) {}
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getNotifications().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(notifications = result.data, isLoading = false, errorMessage = null)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun markAsRead(notificationId: String) {
        _uiState.update { state ->
            val updated = state.notifications.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            state.copy(notifications = updated)
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(notifications = updated)
        }
    }

    fun deleteNotification(notificationId: String) {
        _uiState.update { state ->
            val updated = state.notifications.filter { it.id != notificationId }
            state.copy(notifications = updated)
        }
    }

    fun dispatchTestPushNotification(
        channelType: NotificationChannelType,
        title: String,
        body: String,
        deepLinkRoute: String? = null
    ) {
        channelManager?.dispatchLocalNotification(
            notificationId = (System.currentTimeMillis() % 100000).toInt(),
            channelType = channelType,
            title = title,
            message = body,
            deepLinkRoute = deepLinkRoute
        )

        // Also append to in-app list
        val newNotif = NotificationItem(
            id = "test_${System.currentTimeMillis()}",
            title = title,
            body = body,
            channel = channelType.channelId,
            deepLinkRoute = deepLinkRoute,
            timestamp = "Baru saja",
            isRead = false
        )

        _uiState.update { state ->
            state.copy(
                notifications = listOf(newNotif) + state.notifications,
                testDispatchMessage = "Notifikasi '$title' berhasil dikirim ke Notification Shade Android!"
            )
        }
    }

    fun clearTestDispatchMessage() {
        _uiState.update { it.copy(testDispatchMessage = null) }
    }
}
