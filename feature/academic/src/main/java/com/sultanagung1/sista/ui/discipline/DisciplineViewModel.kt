package com.sultanagung1.sista.ui.discipline

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.DisciplineSummary
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.data.model.SignWarningLetterRequest
import com.sultanagung1.sista.data.model.WarningLetterItem
import com.sultanagung1.sista.data.repository.DisciplineRepository
import com.sultanagung1.sista.data.repository.ParentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisciplineUiState(
    val isLoading: Boolean = false,
    // Null until the server answers — never a made-up "BAIK / 0 poin".
    val summary: DisciplineSummary? = null,
    val records: List<DisciplineRecord> = emptyList(),
    val recordsLoaded: Boolean = false,
    val warningLetters: List<WarningLetterItem> = emptyList(),
    val lettersLoaded: Boolean = false,
    val selectedTab: Int = 0, // 0: Buku Saku Poin, 1: Riwayat Kasus, 2: Surat Peringatan (SP)
    // Parent accounts only: their children, and whose record is on screen.
    val children: List<ParentChildItem> = emptyList(),
    val selectedChildUuid: String? = null,
    val isSigning: Boolean = false,
    val signError: String? = null,
    val signatureSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class DisciplineViewModel @Inject constructor(
    private val repository: DisciplineRepository,
    private val parentRepository: ParentRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DisciplineUiState(
            // The child selected on the parent dashboard (Screen.Discipline.createRoute).
            selectedChildUuid = savedStateHandle.get<String>(ARG_STUDENT_UUID)?.takeIf { it.isNotBlank() }
        )
    )
    val uiState: StateFlow<DisciplineUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadData()
        viewModelScope.launch {
            val role = sessionManager.userRoleFlow.first().orEmpty().lowercase()
            if (role in PARENT_ROLES) loadChildren()
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    /** Parent switching child: clear the previous child's data before loading, never mix the two. */
    fun selectChild(uuid: String) {
        if (uuid == _uiState.value.selectedChildUuid) return
        _uiState.update {
            it.copy(
                selectedChildUuid = uuid,
                summary = null,
                records = emptyList(),
                recordsLoaded = false,
                warningLetters = emptyList(),
                lettersLoaded = false
            )
        }
        loadData()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            parentRepository.getChildren().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(children = res.data) }
                    is NetworkResult.Error -> recordError("Daftar anak gagal dimuat: ${res.message}")
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun loadData() {
        // A newer load (e.g. after switching child) replaces an unfinished one,
        // so a slow response for the previous child can't overwrite this one.
        loadJob?.cancel()
        val childUuid = _uiState.value.selectedChildUuid
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getDisciplineSummary(childUuid).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(
                            summary = res.data,
                            // No child was chosen: remember which one the server showed.
                            selectedChildUuid = it.selectedChildUuid ?: res.data.student?.uuid
                        )
                    }
                    is NetworkResult.Error -> recordError(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getDisciplineRecords(childUuid).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(records = res.data, recordsLoaded = true) }
                    is NetworkResult.Error -> recordError(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getWarningLetters(childUuid).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(warningLetters = res.data, lettersLoaded = true) }
                    is NetworkResult.Error -> recordError(res.message)
                    is NetworkResult.Loading -> Unit
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /** Keeps the first failure; later ones are usually the same outage. */
    private fun recordError(message: String) =
        _uiState.update { it.copy(errorMessage = it.errorMessage ?: message) }

    /** The signer is the logged-in parent account; the server checks it's this student's parent. */
    fun signWarningLetter(letterId: Long, signatureBase64: String) {
        if (_uiState.value.isSigning) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSigning = true, signError = null) }
            repository.signWarningLetter(letterId, SignWarningLetterRequest(signatureBase64)).collect { res ->
                when (res) {
                    is NetworkResult.Success -> {
                        _uiState.update { state ->
                            val updatedLetters = state.warningLetters.map {
                                if (it.id == letterId) it.copy(isSigned = true, signedAt = res.data, canSign = false)
                                else it
                            }
                            state.copy(isSigning = false, warningLetters = updatedLetters, signatureSuccess = true)
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isSigning = false, signError = res.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearSignError() {
        _uiState.update { it.copy(signError = null) }
    }

    fun clearSignatureSuccess() {
        _uiState.update { it.copy(signatureSuccess = false) }
    }

    companion object {
        /** Nav argument of Screen.Discipline. */
        const val ARG_STUDENT_UUID = "studentUuid"

        /** Roles the server treats as a parent in DisciplineMobileApiController. */
        val PARENT_ROLES = setOf("parent", "orangtua")
    }
}
