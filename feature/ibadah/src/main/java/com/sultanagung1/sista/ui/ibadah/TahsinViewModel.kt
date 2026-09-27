package com.sultanagung1.sista.ui.ibadah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.TahsinSubmissionItem
import com.sultanagung1.sista.data.repository.TahsinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TahsinUiState(
    val isLoadingList: Boolean = false,
    val submissions: List<TahsinSubmissionItem> = emptyList(),
    val statusFilter: String? = null, // teacher list: null | "pending" | "reviewed"
    val listErrorMessage: String? = null,

    val isLoadingDetail: Boolean = false,
    val detail: TahsinSubmissionItem? = null,
    val detailErrorMessage: String? = null,

    val isAnnotating: Boolean = false,
    val annotateErrorMessage: String? = null,

    val isMarkingReviewed: Boolean = false
)

/**
 * Shared by the student's submission history and the teacher's review
 * queue — both list and view/annotate the same TahsinSubmission records,
 * just through different endpoints and permissions (enforced server-side).
 */
@HiltViewModel
class TahsinViewModel @Inject constructor(
    private val tahsinRepository: TahsinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TahsinUiState())
    val uiState: StateFlow<TahsinUiState> = _uiState.asStateFlow()

    fun loadMySubmissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listErrorMessage = null) }
            tahsinRepository.getMySubmissions().collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingList = false, submissions = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingList = false, listErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun loadAssignedSubmissions(status: String? = _uiState.value.statusFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listErrorMessage = null, statusFilter = status) }
            tahsinRepository.getAssignedSubmissions(status).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingList = false, submissions = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingList = false, listErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, detailErrorMessage = null) }
            tahsinRepository.getSubmissionDetail(id).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoadingDetail = false, detail = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoadingDetail = false, detailErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun annotate(submissionId: Long, timestampSeconds: Int, note: String, tajwidCategory: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnnotating = true, annotateErrorMessage = null) }
            tahsinRepository.annotate(submissionId, timestampSeconds, note, tajwidCategory).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update { it.copy(isAnnotating = false) }
                        // Reload detail so the new marker/annotation list reflects
                        // the server's authoritative state immediately.
                        loadDetail(submissionId)
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isAnnotating = false, annotateErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun markReviewed(submissionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingReviewed = true) }
            tahsinRepository.markReviewed(submissionId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isMarkingReviewed = false, detail = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isMarkingReviewed = false, detailErrorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearAnnotateError() {
        _uiState.update { it.copy(annotateErrorMessage = null) }
    }
}
