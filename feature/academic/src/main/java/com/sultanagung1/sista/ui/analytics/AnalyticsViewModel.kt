package com.sultanagung1.sista.ui.analytics

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalyticsUiState<out T> {
    data object Loading : AnalyticsUiState<Nothing>
    data class Success<T>(val data: T) : AnalyticsUiState<T>
    data class Error(val message: String) : AnalyticsUiState<Nothing>
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _studentAnalytics = MutableStateFlow<AnalyticsUiState<StudentAnalyticsData>>(AnalyticsUiState.Loading)
    val studentAnalytics: StateFlow<AnalyticsUiState<StudentAnalyticsData>> = _studentAnalytics.asStateFlow()

    private val _classAnalytics = MutableStateFlow<AnalyticsUiState<ClassAnalyticsData>>(AnalyticsUiState.Loading)
    val classAnalytics: StateFlow<AnalyticsUiState<ClassAnalyticsData>> = _classAnalytics.asStateFlow()

    private val _teacherClassOptions = MutableStateFlow<List<TeacherClassOption>>(emptyList())
    val teacherClassOptions: StateFlow<List<TeacherClassOption>> = _teacherClassOptions.asStateFlow()

    private val _selectedTeacherClass = MutableStateFlow<TeacherClassOption?>(null)
    val selectedTeacherClass: StateFlow<TeacherClassOption?> = _selectedTeacherClass.asStateFlow()

    private val _parentProgress = MutableStateFlow<AnalyticsUiState<ParentProgressData>>(AnalyticsUiState.Loading)
    val parentProgress: StateFlow<AnalyticsUiState<ParentProgressData>> = _parentProgress.asStateFlow()

    private val _executiveKpi = MutableStateFlow<AnalyticsUiState<ExecutiveAnalyticsData>>(AnalyticsUiState.Loading)
    val executiveKpi: StateFlow<AnalyticsUiState<ExecutiveAnalyticsData>> = _executiveKpi.asStateFlow()

    init {
        loadAllAnalytics()
    }

    fun loadAllAnalytics() {
        loadStudentAnalytics()
        loadClassAnalytics()
        loadParentProgress()
        loadExecutiveKpi()
    }

    fun loadStudentAnalytics() {
        viewModelScope.launch {
            _studentAnalytics.value = AnalyticsUiState.Loading
            analyticsRepository.getStudentAnalytics().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _studentAnalytics.value = AnalyticsUiState.Success(res.data)
                    is NetworkResult.Error -> _studentAnalytics.value = AnalyticsUiState.Error(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun loadClassAnalytics() {
        viewModelScope.launch {
            _classAnalytics.value = AnalyticsUiState.Loading
            analyticsRepository.getTeacherClasses().collect { res ->
                when (res) {
                    is NetworkResult.Success -> {
                        _teacherClassOptions.value = res.data
                        val target = _selectedTeacherClass.value ?: res.data.firstOrNull()
                        if (target != null) {
                            _selectedTeacherClass.value = target
                            loadClassPerformance(target)
                        } else {
                            _classAnalytics.value = AnalyticsUiState.Error("Anda belum memiliki kelas/mata pelajaran yang diampu.")
                        }
                    }
                    is NetworkResult.Error -> _classAnalytics.value = AnalyticsUiState.Error(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun selectTeacherClass(option: TeacherClassOption) {
        _selectedTeacherClass.value = option
        loadClassPerformance(option)
    }

    private fun loadClassPerformance(option: TeacherClassOption) {
        viewModelScope.launch {
            _classAnalytics.value = AnalyticsUiState.Loading
            analyticsRepository.getClassAnalytics(option.className, option.subjectName).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _classAnalytics.value = AnalyticsUiState.Success(res.data)
                    is NetworkResult.Error -> _classAnalytics.value = AnalyticsUiState.Error(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun loadParentProgress() {
        viewModelScope.launch {
            _parentProgress.value = AnalyticsUiState.Loading
            analyticsRepository.getParentProgress().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _parentProgress.value = AnalyticsUiState.Success(res.data)
                    is NetworkResult.Error -> _parentProgress.value = AnalyticsUiState.Error(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun loadExecutiveKpi() {
        viewModelScope.launch {
            _executiveKpi.value = AnalyticsUiState.Loading
            analyticsRepository.getExecutiveKpi().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _executiveKpi.value = AnalyticsUiState.Success(res.data)
                    is NetworkResult.Error -> _executiveKpi.value = AnalyticsUiState.Error(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
