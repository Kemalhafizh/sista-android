package com.sultanagung1.sista.ui.schoolops

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.SchoolOperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchoolOperationsUiState(
    val isLoading: Boolean = false,
    val calendarEvents: List<AcademicCalendarEventItem> = emptyList(),
    val spmbWaves: List<SpmbWaveItem> = emptyList(),
    val spmbStatus: SpmbRegistrationStatus? = null,
    val uksVisits: List<UksRecordVisitItem> = emptyList(),
    val healthScreening: HealthScreeningData? = null,
    val teachingJournals: List<SchoolTeachingJournalItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val journalSavedSuccess: Boolean = false,
    val spmbRegisteredSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class SchoolOperationsViewModel @Inject constructor(
    private val repository: SchoolOperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolOperationsUiState())
    val uiState: StateFlow<SchoolOperationsUiState> = _uiState.asStateFlow()

    init {
        loadCalendarEvents()
        loadSpmbWaves()
        loadUksData()
        loadTeachingJournals()
    }

    fun loadCalendarEvents(month: Int? = null, category: String? = null) {
        viewModelScope.launch {
            repository.getCalendarEvents(month = month, category = category).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(calendarEvents = result.data) }
                }
            }
        }
    }

    fun loadSpmbWaves() {
        viewModelScope.launch {
            repository.getSpmbWaves().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(spmbWaves = result.data) }
                }
            }
        }
    }

    fun registerSpmb(
        fullName: String,
        nisn: String,
        schoolOrigin: String,
        phone: String,
        track: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val request = SpmbRegisterRequest(
                fullName = fullName,
                nisn = nisn,
                schoolOrigin = schoolOrigin,
                phoneNumber = phone,
                trackName = track
            )
            repository.registerSpmb(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                spmbRegisteredSuccess = true,
                                spmbStatus = result.data
                            )
                        }
                        onSuccess()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun checkSpmbStatus(regNo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.checkSpmbStatus(regNo).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(isLoading = false, spmbStatus = result.data) }
                }
            }
        }
    }

    fun loadUksData(studentId: Long? = null) {
        viewModelScope.launch {
            repository.getUksVisits(studentId).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(uksVisits = result.data) }
                }
            }
        }
        viewModelScope.launch {
            repository.getHealthScreening(studentId ?: 1L).collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(healthScreening = result.data) }
                }
            }
        }
    }

    fun loadTeachingJournals() {
        viewModelScope.launch {
            repository.getTeachingJournals().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(teachingJournals = result.data) }
                }
            }
        }
    }

    fun storeJournal(
        className: String,
        subjectName: String,
        topic: String,
        date: String,
        notes: String,
        present: Int,
        absent: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val request = StoreTeachingJournalRequest(
                className = className,
                subjectName = subjectName,
                topic = topic,
                date = date,
                notes = notes,
                attendancePresent = present,
                attendanceAbsent = absent
            )
            repository.storeTeachingJournal(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            val updated = it.teachingJournals.toMutableList()
                            updated.add(0, result.data)
                            it.copy(
                                isSubmitting = false,
                                journalSavedSuccess = true,
                                teachingJournals = updated
                            )
                        }
                        onSuccess()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                    }
                    else -> {}
                }
            }
        }
    }
}
