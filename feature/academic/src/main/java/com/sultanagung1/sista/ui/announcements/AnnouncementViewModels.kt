package com.sultanagung1.sista.ui.announcements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.AnnouncementItem
import com.sultanagung1.sista.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The categories the web form offers; [apiValue] is what the server stores. */
enum class AnnouncementCategory(val label: String, val apiValue: String?) {
    All("Semua", null),
    Academic("Akademik", "akademik"),
    Finance("Keuangan", "keuangan"),
    Activity("Kegiatan", "kegiatan"),
    Islamic("Keislaman", "keislaman"),
    General("Umum", "umum"),
    ;

    companion object {
        fun of(apiValue: String?): AnnouncementCategory =
            values().firstOrNull { it.apiValue != null && it.apiValue.equals(apiValue, ignoreCase = true) } ?: General
    }
}

data class AnnouncementFeedUiState(
    val isLoading: Boolean = false,
    /** Null until the first load. */
    val announcements: List<AnnouncementItem>? = null,
    val errorMessage: String? = null,
    val category: AnnouncementCategory = AnnouncementCategory.All,
    val query: String = "",
    /** A live broadcast or emergency that arrived while the list was open. */
    val liveBanner: String? = null,
    val liveIsEmergency: Boolean = false,
) {
    /** Search runs on what the server sent: title, summary and author. */
    val visible: List<AnnouncementItem>
        get() {
            val q = query.trim()
            val list = announcements.orEmpty()
            return if (q.isEmpty()) list else list.filter {
                it.title.contains(q, true) || it.summary.contains(q, true) || it.author.contains(q, true)
            }
        }
}

/** Announcements meant for this account, pinned first; a live broadcast reloads the list. */
@HiltViewModel
class AnnouncementFeedViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val webSocketManager: ReverbWebSocketManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementFeedUiState())
    val uiState: StateFlow<AnnouncementFeedUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is WebSocketEvent.AnnouncementBroadcast -> {
                        _uiState.update { it.copy(liveBanner = "Pengumuman baru: ${event.title}", liveIsEmergency = false) }
                        load()
                    }
                    is WebSocketEvent.EmergencyAlertTriggered ->
                        _uiState.update { it.copy(liveBanner = "${event.title}. ${event.message}", liveIsEmergency = true) }
                    else -> Unit
                }
            }
        }
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            notificationRepository.getAnnouncements(_uiState.value.category.apiValue).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, announcements = result.data, errorMessage = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun selectCategory(category: AnnouncementCategory) {
        if (category == _uiState.value.category) return
        _uiState.update { it.copy(category = category, announcements = null, errorMessage = null) }
        load()
    }

    fun search(query: String) = _uiState.update { it.copy(query = query) }

    /** The server records the read when the detail opens; show it at once here. */
    fun markOpened(id: String) = _uiState.update { state ->
        state.copy(announcements = state.announcements?.map { if (it.id == id) it.copy(isRead = true) else it })
    }

    fun dismissBanner() = _uiState.update { it.copy(liveBanner = null) }
}

data class AnnouncementDetailUiState(
    val isLoading: Boolean = false,
    val announcement: AnnouncementItem? = null,
    val errorMessage: String? = null,
    val acknowledging: Boolean = false,
    val acknowledgeError: String? = null,
)

/** One announcement from the route's `id`; opening it records the read. */
@HiltViewModel
class AnnouncementDetailViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val id: String? = savedStateHandle.get<String>("id")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(AnnouncementDetailUiState())
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = id ?: run {
            _uiState.update { it.copy(errorMessage = "Pengumuman tidak ditemukan.") }
            return
        }
        viewModelScope.launch {
            notificationRepository.getAnnouncementDetail(id).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, announcement = result.data, errorMessage = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun acknowledge() {
        val id = id ?: return
        if (_uiState.value.acknowledging) return
        viewModelScope.launch {
            notificationRepository.acknowledgeAnnouncement(id).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(acknowledging = true, acknowledgeError = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(acknowledging = false, announcement = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(acknowledging = false, acknowledgeError = result.message) }
                }
            }
        }
    }
}
