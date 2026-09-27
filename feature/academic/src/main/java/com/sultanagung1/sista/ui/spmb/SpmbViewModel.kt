package com.sultanagung1.sista.ui.spmb

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.SpmbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SpmbUiEvent : UiEvent {
    data class LoadData(val unit: Unit = Unit) : SpmbUiEvent
    data class Track(val registrationNumber: String) : SpmbUiEvent
    data class ClearMessages(val unit: Unit = Unit) : SpmbUiEvent
}

data class SpmbUiState(
    val isLoading: Boolean = false,
    val waves: List<SpmbWaveItem> = emptyList(),
    val requirements: List<String> = emptyList(),
    val registrationFee: Long = 250000L,
    val currentStep: Int = 1, // 1: Data Diri, 2: Data Orang Tua, 3: Unggah Dokumen, 4: Konfirmasi
    val draft: SpmbRegistrationDraft = SpmbRegistrationDraft(),
    val isSubmitting: Boolean = false,
    val submissionSuccessNumber: String? = null,
    val trackingQuery: String = "",
    val trackingInfo: SpmbTrackingInfo? = null,
    val isTrackingLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState


@HiltViewModel
class SpmbViewModel @Inject constructor(
    private val spmbRepository: SpmbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpmbUiState())
    val uiState: StateFlow<SpmbUiState> = _uiState.asStateFlow()

    init {
        loadSpmbData()
    }

    fun onEvent(event: SpmbUiEvent) {
        when (event) {
            is SpmbUiEvent.LoadData -> loadSpmbData()
            is SpmbUiEvent.Track -> trackRegistration(event.registrationNumber)
            is SpmbUiEvent.ClearMessages -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    fun loadSpmbData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            spmbRepository.getWaves().collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoading = false, waves = result.data, requirements = getStaticRequirements(), errorMessage = null)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message, requirements = getStaticRequirements())
                    }
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun updateDraft(transform: (SpmbRegistrationDraft) -> SpmbRegistrationDraft) {
        _uiState.update { it.copy(draft = transform(it.draft)) }
    }

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(1, 4)) }
    }

    fun nextStep(): Boolean {
        val current = _uiState.value.currentStep
        val draft = _uiState.value.draft

        when (current) {
            1 -> {
                if (draft.fullName.isBlank() || draft.nisn.isBlank() || draft.schoolOrigin.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Mohon lengkapi Nama Lengkap, NISN, dan Asal Sekolah.") }
                    return false
                }
            }
            2 -> {
                if (draft.fatherName.isBlank() || draft.motherName.isBlank() || draft.phoneWhatsApp.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Mohon lengkapi Nama Ayah, Nama Ibu, dan No WhatsApp.") }
                    return false
                }
            }
            3 -> {
                // Upload documents step
            }
        }

        _uiState.update { it.copy(currentStep = (current + 1).coerceAtMost(4), errorMessage = null) }
        return true
    }

    fun prevStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1), errorMessage = null) }
    }

    // Real files picked from the device, keyed by docType — held here rather
    // than in SpmbRegistrationDraft (a :core:model class with zero Android
    // dependency by design) and uploaded once a real registration_number
    // exists (registration happens at Step 4, after documents are picked at
    // Step 3, so they can't be sent until submitRegistration() succeeds).
    private val pickedDocuments = mutableMapOf<String, SpmbDocumentAttachment>()

    fun onDocumentPicked(docType: String, attachment: SpmbDocumentAttachment) {
        pickedDocuments[docType] = attachment
        updateDraft { draft ->
            when (docType) {
                "photo" -> draft.copy(uploadedPhotoName = attachment.fileName)
                "kk" -> draft.copy(uploadedKkName = attachment.fileName)
                "rapor" -> draft.copy(uploadedRaporName = attachment.fileName)
                "sertifikat" -> draft.copy(uploadedCertificateName = attachment.fileName)
                else -> draft
            }
        }
    }

    fun submitRegistration(onSuccess: (String) -> Unit) {
        val draft = _uiState.value.draft
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val req = SpmbRegisterRequest(
                fullName = draft.fullName,
                nisn = draft.nisn,
                schoolOrigin = draft.schoolOrigin,
                phoneNumber = draft.phoneWhatsApp,
                trackName = draft.selectedTrack
            )
            spmbRepository.register(req).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val regNumber = result.data.registrationNumber
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                submissionSuccessNumber = regNumber,
                                successMessage = "Pendaftaran berhasil! Nomor Registrasi Anda: $regNumber"
                            )
                        }
                        // Real documents picked in Step 3 can only be sent now
                        // that a real registration_number exists to attach
                        // them to — a failure here doesn't undo the
                        // registration, it's surfaced separately below.
                        uploadPickedDocuments(regNumber)
                        trackRegistration(regNumber)
                        onSuccess(regNumber)
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = result.message)
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    private fun uploadPickedDocuments(registrationNo: String) {
        pickedDocuments.forEach { (docType, attachment) ->
            viewModelScope.launch {
                spmbRepository.uploadDocument(registrationNo, docType, attachment).collect { result ->
                    if (result is NetworkResult.Error) {
                        _uiState.update {
                            it.copy(errorMessage = "Sebagian dokumen gagal diunggah: ${result.message}. Anda dapat mengunggah ulang nanti.")
                        }
                    }
                }
            }
        }
    }

    fun setTrackingQuery(query: String) {
        _uiState.update { it.copy(trackingQuery = query) }
    }

    fun trackRegistration(registrationNumber: String) {
        val reg = registrationNumber.trim().ifBlank { _uiState.value.trackingQuery.trim() }
        if (reg.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Masukkan nomor registrasi SPMB Anda.") }
            return
        }

        _uiState.update { it.copy(isTrackingLoading = true, trackingQuery = reg, errorMessage = null) }
        viewModelScope.launch {
            spmbRepository.trackRegistration(reg).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val status = result.data
                        _uiState.update {
                            it.copy(
                                isTrackingLoading = false,
                                trackingInfo = SpmbTrackingInfo(
                                    registrationNumber = status.registrationNumber,
                                    applicantName = status.fullName,
                                    track = status.track,
                                    status = status.verificationStatus,
                                    // No payment-gateway integration exists for
                                    // SPMB yet, so this can't be verified — left
                                    // false rather than always claiming "paid".
                                    paymentVerified = false,
                                    timeline = buildTimeline(status.verificationStatus),
                                    cbtDate = status.cbtTestDate,
                                    interviewDate = status.interviewDate,
                                    notes = status.notes
                                )
                            )
                        }
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isTrackingLoading = false, errorMessage = result.message, trackingInfo = null)
                    }
                    is NetworkResult.Loading -> _uiState.update { it.copy(isTrackingLoading = true) }
                }
            }
        }
    }

    /**
     * A best-effort visual timeline derived from the real verification
     * status — the backend has no per-step timestamp columns (no CBT/
     * interview scheduling exists yet either, see spmbCheckStatus()), so
     * only the two steps backed by a real status transition get a real
     * "completed" state; the rest show as pending with no fabricated dates.
     */
    private fun buildTimeline(status: String): List<TimelineEvent> {
        val isVerified = status == "terverifikasi"
        return listOf(
            TimelineEvent("Pendaftaran Formulir Online", true, null, "Formulir pendaftaran berhasil diserahkan."),
            TimelineEvent("Verifikasi Berkas Administrasi", isVerified, null, if (isVerified) "Berkas telah diverifikasi oleh panitia." else "Menunggu verifikasi panitia."),
            TimelineEvent("Ujian CBT Potensi Akademik & Minat", false, null, "Jadwal akan diumumkan melalui SuperApp."),
            TimelineEvent("Wawancara Keislaman & Tahfidz", false, null, "Jadwal akan diumumkan melalui SuperApp."),
            TimelineEvent("Pengumuman Kelulusan Resmi", false, null, "Pengumuman melalui aplikasi Sulaone Mobile."),
            TimelineEvent("Daftar Ulang & Pengukuran Seragam", false, null, "Penyelesaian administrasi dan fitting seragam YBWSA.")
        )
    }

    private fun getStaticRequirements(): List<String> {
        return listOf(
            "Pas Foto berwarna terbaru ukuran 3x4 (latar belakang merah/biru)",
            "Scan/Foto Asli Kartu Keluarga (KK) & Akta Kelahiran",
            "Scan Rapor SMP/MTs sederajat Semester 1 s.d 5 (dilegalisir)",
            "Piagam/Sertifikat Kejuaraan Akademik/Non-Akademik (Khusus Jalur Prestasi)",
            "Syahadah/Surat Keterangan Tahfidz minimal 3 Juz (Khusus Jalur Tahfidz)",
            "Surat Pernyataan Kesanggupan Mentaati Tata Tertib Sekolah YBWSA"
        )
    }

}
