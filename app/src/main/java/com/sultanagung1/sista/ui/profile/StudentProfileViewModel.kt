package com.sultanagung1.sista.ui.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.StudentProfile360Data
import com.sultanagung1.sista.data.repository.StudentProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentProfileUiState(
    val isLoading: Boolean = false,
    val profile: StudentProfile360Data? = null,
    val selectedTab: Int = 0,
    val errorMessage: String? = null
)


@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val repository: StudentProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile(null)
    }

    fun loadProfile(studentId: Long? = null) {
        viewModelScope.launch {
            repository.getStudentProfile(studentId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoading = false, profile = result.data)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}
