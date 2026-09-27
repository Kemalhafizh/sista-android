package com.sultanagung1.sista.ui.document

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.OcrScanResult
import com.sultanagung1.sista.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentScannerUiState(
    val isUploading: Boolean = false,
    val isPolling: Boolean = false,
    val result: OcrScanResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DocumentScannerViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentScannerUiState())
    val uiState: StateFlow<DocumentScannerUiState> = _uiState.asStateFlow()

    fun scanDocument(imageBytes: ByteArray, fileName: String, mimeType: String, documentType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null, result = null) }
            repository.startOcrScan(imageBytes, fileName, mimeType, documentType).collect { started ->
                when (started) {
                    is NetworkResult.Success -> {
                        _uiState.update { it.copy(isUploading = false, isPolling = true) }
                        repository.pollOcrResult(started.data.taskId).collect { polled ->
                            when (polled) {
                                is NetworkResult.Success -> _uiState.update {
                                    it.copy(isPolling = false, result = polled.data)
                                }
                                is NetworkResult.Error -> _uiState.update {
                                    it.copy(isPolling = false, errorMessage = polled.message)
                                }
                                is NetworkResult.Loading -> Unit
                            }
                        }
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isUploading = false, errorMessage = started.message)
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(result = null, errorMessage = null) }
    }
}
