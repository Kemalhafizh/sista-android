package com.sultanagung1.sista.ui.announcements

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.AnnouncementItem
import com.sultanagung1.sista.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnnouncementUiState(
    val announcements: List<AnnouncementItem> = emptyList(),
    val selectedAnnouncement: AnnouncementItem? = null,
    val selectedCategory: String = "Semua",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val liveAlertBanner: String? = null
)


@HiltViewModel
class AnnouncementViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val webSocketManager: ReverbWebSocketManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementUiState())
    val uiState: StateFlow<AnnouncementUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
        listenToWebSocketBroadcasts()
    }

    fun loadAnnouncements(category: String? = null) {
        viewModelScope.launch {
            val apiCategory = if (category == "Semua") null else category
            notificationRepository.getAnnouncements(apiCategory).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(announcements = result.data, isLoading = false, errorMessage = null)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadAnnouncements(category)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadAnnouncementDetail(id: String) {
        viewModelScope.launch {
            notificationRepository.getAnnouncementDetail(id).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(selectedAnnouncement = result.data, isLoading = false)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun listenToWebSocketBroadcasts() {
        if (webSocketManager == null) return
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is WebSocketEvent.AnnouncementBroadcast -> {
                        val newAnn = AnnouncementItem(
                            id = event.announcementId,
                            title = event.title,
                            summary = event.summary,
                            content = event.summary,
                            category = event.category,
                            author = "Pusat Informasi Sekolah",
                            date = event.timestamp,
                            priority = event.priority
                        )
                        _uiState.update { state ->
                            state.copy(
                                announcements = listOf(newAnn) + state.announcements,
                                liveAlertBanner = "Pengumuman baru: ${event.title}"
                            )
                        }
                    }
                    is WebSocketEvent.EmergencyAlertTriggered -> {
                        _uiState.update { it.copy(liveAlertBanner = "🚨 DARURAT: ${event.title} - ${event.message}") }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun clearLiveBanner() {
        _uiState.update { it.copy(liveAlertBanner = null) }
    }
}
