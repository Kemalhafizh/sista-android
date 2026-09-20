package com.sultanagung1.sista.ui.extracurricular

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.EkskulAttendanceRequest
import com.sultanagung1.sista.data.model.EkskulItem
import com.sultanagung1.sista.data.model.OsisPostItem
import com.sultanagung1.sista.data.repository.ExtracurricularRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExtracurricularUiState(
    val isLoading: Boolean = false,
    val ekskuls: List<EkskulItem> = emptyList(),
    val osisPosts: List<OsisPostItem> = emptyList(),
    val selectedTab: Int = 0, // 0: Katalog & Jadwal Ekskul, 1: Papan Feed OSIS
    val registrationSuccess: Boolean = false,
    val attendanceSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class ExtracurricularViewModel @Inject constructor(private val repository: ExtracurricularRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtracurricularUiState())
    val uiState: StateFlow<ExtracurricularUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getEkskulList().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(ekskuls = res.data) }
                }
            }
            repository.getOsisFeed().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(osisPosts = res.data, isLoading = false) }
                }
            }
        }
    }

    fun registerEkskul(ekskulId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.registerEkskul(ekskulId).collect { res ->
                if (res is NetworkResult.Success) {
                    val updated = _uiState.value.ekskuls.map {
                        if (it.id == ekskulId) it.copy(isRegistered = true, memberCount = it.memberCount + 1)
                        else it
                    }
                    _uiState.update { it.copy(isLoading = false, ekskuls = updated, registrationSuccess = true) }
                }
            }
        }
    }

    fun submitAttendance(ekskulId: Long, qrPayload: String, studentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.submitEkskulAttendance(EkskulAttendanceRequest(ekskulId, qrPayload, studentId)).collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(isLoading = false, attendanceSuccess = true) }
                }
            }
        }
    }

    fun clearSuccessFlags() {
        _uiState.update { it.copy(registrationSuccess = false, attendanceSuccess = false) }
    }
}
