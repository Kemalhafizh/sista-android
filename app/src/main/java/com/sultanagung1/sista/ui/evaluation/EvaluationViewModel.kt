package com.sultanagung1.sista.ui.evaluation

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.FacilitySurveyItem
import com.sultanagung1.sista.data.model.OsisCandidateItem
import com.sultanagung1.sista.data.model.SubmitEvaluationRequest
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
    val osisCandidates: List<OsisCandidateItem> = emptyList(),
    val selectedTab: Int = 0, // 0: Evaluasi Guru (EKG), 1: Survey Fasilitas, 2: E-Voting Ketua OSIS
    val evaluationSuccess: Boolean = false,
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
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(teacherEvaluations = res.data) }
                }
            }
            repository.getFacilitySurveys().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(facilitySurveys = res.data) }
                }
            }
            repository.getOsisElection().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(osisCandidates = res.data, isLoading = false) }
                }
            }
        }
    }

    fun submitTeacherEvaluation(id: Long, pedagogy: Int, punctuality: Int, manner: Int, feedback: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val req = SubmitEvaluationRequest(id, pedagogy, punctuality, manner, feedback)
            repository.submitTeacherEvaluation(req).collect { res ->
                if (res is NetworkResult.Success) {
                    val updated = _uiState.value.teacherEvaluations.map {
                        if (it.id == id) it.copy(isSubmitted = true, pedagogyRating = pedagogy, punctualityRating = punctuality, islamicMannerRating = manner)
                        else it
                    }
                    _uiState.update { it.copy(isLoading = false, teacherEvaluations = updated, evaluationSuccess = true) }
                }
            }
        }
    }

    fun castOsisVote(candidateId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.castOsisVote(candidateId, "voter_sha256_mock_hash").collect { res ->
                if (res is NetworkResult.Success) {
                    val updated = _uiState.value.osisCandidates.map {
                        if (it.id == candidateId) it.copy(isVoted = true, totalVotes = it.totalVotes + 1)
                        else it.copy(isVoted = false)
                    }
                    _uiState.update { it.copy(isLoading = false, osisCandidates = updated, voteSuccess = true) }
                }
            }
        }
    }

    fun clearSuccessFlags() {
        _uiState.update { it.copy(evaluationSuccess = false, voteSuccess = false) }
    }
}
