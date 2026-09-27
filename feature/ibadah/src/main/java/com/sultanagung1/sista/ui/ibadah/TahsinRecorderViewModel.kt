package com.sultanagung1.sista.ui.ibadah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.TahsinSubmissionItem
import com.sultanagung1.sista.data.repository.TahsinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TahsinRecorderUiState(
    val isSubmitting: Boolean = false,
    val submitResult: TahsinSubmissionItem? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TahsinRecorderViewModel @Inject constructor(
    private val tahsinRepository: TahsinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TahsinRecorderUiState())
    val uiState: StateFlow<TahsinRecorderUiState> = _uiState.asStateFlow()

    fun submitRecording(filePath: String, surahName: String, startAyah: Int?, endAyah: Int?, durationSeconds: Int) {
        viewModelScope.launch {
            _uiState.value = TahsinRecorderUiState(isSubmitting = true)
            tahsinRepository.submitRecording(filePath, surahName, startAyah, endAyah, durationSeconds).collect { result ->
                _uiState.value = when (result) {
                    is NetworkResult.Success -> TahsinRecorderUiState(isSubmitting = false, submitResult = result.data)
                    is NetworkResult.Error -> TahsinRecorderUiState(isSubmitting = false, errorMessage = result.message)
                    is NetworkResult.Loading -> _uiState.value.copy(isSubmitting = true)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = TahsinRecorderUiState()
    }
}
