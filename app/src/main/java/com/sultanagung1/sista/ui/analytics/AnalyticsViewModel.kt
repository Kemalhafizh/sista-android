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


@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _studentAnalytics = MutableStateFlow<StudentAnalyticsData?>(null)
    val studentAnalytics: StateFlow<StudentAnalyticsData?> = _studentAnalytics.asStateFlow()

    private val _classAnalytics = MutableStateFlow<ClassAnalyticsData?>(null)
    val classAnalytics: StateFlow<ClassAnalyticsData?> = _classAnalytics.asStateFlow()

    private val _parentProgress = MutableStateFlow<ParentProgressData?>(null)
    val parentProgress: StateFlow<ParentProgressData?> = _parentProgress.asStateFlow()

    private val _executiveKpi = MutableStateFlow<ExecutiveAnalyticsData?>(null)
    val executiveKpi: StateFlow<ExecutiveAnalyticsData?> = _executiveKpi.asStateFlow()

    init {
        loadAllAnalytics()
    }

    fun loadAllAnalytics() {
        viewModelScope.launch {
            analyticsRepository.getStudentAnalytics().collect { res ->
                if (res is NetworkResult.Success) _studentAnalytics.value = res.data
            }
        }
        viewModelScope.launch {
            analyticsRepository.getClassAnalytics("XII MIPA 1", "Fisika Modern").collect { res ->
                if (res is NetworkResult.Success) _classAnalytics.value = res.data
            }
        }
        viewModelScope.launch {
            analyticsRepository.getParentProgress().collect { res ->
                if (res is NetworkResult.Success) _parentProgress.value = res.data
            }
        }
        viewModelScope.launch {
            analyticsRepository.getExecutiveKpi().collect { res ->
                if (res is NetworkResult.Success) _executiveKpi.value = res.data
            }
        }
    }
}
