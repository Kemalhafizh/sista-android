package com.sultanagung1.sista.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.AdminDashboardData
import com.sultanagung1.sista.data.model.EmergencyBroadcastData
import com.sultanagung1.sista.data.model.PendingApprovalItem
import com.sultanagung1.sista.data.model.SchoolKpiSummary
import com.sultanagung1.sista.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The outcome of deciding a request, as the server worded it. */
data class ApprovalOutcome(val message: String, val succeeded: Boolean)

data class AdminUiState(
    val principalName: String? = null,
    val isLoading: Boolean = false,
    val dashboardData: AdminDashboardData? = null,
    /** `mobile/admin/dashboard` failed; the headline numbers are unknown. */
    val errorMessage: String? = null,
    val schoolKpi: SchoolKpiSummary? = null,
    val kpiError: String? = null,
    val pendingApprovals: List<PendingApprovalItem> = emptyList(),
    val isLoadingApprovals: Boolean = false,
    val approvalsError: String? = null,
    val processingApprovalId: Long? = null,
    val approvalOutcome: ApprovalOutcome? = null,
    val isSendingEmergencyBroadcast: Boolean = false,
    val emergencyBroadcastSent: EmergencyBroadcastData? = null,
    val broadcastError: String? = null,
)

/**
 * The admin and leadership home: `mobile/admin/dashboard` headline numbers,
 * `mobile/admin/kpi`, the requests waiting on this account's step, and the
 * emergency broadcast. Each part fails on its own so one error never blanks
 * the rest.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userNameFlow.collect { name -> _uiState.update { it.copy(principalName = name) } }
        }
        refresh()
    }

    fun refresh() {
        loadDashboard()
        loadSchoolKpi()
        loadPendingApprovals()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            adminRepository.getDashboard().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, dashboardData = result.data, errorMessage = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun loadSchoolKpi() {
        viewModelScope.launch {
            adminRepository.getSchoolKpi().collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(schoolKpi = result.data, kpiError = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(kpiError = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    /** Requests whose current step is this account's to decide. */
    fun loadPendingApprovals() {
        viewModelScope.launch {
            adminRepository.getPendingApprovals().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoadingApprovals = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoadingApprovals = false, pendingApprovals = result.data, approvalsError = null)
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingApprovals = false, approvalsError = result.message) }
                }
            }
        }
    }

    /**
     * Decides this account's step. On success the request leaves this list (it
     * moved to the next approver or is finished); on refusal the list reloads,
     * since it was stale.
     */
    fun processApproval(id: Long, action: String, notes: String? = null) {
        if (_uiState.value.processingApprovalId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(processingApprovalId = id, approvalOutcome = null) }
            adminRepository.processApproval(id.toString(), action, notes).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                processingApprovalId = null,
                                pendingApprovals = it.pendingApprovals.filterNot { item -> item.id == id },
                                approvalOutcome = ApprovalOutcome(result.data, succeeded = true),
                            )
                        }
                        loadDashboard()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(processingApprovalId = null, approvalOutcome = ApprovalOutcome(result.message, succeeded = false)) }
                        loadPendingApprovals()
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun dismissApprovalOutcome() = _uiState.update { it.copy(approvalOutcome = null) }

    /** FASE 71.4 — pushes an emergency alert to every connected app. */
    fun broadcastEmergency(title: String, message: String, location: String?) {
        viewModelScope.launch {
            adminRepository.broadcastEmergency(title, message, location).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isSendingEmergencyBroadcast = true, broadcastError = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isSendingEmergencyBroadcast = false, emergencyBroadcastSent = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSendingEmergencyBroadcast = false, broadcastError = result.message) }
                }
            }
        }
    }

    fun dismissEmergencyBroadcastConfirmation() = _uiState.update { it.copy(emergencyBroadcastSent = null, broadcastError = null) }
}
