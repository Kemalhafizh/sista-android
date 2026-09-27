package com.sultanagung1.sista.ui.scanner

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanResultHandler: ScanResultHandler
) : ViewModel() {

    private val _currentMode = MutableStateFlow(ScanMode.ATTENDANCE)
    val currentMode: StateFlow<ScanMode> = _currentMode.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _scanResult = MutableStateFlow<ParsedScanResult?>(null)
    val scanResult: StateFlow<ParsedScanResult?> = _scanResult.asStateFlow()

    fun switchMode(mode: ScanMode) {
        _currentMode.value = mode
        resetScan()
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    /** [rawCode] is a real decoded barcode/QR payload from the camera analyzer — never a fabricated sample. */
    fun onCodeScanned(rawCode: String) {
        if (!_isScanning.value || _isProcessing.value) return
        _isScanning.value = false
        _isProcessing.value = true

        viewModelScope.launch {
            val parsed = scanResultHandler.parse(rawCode, _currentMode.value)
            _isProcessing.value = false
            _scanResult.value = parsed
        }
    }

    fun resetScan() {
        _scanResult.value = null
        _isScanning.value = true
    }
}
