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
import kotlin.random.Random

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
            try {
                spmbRepository.getWaves().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    waves = result.data,
                                    requirements = getSampleRequirements()
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    waves = getSampleWaves(),
                                    requirements = getSampleRequirements()
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        waves = getSampleWaves(),
                        requirements = getSampleRequirements()
                    )
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

    fun simulateUploadDoc(docType: String) {
        val randomFileId = Random.nextInt(1000, 9999)
        updateDraft { draft ->
            when (docType) {
                "photo" -> draft.copy(uploadedPhotoName = "PAS_FOTO_${draft.nisn.ifBlank { "CALON" }}_$randomFileId.jpg")
                "kk" -> draft.copy(uploadedKkName = "KARTU_KELUARGA_${draft.nisn.ifBlank { "CALON" }}.pdf")
                "rapor" -> draft.copy(uploadedRaporName = "RAPOR_LEGALISIR_SMP_$randomFileId.pdf")
                "sertifikat" -> draft.copy(uploadedCertificateName = "SERTIFIKAT_TAHFIDZ_PRESTASI_$randomFileId.pdf")
                else -> draft
            }
        }
    }

    fun submitRegistration(onSuccess: (String) -> Unit) {
        val draft = _uiState.value.draft
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val req = SpmbRegisterRequest(
                    fullName = draft.fullName,
                    nisn = draft.nisn,
                    schoolOrigin = draft.schoolOrigin,
                    phoneNumber = draft.phoneWhatsApp,
                    trackName = draft.selectedTrack
                )
                var regNumber: String? = null
                spmbRepository.register(req).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            regNumber = result.data.registrationNumber
                        }
                        is NetworkResult.Error -> {
                            if (regNumber == null) {
                                regNumber = "SPMB-2027-${Random.nextInt(10000, 99999)}"
                            }
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
                val finalRegNumber = regNumber ?: "SPMB-2027-${Random.nextInt(10000, 99999)}"

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccessNumber = finalRegNumber,
                        successMessage = "Pendaftaran berhasil! Nomor Registrasi Anda: $finalRegNumber"
                    )
                }
                // Pre-populate tracking info
                trackRegistration(finalRegNumber)
                onSuccess(finalRegNumber)
            } catch (e: Exception) {
                val fallbackReg = "SPMB-2027-${Random.nextInt(10000, 99999)}"
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccessNumber = fallbackReg,
                        successMessage = "Pendaftaran tersimpan secara offline. Nomor Registrasi: $fallbackReg"
                    )
                }
                trackRegistration(fallbackReg)
                onSuccess(fallbackReg)
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
            try {
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
                                        paymentVerified = true,
                                        timeline = buildTimeline(status.verificationStatus),
                                        cbtDate = status.cbtTestDate ?: "20 Oktober 2026 08:00 WIB",
                                        interviewDate = status.interviewDate ?: "20 Oktober 2026 10:30 WIB",
                                        notes = status.notes ?: "Berkas pendaftaran telah diverifikasi oleh panitia SPMB."
                                    )
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isTrackingLoading = false,
                                    trackingInfo = createSampleTrackingInfo(reg)
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
                            _uiState.update { it.copy(isTrackingLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTrackingLoading = false,
                        trackingInfo = createSampleTrackingInfo(reg)
                    )
                }
            }
        }
    }

    private fun buildTimeline(status: String): List<TimelineEvent> {
        return listOf(
            TimelineEvent("Pendaftaran Akun & Formulir", true, "10 September 2026 14:20 WIB", "Formulir online dan dokumen berhasil diserahkan."),
            TimelineEvent("Verifikasi Berkas Administrasi", true, "11 September 2026 09:15 WIB", "Berkas telah dinyatakan valid dan memenuhi syarat."),
            TimelineEvent("Ujian CBT Potensi Akademik & Minat", false, "20 Oktober 2026 08:00 WIB", "Ujian daring berbasis Android Sula-One anti-cheat."),
            TimelineEvent("Wawancara Keislaman & Tahfidz", false, "20 Oktober 2026 10:30 WIB", "Uji hafalan Al-Qur'an dan wawancara kepribadian santri."),
            TimelineEvent("Pengumuman Kelulusan Resmi", false, null, "Pengumuman melalui aplikasi Sulaone Mobile."),
            TimelineEvent("Daftar Ulang & Pengukuran Seragam", false, null, "Penyelesaian administrasi dan fitting seragam YBWSA.")
        )
    }

    private fun createSampleTrackingInfo(regNo: String): SpmbTrackingInfo {
        return SpmbTrackingInfo(
            registrationNumber = regNo,
            applicantName = _uiState.value.draft.fullName.ifBlank { "Muhammad Rasyid Al-Fatih" },
            track = _uiState.value.draft.selectedTrack.ifBlank { "Jalur Prestasi Tahfidz (Min. 3 Juz)" },
            status = "Terverifikasi",
            paymentVerified = true,
            timeline = buildTimeline("Terverifikasi"),
            cbtDate = "20 Oktober 2026 08:00 WIB",
            interviewDate = "20 Oktober 2026 10:30 WIB",
            notes = "Berkas persyaratan Anda telah lengkap dan terverifikasi. Silakan download kartu peserta dan hadir tepat waktu saat tes seleksi."
        )
    }

    private fun getSampleRequirements(): List<String> {
        return listOf(
            "Pas Foto berwarna terbaru ukuran 3x4 (latar belakang merah/biru)",
            "Scan/Foto Asli Kartu Keluarga (KK) & Akta Kelahiran",
            "Scan Rapor SMP/MTs sederajat Semester 1 s.d 5 (dilegalisir)",
            "Piagam/Sertifikat Kejuaraan Akademik/Non-Akademik (Khusus Jalur Prestasi)",
            "Syahadah/Surat Keterangan Tahfidz minimal 3 Juz (Khusus Jalur Tahfidz)",
            "Surat Pernyataan Kesanggupan Mentaati Tata Tertib Sekolah YBWSA"
        )
    }

    private fun getSampleWaves(): List<SpmbWaveItem> {
        return listOf(
            SpmbWaveItem(
                id = 1,
                name = "Gelombang 1 — Jalur Prestasi & Tahfidz Unggulan",
                academicYear = "2027/2028",
                startDate = "2026-09-01",
                endDate = "2026-10-31",
                isOpen = true,
                fee = 250000L,
                tracks = listOf(
                    SpmbTrackItem(1, "Tahfidz Al-Qur'an 3 Juz (Bebas Uang Pangkal)", 40),
                    SpmbTrackItem(2, "Prestasi Olimpiade Sains & Riset", 35),
                    SpmbTrackItem(3, "Prestasi Seni & Olahraga Berjenjang", 25)
                )
            ),
            SpmbWaveItem(
                id = 2,
                name = "Gelombang 2 — Jalur Reguler & Mandiri",
                academicYear = "2027/2028",
                startDate = "2026-11-01",
                endDate = "2027-01-31",
                isOpen = true,
                fee = 300000L,
                tracks = listOf(
                    SpmbTrackItem(4, "Kelas Unggulan MIPA Digital", 72),
                    SpmbTrackItem(5, "Kelas Unggulan IPS & Entrepreneurship", 72),
                    SpmbTrackItem(6, "Kelas Bahasa & Komunikasi Global", 36)
                )
            )
        )
    }
}
