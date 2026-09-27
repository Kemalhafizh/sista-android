package com.sultanagung1.sista.ui.academic

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.RaporDetailData
import com.sultanagung1.sista.data.repository.RaporRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RaporUiState(
    val isLoading: Boolean = false,
    val raporData: RaporDetailData? = null,
    val isExportingPdf: Boolean = false,
    val errorMessage: String? = null,
    val exportSuccessMessage: String? = null
)


@HiltViewModel
class RaporViewModel @Inject constructor(
    private val repository: RaporRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaporUiState())
    val uiState: StateFlow<RaporUiState> = _uiState.asStateFlow()

    fun fetchRapor(childId: Long? = null) {
        viewModelScope.launch {
            repository.getStudentRapor(childId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        raporData = result.data,
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

    fun exportPdf(reportCardId: Long, studentId: Long) {
        viewModelScope.launch {
            repository.generatePdf(reportCardId, studentId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isExportingPdf = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isExportingPdf = false,
                        exportSuccessMessage = result.data
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isExportingPdf = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
