package com.sultanagung1.sista.ui.document

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.SignatureSubmission
import com.sultanagung1.sista.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignatureUiState(
    val isSubmitting: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SignatureViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignatureUiState())
    val uiState: StateFlow<SignatureUiState> = _uiState.asStateFlow()

    fun submit(documentType: String, documentId: Long, signatureDataBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            repository.submitDigitalSignature(
                SignatureSubmission(documentType, documentId, signatureDataBase64)
            ).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(isSubmitting = false, isSaved = true) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
