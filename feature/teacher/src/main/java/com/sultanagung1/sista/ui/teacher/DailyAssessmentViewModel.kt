package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.AssessmentScoreSheet
import com.sultanagung1.sista.data.model.DailyAssessmentItem
import com.sultanagung1.sista.data.model.RemedialDashboard
import com.sultanagung1.sista.data.model.RemedialItem
import com.sultanagung1.sista.data.model.ScoreSheetRules
import com.sultanagung1.sista.data.model.StudentAssessmentHistoryItem
import com.sultanagung1.sista.data.repository.DailyAssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyAssessmentUiState(
    val isLoading: Boolean = false,
    val assessments: List<DailyAssessmentItem> = emptyList(),
    val remedialDashboard: RemedialDashboard? = null,
    val remedials: List<RemedialItem> = emptyList(),
    val studentHistory: List<StudentAssessmentHistoryItem> = emptyList(),
    /** The score sheet of the assessment being scored (`assessments/{id}/scores`). */
    val sheet: AssessmentScoreSheet? = null,
    val isLoadingSheet: Boolean = false,
    val sheetError: String? = null,
    /** Text typed per student id; students without an entry keep their stored score. */
    val edits: Map<Long, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val summary: ScoreSheetRules.Summary?
        get() = sheet?.let { ScoreSheetRules.summary(it.students, edits, it.assessment.kkm, it.assessment.maxScore) }
}

/**
 * Daily assessments for teachers (list, score sheet, auto remedial) and the
 * student's remedial list. Nothing loads on creation: each screen asks for
 * its own data, so a student never calls a teacher endpoint.
 */
@HiltViewModel
class DailyAssessmentViewModel @Inject constructor(
    private val repository: DailyAssessmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyAssessmentUiState())
    val uiState: StateFlow<DailyAssessmentUiState> = _uiState.asStateFlow()

    fun fetchTeacherAssessments() {
        viewModelScope.launch {
            repository.getTeacherAssessments().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            assessments = result.data.assessments,
                            remedialDashboard = result.data.remedialDashboard,
                            errorMessage = null,
                        )
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    /** Loads the class and its stored scores; typed edits are kept across reloads. */
    fun loadScoreSheet(assessmentId: Long) {
        viewModelScope.launch {
            repository.getScoreSheet(assessmentId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoadingSheet = true, sheetError = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingSheet = false, sheet = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingSheet = false, sheetError = result.message) }
                }
            }
        }
    }

    fun editScore(studentId: Long, text: String) {
        val stored = _uiState.value.sheet?.students?.firstOrNull { it.studentId == studentId }?.score
        _uiState.update {
            // Typing the stored value back is no change at all.
            val edits = if (text == ScoreSheetRules.textOf(stored)) it.edits - studentId else it.edits + (studentId to text)
            it.copy(edits = edits, successMessage = null)
        }
    }

    /** Sends only changed, valid scores; a blank is never sent as 0. */
    fun saveScores() {
        val state = _uiState.value
        val sheet = state.sheet ?: return
        val changes = ScoreSheetRules.changes(sheet.students, state.edits, sheet.assessment.maxScore)
        if (changes.isEmpty() || state.isSubmitting) return
        val assessmentId = sheet.assessment.id
        viewModelScope.launch {
            repository.submitBatchScores(assessmentId, changes).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
                    is NetworkResult.Success -> {
                        val under = result.data.studentsUnderKkm
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                edits = emptyMap(),
                                successMessage = "${result.data.totalSaved} nilai tersimpan." +
                                    if (under > 0) " $under siswa di bawah KKM." else "",
                            )
                        }
                        loadScoreSheet(assessmentId)
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun triggerAutoRemedial(assessmentId: Long, deadline: String? = null) {
        viewModelScope.launch {
            repository.autoAssignRemedial(assessmentId, deadline).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isSubmitting = false, successMessage = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun fetchStudentRemedials() {
        viewModelScope.launch {
            repository.getStudentRemedials().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, remedials = result.data, errorMessage = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun fetchStudentHistory() {
        viewModelScope.launch {
            repository.getStudentAssessmentHistory().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, studentHistory = result.data, errorMessage = null) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
