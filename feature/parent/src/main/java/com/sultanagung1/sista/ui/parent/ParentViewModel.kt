package com.sultanagung1.sista.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.ChildActivityEvent
import com.sultanagung1.sista.data.model.ChildAttendanceLog
import com.sultanagung1.sista.data.model.ChildGradeItem
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.ChildVsClassComparison
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.data.model.WeeklyDigest
import com.sultanagung1.sista.data.repository.NotificationRepository
import com.sultanagung1.sista.data.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentUiState(
    val isLoading: Boolean = false,
    // Real logged-in identity (SessionManager), not a hardcoded name.
    val parentName: String = "",
    val children: List<ParentChildItem> = emptyList(),
    /** Loading `parent/children` failed; nothing else can be shown. */
    val errorMessage: String? = null,
    val selectedChild: ParentChildItem? = null,
    // Everything below belongs to [selectedChild] and is cleared when it changes.
    val isLoadingChildDetail: Boolean = false,
    val selectedChildSummary: ChildSummaryResponse? = null,
    val childAttendanceLogs: List<ChildAttendanceLog> = emptyList(),
    val childGrades: List<ChildGradeItem> = emptyList(),
    val isLoadingExperience: Boolean = false,
    val activityFeed: List<ChildActivityEvent> = emptyList(),
    val weeklyDigest: WeeklyDigest? = null,
    val classComparison: List<ChildVsClassComparison> = emptyList(),
    /** A per-child section failed to load; the rest of the screen still shows. */
    val childErrorMessage: String? = null,
    // FASE 71.3: a live gate check-in for the selected child, pushed over the
    // backend's AttendanceLoggedEvent broadcast — null until one arrives.
    val liveGateStatus: WebSocketEvent.LiveAttendanceRecorded? = null,
    val unreadNotifications: Int = 0,
)

/**
 * The parent's children and, for the chosen child, everything the school
 * recorded: `parent/children`, `parent/child/{uuid}/summary|attendance|grades`
 * and `parent/child/{uuid}/feed|digest|benchmark`. Every per-child request
 * names the chosen child — a parent of two used to see the first child's feed
 * under the second child's name.
 */
@HiltViewModel
class ParentViewModel @Inject constructor(
    private val parentRepository: ParentRepository,
    private val sessionManager: SessionManager,
    private val webSocketManager: ReverbWebSocketManager,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    private var childJobs: List<Job> = emptyList()

    /** A child a screen was opened for (detail, feed); chosen as soon as the list arrives. */
    private var preferredUuid: String? = null

    init {
        loadDashboard()
        listenToLiveGateStatus()
    }

    private fun listenToLiveGateStatus() {
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                if (event is WebSocketEvent.LiveAttendanceRecorded &&
                    event.studentId == _uiState.value.selectedChild?.uuid
                ) {
                    _uiState.update { it.copy(liveGateStatus = event) }
                }
            }
        }
    }

    /** Loads the children, keeping the chosen child when it is still one of them. */
    fun loadDashboard() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val name = sessionManager.userNameFlow.first().orEmpty()
            _uiState.update { it.copy(parentName = name) }

            parentRepository.getChildren().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val keep = preferredUuid ?: _uiState.value.selectedChild?.uuid
                        val chosen = result.data.firstOrNull { it.uuid == keep } ?: result.data.firstOrNull()
                        _uiState.update { it.copy(isLoading = false, children = result.data) }
                        if (chosen != null) selectChild(chosen, force = true) else clearChild()
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun selectChild(child: ParentChildItem) = selectChild(child, force = false)

    /** Shows [uuid] when it is one of this parent's children; otherwise keeps the current child. */
    fun focusChild(uuid: String?) {
        if (uuid.isNullOrBlank()) return
        preferredUuid = uuid
        _uiState.value.children.firstOrNull { it.uuid == uuid }?.let(::selectChild)
    }

    private fun selectChild(child: ParentChildItem, force: Boolean) {
        if (!force && child.uuid == _uiState.value.selectedChild?.uuid) return
        childJobs.forEach { it.cancel() }
        _uiState.update {
            it.copy(
                selectedChild = child,
                selectedChildSummary = null,
                childAttendanceLogs = emptyList(),
                childGrades = emptyList(),
                activityFeed = emptyList(),
                weeklyDigest = null,
                classComparison = emptyList(),
                childErrorMessage = null,
                liveGateStatus = null,
                isLoadingChildDetail = true,
                isLoadingExperience = true,
            )
        }
        childJobs = loadChild(child.uuid)
    }

    private fun clearChild() {
        childJobs.forEach { it.cancel() }
        _uiState.update { ParentUiState(parentName = it.parentName, children = it.children) }
    }

    private fun loadChild(uuid: String): List<Job> = listOf(
        viewModelScope.launch {
            parentRepository.getChildSummary(uuid).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingChildDetail = false, selectedChildSummary = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingChildDetail = false, childErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        },
        viewModelScope.launch {
            parentRepository.getChildAttendanceHistory(uuid).collect { result ->
                if (result is NetworkResult.Success) _uiState.update { it.copy(childAttendanceLogs = result.data) }
            }
        },
        viewModelScope.launch {
            parentRepository.getChildGrades(uuid).collect { result ->
                if (result is NetworkResult.Success) _uiState.update { it.copy(childGrades = result.data) }
            }
        },
        viewModelScope.launch {
            parentRepository.getChildFeed(uuid).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingExperience = false, activityFeed = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingExperience = false, childErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        },
        viewModelScope.launch {
            parentRepository.getWeeklyDigest(uuid).collect { result ->
                if (result is NetworkResult.Success) _uiState.update { it.copy(weeklyDigest = result.data) }
            }
        },
        viewModelScope.launch {
            parentRepository.getChildComparison(uuid).collect { result ->
                if (result is NetworkResult.Success) _uiState.update { it.copy(classComparison = result.data) }
            }
        },
    )

    /** The bell on the home; reloaded whenever the home comes back into view. */
    fun loadUnreadNotifications() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount().collect { result ->
                if (result is NetworkResult.Success) _uiState.update { it.copy(unreadNotifications = result.data) }
            }
        }
    }

    /** Reloads the chosen child's data (pull to refresh). */
    fun refresh() {
        val child = _uiState.value.selectedChild
        if (child == null) loadDashboard() else selectChild(child, force = true)
    }
}
