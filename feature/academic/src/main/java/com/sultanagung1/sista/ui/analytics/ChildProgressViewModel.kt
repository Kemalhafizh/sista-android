package com.sultanagung1.sista.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.ParentProgressData
import com.sultanagung1.sista.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildProgressUiState(
    val isLoading: Boolean = false,
    val data: ParentProgressData? = null,
    val errorMessage: String? = null,
)

/**
 * One child's progress. The child comes from the route (`studentUuid`), so it
 * is the child chosen on the parent's home; without one the server answers
 * for the first linked child.
 */
@HiltViewModel
class ChildProgressViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentUuid: String? = savedStateHandle.get<String>("studentUuid")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(ChildProgressUiState())
    val uiState: StateFlow<ChildProgressUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            analyticsRepository.getParentProgress(studentUuid).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { ChildProgressUiState(data = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
