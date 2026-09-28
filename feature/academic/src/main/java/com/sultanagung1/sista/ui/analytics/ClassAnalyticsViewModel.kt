package com.sultanagung1.sista.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.ClassAnalyticsData
import com.sultanagung1.sista.data.model.TeacherClassOption
import com.sultanagung1.sista.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassAnalyticsUiState(
    val isLoadingClasses: Boolean = false,
    /** Null until the list arrives; empty when the teacher has no class this year. */
    val classes: List<TeacherClassOption>? = null,
    val classesError: String? = null,
    val selected: TeacherClassOption? = null,
    val isLoadingPerformance: Boolean = false,
    val performance: ClassAnalyticsData? = null,
    val performanceError: String? = null,
)

/**
 * The teacher's class analytics: the (class, subject) pairs they teach this
 * year, then the grades of the chosen one.
 */
@HiltViewModel
class ClassAnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassAnalyticsUiState())
    val uiState: StateFlow<ClassAnalyticsUiState> = _uiState.asStateFlow()

    private var performanceJob: Job? = null

    init {
        load()
    }

    /** Reloads the class list, then the chosen class (kept when it is still taught). */
    fun load() {
        viewModelScope.launch {
            analyticsRepository.getTeacherClasses().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoadingClasses = true) }
                    is NetworkResult.Success -> {
                        val classes = result.data
                        val keep = _uiState.value.selected?.let { current -> classes.firstOrNull { it.sameAs(current) } }
                        val target = keep ?: classes.firstOrNull()
                        _uiState.update { it.copy(isLoadingClasses = false, classes = classes, classesError = null) }
                        if (target != null) select(target) else _uiState.update { it.copy(selected = null, performance = null) }
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingClasses = false, classesError = result.message) }
                }
            }
        }
    }

    fun select(option: TeacherClassOption) {
        val switching = _uiState.value.selected?.sameAs(option) != true
        _uiState.update {
            it.copy(
                selected = option,
                performance = if (switching) null else it.performance,
                performanceError = null,
            )
        }
        performanceJob?.cancel()
        performanceJob = viewModelScope.launch {
            analyticsRepository.getClassAnalytics(option).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoadingPerformance = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingPerformance = false, performance = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingPerformance = false, performanceError = result.message) }
                }
            }
        }
    }
}

/** Same class and subject: by id when the server sent ids, else by name. */
internal fun TeacherClassOption.sameAs(other: TeacherClassOption): Boolean =
    if (classroomId != null && other.classroomId != null) {
        classroomId == other.classroomId && subjectId == other.subjectId
    } else {
        className == other.className && subjectName == other.subjectName
    }
