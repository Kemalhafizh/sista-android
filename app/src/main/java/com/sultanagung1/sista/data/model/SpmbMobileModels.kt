package com.sultanagung1.sista.data.model

data class SpmbInfo(
    val batchName: String,
    val isActive: Boolean,
    val fee: Long,
    val requirements: List<String>,
    val waves: List<SpmbWaveItem> = emptyList()
)

data class SpmbRegistrationResult(
    val registrationNumber: String,
    val success: Boolean,
    val message: String
)

data class TimelineEvent(
    val step: String,
    val isCompleted: Boolean,
    val timestamp: String?,
    val description: String? = null
)

data class SpmbTrackingInfo(
    val registrationNumber: String,
    val applicantName: String,
    val track: String,
    val status: String,
    val paymentVerified: Boolean,
    val timeline: List<TimelineEvent>,
    val cbtDate: String? = null,
    val interviewDate: String? = null,
    val notes: String? = null
)

data class SpmbRegistrationDraft(
    val fullName: String = "",
    val nisn: String = "",
    val birthPlaceDate: String = "",
    val gender: String = "Laki-laki",
    val religion: String = "Islam",
    val schoolOrigin: String = "",
    val avgScore: String = "",
    val fatherName: String = "",
    val fatherJob: String = "",
    val motherName: String = "",
    val motherJob: String = "",
    val phoneWhatsApp: String = "",
    val address: String = "",
    val selectedTrack: String = "Jalur Prestasi Tahfidz (Min. 3 Juz)",
    val uploadedPhotoName: String? = null,
    val uploadedKkName: String? = null,
    val uploadedRaporName: String? = null,
    val uploadedCertificateName: String? = null
)
