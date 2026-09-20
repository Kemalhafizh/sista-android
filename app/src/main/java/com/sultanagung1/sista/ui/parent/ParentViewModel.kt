package com.sultanagung1.sista.ui.parent

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParentUiState(
    val isLoading: Boolean = false,
    val dashboardData: ParentDashboardData? = null,
    val selectedChild: ChildSummary? = null,
    val childAttendanceLogs: List<ChildAttendanceLog> = emptyList(),
    val activityFeed: List<com.sultanagung1.sista.data.model.ChildActivityEvent> = emptyList(),
    val weeklyDigest: com.sultanagung1.sista.data.model.WeeklyDigest? = null,
    val classComparison: List<com.sultanagung1.sista.data.model.ChildVsClassComparison> = emptyList(),
    val errorMessage: String? = null
)


@HiltViewModel
class ParentViewModel @Inject constructor(
    private val parentRepository: ParentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        loadParentExperience()
    }

    fun loadParentExperience() {
        viewModelScope.launch {
            try {
                parentRepository.getChildFeed().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.value = _uiState.value.copy(activityFeed = result.data)
                    }
                }
                parentRepository.getWeeklyDigest().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.value = _uiState.value.copy(weeklyDigest = result.data)
                    }
                }
                parentRepository.getChildComparison().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.value = _uiState.value.copy(classComparison = result.data)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            parentRepository.getDashboard().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> {
                        val data = result.data
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dashboardData = data,
                            selectedChild = data.children.firstOrNull(),
                            errorMessage = null
                        )
                        data.children.firstOrNull()?.let { loadChildAttendance(it.studentId) }
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun selectChild(child: ChildSummary) {
        _uiState.value = _uiState.value.copy(selectedChild = child)
        loadChildAttendance(child.studentId)
    }

    fun loadChildAttendance(studentId: String) {
        viewModelScope.launch {
            parentRepository.getChildAttendanceHistory(studentId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(childAttendanceLogs = result.data)
                    else -> {}
                }
            }
        }
    }
}
