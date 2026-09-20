package com.sultanagung1.sista.ui.discipline

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.DisciplineSummary
import com.sultanagung1.sista.data.model.SignWarningLetterRequest
import com.sultanagung1.sista.data.model.WarningLetterItem
import com.sultanagung1.sista.data.repository.DisciplineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisciplineUiState(
    val isLoading: Boolean = false,
    val summary: DisciplineSummary = DisciplineSummary(),
    val records: List<DisciplineRecord> = emptyList(),
    val warningLetters: List<WarningLetterItem> = emptyList(),
    val selectedTab: Int = 0, // 0: Buku Saku Poin, 1: Riwayat Kasus, 2: Surat Peringatan (SP)
    val signatureSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class DisciplineViewModel @Inject constructor(private val repository: DisciplineRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DisciplineUiState())
    val uiState: StateFlow<DisciplineUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getDisciplineSummary().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(summary = res.data) }
                }
            }
            repository.getDisciplineRecords().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(records = res.data) }
                }
            }
            repository.getWarningLetters().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(warningLetters = res.data, isLoading = false) }
                }
            }
        }
    }

    fun signWarningLetter(letterId: Long, signatureData: String, parentName: String, parentPhone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = SignWarningLetterRequest(letterId, signatureData, parentName, parentPhone)
            repository.signWarningLetter(letterId, request).collect { res ->
                when (res) {
                    is NetworkResult.Success -> {
                        _uiState.update { state ->
                            val updatedLetters = state.warningLetters.map {
                                if (it.id == letterId) it.copy(isSignedByParent = true, parentSignedAt = "2026-08-26 21:00")
                                else it
                            }
                            state.copy(isLoading = false, warningLetters = updatedLetters, signatureSuccess = true)
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun clearSignatureSuccess() {
        _uiState.update { it.copy(signatureSuccess = false) }
    }
}
