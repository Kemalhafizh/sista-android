package com.sultanagung1.sista.ui.evaluation

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.FacilitySurveyItem
import com.sultanagung1.sista.data.model.OsisCandidateItem
import com.sultanagung1.sista.data.model.TeacherEvaluationItem
import com.sultanagung1.sista.data.repository.EvaluationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EvaluationUiState(
    val isLoading: Boolean = false,
    val teacherEvaluations: List<TeacherEvaluationItem> = emptyList(),
    val facilitySurveys: List<FacilitySurveyItem> = emptyList(),
    val electionId: Long? = null,
    val osisCandidates: List<OsisCandidateItem> = emptyList(),
    val selectedTab: Int = 0, // 0: Evaluasi Guru (EKG), 1: Survey Fasilitas, 2: E-Voting Ketua OSIS
    val evaluationSuccess: Boolean = false,
    val facilitySurveySuccess: Boolean = false,
    val voteSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class EvaluationViewModel @Inject constructor(private val repository: EvaluationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EvaluationUiState())
    val uiState: StateFlow<EvaluationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getTeacherEvaluations().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(teacherEvaluations = res.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getFacilitySurveys().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(facilitySurveys = res.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getOsisElection().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(electionId = res.data.id, osisCandidates = res.data.candidates, isLoading = false)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun submitTeacherEvaluation(id: Long, pedagogy: Int, punctuality: Int, manner: Int, feedback: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.submitTeacherEvaluation(id, pedagogy, punctuality, manner, feedback).collect { res ->
                when (res) {
                    is NetworkResult.Success -> {
                        val updated = _uiState.value.teacherEvaluations.map {
                            if (it.id == id) it.copy(isSubmitted = true, pedagogyRating = pedagogy, punctualityRating = punctuality, islamicMannerRating = manner)
                            else it
                        }
                        _uiState.update { it.copy(isLoading = false, teacherEvaluations = updated, evaluationSuccess = true) }
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    /** Stages a local star rating for [facilityId] — not yet sent to the server until submitFacilitySurveys(). */
    fun rateFacility(facilityId: Long, rating: Int) {
        _uiState.update { state ->
            state.copy(
                facilitySurveys = state.facilitySurveys.map {
                    if (it.id == facilityId) it.copy(satisfactionLevel = rating) else it
                }
            )
        }
    }

    /** Submits one real POST per rated (and not-yet-submitted) facility — the backend only accepts one facility_id per call. */
    fun submitFacilitySurveys() {
        val toSubmit = _uiState.value.facilitySurveys.filter { it.satisfactionLevel > 0 && !it.isSubmitted }
        if (toSubmit.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Beri rating minimal satu fasilitas sebelum mengirim survei.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var anySucceeded = false
            var lastError: String? = null
            toSubmit.forEach { survey ->
                repository.submitFacilityEvaluation(survey.id, survey.satisfactionLevel).collect { res ->
                    when (res) {
                        is NetworkResult.Success -> {
                            anySucceeded = true
                            _uiState.update { state ->
                                state.copy(facilitySurveys = state.facilitySurveys.map {
                                    if (it.id == survey.id) it.copy(isSubmitted = true) else it
                                })
                            }
                        }
                        is NetworkResult.Error -> lastError = res.message
                        is NetworkResult.Loading -> Unit
                    }
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    facilitySurveySuccess = anySucceeded,
                    errorMessage = if (!anySucceeded) lastError else null
                )
            }
        }
    }

    /**
     * [biometricSignature] must come from a real local BiometricVault
     * confirmation (see TeacherEvaluationScreen's vote dialog) — the
     * backend does not cryptographically verify this field (see
     * EvaluationMobileApiController::castVote()'s "Simulated biometric
     * validation" comment), so the ONLY real security value it carries is
     * whatever the caller genuinely gated it behind, not the string's
     * content itself.
     */
    fun castOsisVote(candidateId: Long, biometricSignature: String) {
        val electionId = _uiState.value.electionId ?: run {
            _uiState.update { it.copy(errorMessage = "Data pemilihan OSIS belum termuat.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.castOsisVote(electionId, candidateId, biometricSignature).collect { res ->
                when (res) {
                    is NetworkResult.Success -> {
                        val updated = _uiState.value.osisCandidates.map {
                            if (it.id == candidateId) it.copy(isVoted = true, totalVotes = it.totalVotes + 1)
                            else it.copy(isVoted = false)
                        }
                        _uiState.update { it.copy(isLoading = false, osisCandidates = updated, voteSuccess = true) }
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearSuccessFlags() {
        _uiState.update { it.copy(evaluationSuccess = false, facilitySurveySuccess = false, voteSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
