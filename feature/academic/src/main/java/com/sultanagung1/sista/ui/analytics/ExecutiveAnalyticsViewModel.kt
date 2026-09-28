package com.sultanagung1.sista.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.ExecutiveAnalyticsData
import com.sultanagung1.sista.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExecutiveUiState(
    val isLoading: Boolean = false,
    val data: ExecutiveAnalyticsData? = null,
    val errorMessage: String? = null,
)

/** Leadership analytics: `analytics/executive/kpi` only. */
@HiltViewModel
class ExecutiveAnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExecutiveUiState())
    val uiState: StateFlow<ExecutiveUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Keeps what is on screen while reloading; an error then shows beside it. */
    fun load() {
        viewModelScope.launch {
            analyticsRepository.getExecutiveKpi().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { ExecutiveUiState(data = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
