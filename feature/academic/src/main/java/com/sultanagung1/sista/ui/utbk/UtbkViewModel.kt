package com.sultanagung1.sista.ui.utbk

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.AlumniCampusItem
import com.sultanagung1.sista.data.model.PtnRecommendationItem
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
    val estimatedScore: Double = 0.0,
    val recommendations: List<PtnRecommendationItem> = emptyList(),
    val recommendationMessage: String? = null,
    val alumniList: List<AlumniCampusItem> = emptyList(),
    val selectedTab: Int = 0, // 0: Simulasi UTBK, 1: Rekomendasi Jurusan AI, 2: Direktori Alumni PTN
    val activeSimulationId: Long? = null,
    val activeSubtestTimerSeconds: Int = 2700, // 45 minutes
    val errorMessage: String? = null
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
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(tryouts = res.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getMajorRecommendations().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(
                            estimatedScore = res.data.estimatedScore,
                            recommendations = res.data.recommendations,
                            recommendationMessage = res.data.message
                        )
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getCampusAlumniDirectory().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(alumniList = res.data, isLoading = false) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
