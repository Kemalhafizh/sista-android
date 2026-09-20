package com.sultanagung1.sista.ui.utbk

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.AlumniCampusItem
import com.sultanagung1.sista.data.model.MajorRecommendationItem
import com.sultanagung1.sista.data.model.UtbkTryoutItem
import com.sultanagung1.sista.data.repository.UtbkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UtbkUiState(
    val isLoading: Boolean = false,
    val tryouts: List<UtbkTryoutItem> = emptyList(),
    val recommendations: List<MajorRecommendationItem> = emptyList(),
    val alumniList: List<AlumniCampusItem> = emptyList(),
    val selectedTab: Int = 0, // 0: Simulasi UTBK, 1: Rekomendasi Jurusan AI, 2: Direktori Alumni PTN
    val activeSimulationId: Long? = null,
    val activeSubtestTimerSeconds: Int = 2700 // 45 minutes
)


@HiltViewModel
class UtbkViewModel @Inject constructor(private val repository: UtbkRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UtbkUiState())
    val uiState: StateFlow<UtbkUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getUtbkTryouts().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(tryouts = res.data) }
                }
            }
            repository.getMajorRecommendations().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(recommendations = res.data) }
                }
            }
            repository.getCampusAlumniDirectory().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(alumniList = res.data, isLoading = false) }
                }
            }
        }
    }
}
