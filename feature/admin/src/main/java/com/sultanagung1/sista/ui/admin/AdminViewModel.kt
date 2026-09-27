package com.sultanagung1.sista.ui.admin

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val principalName: String? = null,
    val dashboardData: AdminDashboardData? = null,
    val schoolKpi: SchoolKpiSummary? = null,
    val pendingApprovals: List<PendingApprovalItem> = emptyList(),
    val isLoadingApprovals: Boolean = false,
    val processingApprovalId: Long? = null,
    val isSendingEmergencyBroadcast: Boolean = false,
    val emergencyBroadcastSent: EmergencyBroadcastData? = null,
    val errorMessage: String? = null
)


@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userNameFlow.collect { name ->
                _uiState.value = _uiState.value.copy(principalName = name)
            }
        }
        loadDashboard()
        loadSchoolKpi()
        loadPendingApprovals()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            adminRepository.getDashboard().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboardData = result.data,
                        errorMessage = null
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /** FASE 71.4 follow-up: real SPP-per-cohort ratio & CBT server usage. */
    fun loadSchoolKpi() {
        viewModelScope.launch {
            adminRepository.getSchoolKpi().collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(schoolKpi = result.data)
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    /** FASE 71.4 follow-up: real pending-approvals list, replacing the count-only card. */
    fun loadPendingApprovals() {
        viewModelScope.launch {
            adminRepository.getPendingApprovals().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoadingApprovals = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoadingApprovals = false,
                        pendingApprovals = result.data
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoadingApprovals = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /** Approve/reject a single pending request, then refresh the list so the acted-on item disappears. */
    fun processApproval(id: Long, action: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingApprovalId = id)
            adminRepository.processApproval(id.toString(), action).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            processingApprovalId = null,
                            pendingApprovals = _uiState.value.pendingApprovals.filterNot { it.id == id }
                        )
                        loadDashboard() // refresh the count card on the same screen too
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        processingApprovalId = null,
                        errorMessage = result.message
                    )
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /** FASE 71.4 "Tombol Siaran Darurat" — pushes a real emergency alert to every connected app. */
    fun broadcastEmergency(title: String, message: String, location: String?) {
        viewModelScope.launch {
            adminRepository.broadcastEmergency(title, message, location).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isSendingEmergencyBroadcast = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isSendingEmergencyBroadcast = false,
                        emergencyBroadcastSent = result.data,
                        errorMessage = null
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isSendingEmergencyBroadcast = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun dismissEmergencyBroadcastConfirmation() {
        _uiState.value = _uiState.value.copy(emergencyBroadcastSent = null)
    }
}
