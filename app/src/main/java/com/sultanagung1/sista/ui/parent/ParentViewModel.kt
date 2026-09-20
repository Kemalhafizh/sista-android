package com.sultanagung1.sista.ui.parent

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentUiState(
    val isLoading: Boolean = false,
    // Real logged-in identity (SessionManager), not a hardcoded "Bapak Hendra Gunawan".
    val parentName: String = "",
    val children: List<ParentChildItem> = emptyList(),
    val selectedChild: ParentChildItem? = null,
    val isLoadingChildDetail: Boolean = false,
    val selectedChildSummary: ChildSummaryResponse? = null,
    val childAttendanceLogs: List<ChildAttendanceLog> = emptyList(),
    val childGrades: List<ChildGradeItem> = emptyList(),
    val activityFeed: List<com.sultanagung1.sista.data.model.ChildActivityEvent> = emptyList(),
    val weeklyDigest: com.sultanagung1.sista.data.model.WeeklyDigest? = null,
    val classComparison: List<com.sultanagung1.sista.data.model.ChildVsClassComparison> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Real parent/wali-murid dashboard state. Previously called
 * `mobile/parent/dashboard` / `mobile/parent/children/{id}/attendance` /
 * `mobile/parent/children/{id}/grades` — none of which exist anywhere in the
 * backend, so every load unconditionally fell back to a hardcoded
 * "Bapak Hendra Gunawan, S.T." identity with two fabricated children. This now
 * uses the real, fully-built ApiParentController surface (parent/children,
 * parent/child/{uuid}/summary, parent/child/{uuid}/attendance). Fields the
 * backend genuinely has no data for (homeroom teacher's phone number, BK
 * counselor assignment, a live gate check-in timestamp) are simply not
 * fabricated — the UI shows an honest "belum tersedia" instead.
 */
@HiltViewModel
class ParentViewModel @Inject constructor(
    private val parentRepository: ParentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        loadParentExperience()
    }

    fun loadParentExperience() {
        viewModelScope.launch {
            parentRepository.getChildFeed().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(activityFeed = result.data) }
                }
            }
        }
        viewModelScope.launch {
            parentRepository.getWeeklyDigest().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(weeklyDigest = result.data) }
                }
            }
        }
        viewModelScope.launch {
            parentRepository.getChildComparison().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(classComparison = result.data) }
                }
            }
        }
    }

    fun loadDashboard() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val name = sessionManager.userNameFlow.first().orEmpty()
            _uiState.update { it.copy(parentName = name) }

            parentRepository.getChildren().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val firstChild = result.data.firstOrNull()
                        _uiState.update {
                            it.copy(isLoading = false, children = result.data, selectedChild = firstChild)
                        }
                        firstChild?.let { loadChildDetail(it.uuid) }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun selectChild(child: ParentChildItem) {
        _uiState.update { it.copy(selectedChild = child) }
        loadChildDetail(child.uuid)
    }

    private fun loadChildDetail(uuid: String) {
        _uiState.update { it.copy(isLoadingChildDetail = true) }
        viewModelScope.launch {
            parentRepository.getChildSummary(uuid).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update { it.copy(isLoadingChildDetail = false, selectedChildSummary = result.data) }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoadingChildDetail = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
        viewModelScope.launch {
            parentRepository.getChildAttendanceHistory(uuid).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(childAttendanceLogs = result.data) }
                }
            }
        }
        viewModelScope.launch {
            parentRepository.getChildGrades(uuid).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(childGrades = result.data) }
                }
            }
        }
    }
}
