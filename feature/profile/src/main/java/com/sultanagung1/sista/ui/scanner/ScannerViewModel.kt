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
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _currentMode = MutableStateFlow(ScanMode.ATTENDANCE)
    val currentMode: StateFlow<ScanMode> = _currentMode.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<ParsedScanResult?>(null)
    val scanResult: StateFlow<ParsedScanResult?> = _scanResult.asStateFlow()

    fun switchMode(mode: ScanMode) {
        _currentMode.value = mode
        resetScan()
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    fun onCodeScanned(rawCode: String) {
        if (!_isScanning.value) return
        _isScanning.value = false

        viewModelScope.launch {
            val parsed = ScanResultHandler.parse(rawCode, _currentMode.value)
            _scanResult.value = parsed
        }
    }

    fun simulateScanSuccess() {
        val samplePayload = when (_currentMode.value) {
            ScanMode.ATTENDANCE -> "SULAONE-ATTENDANCE-QR-2026-XYZ887"
            ScanMode.LIBRARY_BOOK -> "9786022448006"
            ScanMode.EVENT_TICKET -> "TICKET-KAJIAN-1448H-9921"
            ScanMode.VISITOR_PASS -> "VISITOR-PASS-YBWSA-3341"
        }
        onCodeScanned(samplePayload)
    }

    fun resetScan() {
        _scanResult.value = null
        _isScanning.value = true
    }
}
