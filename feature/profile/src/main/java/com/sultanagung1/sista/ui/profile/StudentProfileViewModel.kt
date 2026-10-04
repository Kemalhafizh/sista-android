package com.sultanagung1.sista.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.StudentProfile360Data
import com.sultanagung1.sista.data.repository.StudentProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileTab(val label: String) {
    Academic("Akademik"), Ibadah("Ibadah"), Discipline("Kedisiplinan"), Activities("Prestasi & ekskul"), Health("Kesehatan"),
}

/** [profile] null + [errorMessage] null = still loading. */
data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val profile: StudentProfile360Data? = null,
    val tab: ProfileTab = ProfileTab.Academic,
    val errorMessage: String? = null,
)

/**
 * One student's 360 profile. Which student comes from the route
 * (studentId = the student's user id; none/0 = the signed-in student). The
 * server decides who may see it: another family's child or a non-student
 * answers 403/404 and the screen shows that message.
 */
@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val repository: StudentProfileRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentId: Long? = savedStateHandle.get<Long>("studentId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.getStudentProfile(studentId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, profile = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(tab = tab) }
    }
}
