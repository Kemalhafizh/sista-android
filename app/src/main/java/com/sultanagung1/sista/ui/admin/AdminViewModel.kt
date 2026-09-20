package com.sultanagung1.sista.ui.admin

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val dashboardData: AdminDashboardData? = null,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)


@HiltViewModel
class AdminViewModel @Inject constructor(private val adminRepository: AdminRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
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

    fun processApproval(id: String, action: String) {
        viewModelScope.launch {
            adminRepository.processApproval(id, action).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> {
                        val currentPending = _uiState.value.dashboardData?.pendingApprovals?.filterNot { it.id == id } ?: emptyList()
                        val updatedData = _uiState.value.dashboardData?.copy(pendingApprovals = currentPending)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dashboardData = updatedData,
                            actionSuccessMessage = "Pengajuan berhasil di-${if (action == "approve") "setujui" else "tolak"}."
                        )
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionSuccessMessage = null, errorMessage = null)
    }
}
