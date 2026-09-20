package com.sultanagung1.sista.ui.ibadah

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.MutabaahLogItem
import com.sultanagung1.sista.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IbadahUiState(
    val isLoading: Boolean = false,
    val mutabaahLogs: List<MutabaahLogItem> = emptyList(),
    val errorMessage: String? = null
) {
    /** Most recent recorded day — the backend has no fixed checklist template, only a log per date. */
    val latestLog: MutabaahLogItem? get() = mutabaahLogs.maxByOrNull { it.date }
}

@HiltViewModel
class IbadahViewModel @Inject constructor(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(IbadahUiState())
    val uiState: StateFlow<IbadahUiState> = _uiState.asStateFlow()

    init {
        loadMutabaah()
    }

    fun loadMutabaah() {
        viewModelScope.launch {
            studentRepository.getMutabaah().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, mutabaahLogs = result.data)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }
}
