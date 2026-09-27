package com.sultanagung1.sista.ui.uks

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.UksRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UksUiEvent : UiEvent {
    data class LoadData(val studentId: String? = null) : UksUiEvent
    data class ClearMessages(val unit: Unit = Unit) : UksUiEvent
}

data class UksUiState(
    val isLoading: Boolean = false,
    val todayVisits: List<UksVisit> = emptyList(),
    val healthHistory: List<HealthRecord> = emptyList(),
    val screeningSummary: HealthScreeningSummary? = null,
    val availableMedicines: List<MedicineItem> = emptyList(),
    val searchQuery: String = "",
    val isSavingVisit: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // === Real student search for the record-visit form — replaces the old
    // free-text student name field, which had no way to resolve a real
    // student_id to save the visit against. ===
    val studentQuery: String = "",
    val studentSearchResults: List<UksStudentSearchItem> = emptyList(),
    val isSearchingStudent: Boolean = false,
    val selectedStudent: UksStudentSearchItem? = null
) : UiState {
    val filteredVisits: List<UksVisit>
        get() = if (searchQuery.isBlank()) todayVisits
        else todayVisits.filter {
            it.studentName.contains(searchQuery, ignoreCase = true) ||
            it.complaint.contains(searchQuery, ignoreCase = true) ||
            (it.diagnosis?.contains(searchQuery, ignoreCase = true) == true)
        }
}


@HiltViewModel
class UksViewModel @Inject constructor(
    private val uksRepository: UksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UksUiState())
    val uiState: StateFlow<UksUiState> = _uiState.asStateFlow()

    private var studentSearchJob: Job? = null

    init {
        loadData()
    }

    fun onEvent(event: UksUiEvent) {
        when (event) {
            is UksUiEvent.LoadData -> loadData(event.studentId)
            is UksUiEvent.ClearMessages -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    /**
     * Loads real visits/screening/medicines — no sample-data fallback on
     * failure. A load error now surfaces honestly via errorMessage instead
     * of silently substituting fabricated records that looked identical to
     * real ones.
     */
    fun loadData(studentId: String? = null) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        // All visits from getTodayVisits(studentId) belong to this one
        // student — the endpoint is scoped that way — so the display name
        // comes from who was looked up, not per-item (the API response
        // itself carries no student name field, since it doesn't need one).
        val displayName = _uiState.value.selectedStudent?.name
            ?: _uiState.value.screeningSummary?.studentName
            ?: "Siswa #$studentId"
        viewModelScope.launch {
            var visits: List<UksVisit> = emptyList()
            var screening: HealthScreeningSummary? = null
            var loadError: String? = null

            uksRepository.getTodayVisits(studentId?.toLongOrNull()).collect { result ->
                when (result) {
                    is NetworkResult.Success -> visits = result.data.map { item ->
                        UksVisit(
                            id = item.id.toString(),
                            studentName = displayName,
                            time = item.visitTime,
                            complaint = item.complaints,
                            action = item.action ?: "istirahat_uks",
                            temperature = item.temperature?.toFloatOrNull(),
                            bloodPressure = item.bloodPressure,
                            diagnosis = item.diagnosis,
                            medicines = item.medicines.joinToString(", ") { "${it.name} (${it.dose})" }
                                .ifBlank { null },
                            officer = item.handlerName,
                            parentNotified = item.parentNotified
                        )
                    }
                    is NetworkResult.Error -> loadError = result.message
                    is NetworkResult.Loading -> Unit
                }
            }

            if (!studentId.isNullOrBlank()) {
                uksRepository.getHealthScreening(studentId).collect { result ->
                    if (result is NetworkResult.Success) {
                        val s = result.data
                        screening = HealthScreeningSummary(
                            studentName = s.studentName,
                            nisn = s.nisn,
                            bloodType = s.bloodType,
                            heightCm = s.heightCm,
                            weightKg = s.weightKg,
                            bmi = s.bmi,
                            bmiCategory = s.bmiCategory,
                            visionRight = s.visionRight,
                            visionLeft = s.visionLeft,
                            dentalHealth = s.dentalHealth,
                            hearing = s.hearing,
                            allergies = s.allergies,
                            lastScreenedAt = s.lastScreenedAt,
                            screener = s.screener
                        )
                    }
                }
            }

            var medicines: List<MedicineItem> = emptyList()
            uksRepository.getMedicines().collect { result ->
                if (result is NetworkResult.Success) medicines = result.data
            }

            // The visits list IS the real health history for this student —
            // there's no separate "history" endpoint on the backend, and
            // there never was real data behind the old hardcoded 3-item list.
            val history = visits.map { v ->
                HealthRecord(
                    id = v.id,
                    date = v.time,
                    complaint = v.complaint,
                    diagnosis = v.diagnosis,
                    action = v.action,
                    medicines = v.medicines ?: "-",
                    officer = v.officer
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    todayVisits = visits,
                    healthHistory = history,
                    screeningSummary = screening,
                    availableMedicines = medicines,
                    errorMessage = loadError
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Debounced real student search for the record-visit form. */
    fun setStudentQuery(query: String) {
        _uiState.update { it.copy(studentQuery = query, selectedStudent = null) }
        studentSearchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(studentSearchResults = emptyList()) }
            return
        }
        studentSearchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(isSearchingStudent = true) }
            uksRepository.searchStudents(query).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isSearchingStudent = false, studentSearchResults = result.data)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isSearchingStudent = false, studentSearchResults = emptyList())
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun selectStudent(student: UksStudentSearchItem) {
        _uiState.update {
            it.copy(selectedStudent = student, studentQuery = student.name, studentSearchResults = emptyList())
        }
    }

    /**
     * Saves a real UKS visit — requires a real selected student ([selectStudent])
     * rather than a free-text name, since the backend needs a real student_id.
     */
    fun recordVisit(
        complaint: String,
        temperature: Float?,
        bloodPressure: String?,
        diagnosis: String?,
        actions: List<String>,
        selectedMedicines: List<UksMedicineSelection>,
        notes: String,
        onSuccess: () -> Unit
    ) {
        val student = _uiState.value.selectedStudent
        if (student == null || complaint.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pilih siswa dari hasil pencarian dan isi Keluhan wajib diisi.") }
            return
        }

        _uiState.update { it.copy(isSavingVisit = true, errorMessage = null) }
        viewModelScope.launch {
            val request = UksVisitRequest(
                studentId = student.id.toString(),
                complaint = complaint,
                temperature = temperature,
                bloodPressure = bloodPressure,
                diagnosis = diagnosis,
                actions = actions,
                medicines = selectedMedicines,
                notes = notes
            )
            uksRepository.recordVisit(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSavingVisit = false,
                                successMessage = "Kunjungan UKS berhasil dicatat & tersimpan di server.",
                                selectedStudent = null,
                                studentQuery = ""
                            )
                        }
                        loadData()
                        onSuccess()
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isSavingVisit = false, errorMessage = result.message)
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
