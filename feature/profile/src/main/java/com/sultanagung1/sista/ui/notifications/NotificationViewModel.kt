package com.sultanagung1.sista.ui.notifications

import com.sultanagung1.sista.feature.profile.R
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.NotificationItem
import com.sultanagung1.sista.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter(@StringRes val label: Int) { All(R.string.notif_filter_all), Unread(R.string.notif_filter_unread) }

data class NotificationUiState(
    val isLoading: Boolean = false,
    /** Null until the first load; empty when there are none. */
    val notifications: List<NotificationItem>? = null,
    val errorMessage: String? = null,
    val filter: NotificationFilter = NotificationFilter.All,
    /** A read/delete the server refused; shown once, then cleared. */
    val actionMessage: String? = null,
) {
    val unreadCount: Int get() = notifications?.count { !it.isRead } ?: 0
    val visible: List<NotificationItem> get() = notifications.orEmpty().filter { filter == NotificationFilter.All || !it.isRead }
}

/**
 * This account's notifications. Reading, reading all and deleting are saved
 * on the server; the list changes at once and goes back if the server says no.
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            notificationRepository.getNotifications().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, notifications = result.data, errorMessage = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun setFilter(filter: NotificationFilter) = _uiState.update { it.copy(filter = filter) }

    fun markRead(id: String) {
        val item = _uiState.value.notifications?.firstOrNull { it.id == id } ?: return
        if (item.isRead) return
        setRead(id, true)
        viewModelScope.launch {
            notificationRepository.markNotificationRead(id).collect { result ->
                if (result is NetworkResult.Error) {
                    setRead(id, false)
                    _uiState.update { it.copy(actionMessage = result.message) }
                }
            }
        }
    }

    fun markAllRead() {
        val before = _uiState.value.notifications ?: return
        if (before.none { !it.isRead }) return
        _uiState.update { it.copy(notifications = before.map { n -> n.copy(isRead = true) }) }
        viewModelScope.launch {
            notificationRepository.markAllNotificationsRead().collect { result ->
                if (result is NetworkResult.Error) {
                    _uiState.update { it.copy(notifications = before, actionMessage = result.message) }
                }
            }
        }
    }

    fun delete(id: String) {
        val before = _uiState.value.notifications ?: return
        if (before.none { it.id == id }) return
        _uiState.update { it.copy(notifications = before.filterNot { n -> n.id == id }) }
        viewModelScope.launch {
            notificationRepository.deleteNotification(id).collect { result ->
                if (result is NetworkResult.Error) {
                    _uiState.update { it.copy(notifications = before, actionMessage = result.message) }
                }
            }
        }
    }

    fun dismissActionMessage() = _uiState.update { it.copy(actionMessage = null) }

    private fun setRead(id: String, read: Boolean) = _uiState.update { state ->
        state.copy(notifications = state.notifications?.map { if (it.id == id) it.copy(isRead = read) else it })
    }
}
