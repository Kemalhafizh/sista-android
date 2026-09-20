package com.sultanagung1.sista.ui.elearning

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.ElearningMobileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ElearningUiState(
    val isLoading: Boolean = false,
    val classes: List<ElearningClassItem> = emptyList(),
    val materials: List<ElearningMaterialItem> = emptyList(),
    val assignments: List<ElearningAssignmentItem> = emptyList(),
    val selectedClass: ElearningClassItem? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class ElearningViewModel @Inject constructor(
    private val repository: ElearningMobileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ElearningUiState())
    val uiState: StateFlow<ElearningUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            repository.getStudentClasses().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoading = false, classes = result.data)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun selectClass(classItem: ElearningClassItem) {
        _uiState.update { it.copy(selectedClass = classItem) }
        loadClassDetails(classItem.id)
    }

    fun loadClassDetails(classId: Long) {
        viewModelScope.launch {
            repository.getClassMaterials(classId).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(materials = result.data) }
                }
            }
        }
        viewModelScope.launch {
            repository.getClassAssignments(classId).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(assignments = result.data) }
                }
            }
        }
    }

    fun submitAssignment(assignmentId: Long, note: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitSuccess = false) }
            repository.submitAssignment(assignmentId, note, null).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            val updatedAssignments = it.assignments.map { a ->
                                if (a.id == assignmentId) a.copy(isSubmitted = true, submission = result.data) else a
                            }
                            it.copy(
                                isSubmitting = false,
                                submitSuccess = true,
                                assignments = updatedAssignments
                            )
                        }
                        onDone()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun clearSubmitStatus() {
        _uiState.update { it.copy(submitSuccess = false, errorMessage = null) }
    }
}
