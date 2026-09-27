package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

// FASE 34: Kalender Akademik
data class AcademicCalendarEventItem(
    val id: Long,
    val title: String,
    val description: String? = null,
    val category: String = "kegiatan", // ujian, libur, keagamaan, dinas
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String? = null,
    val location: String? = null,
    @SerializedName("is_holiday") val isHoliday: Boolean = false
)

// FASE 35: SPMB / PPDB Mobile
data class SpmbTrackItem(
    val id: Long,
    val name: String,
    val quota: Int = 0
)

data class SpmbWaveItem(
    val id: Long,
    val name: String,
    @SerializedName("academic_year") val academicYear: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("is_open") val isOpen: Boolean = true,
    val fee: Long = 0,
    val tracks: List<SpmbTrackItem> = emptyList()
)

data class SpmbRegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val nisn: String,
    @SerializedName("school_origin") val schoolOrigin: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("track_name") val trackName: String? = null
)

data class SpmbRegistrationStatus(
    @SerializedName("registration_number") val registrationNumber: String,
    @SerializedName("full_name") val fullName: String,
    val track: String,
    @SerializedName("verification_status") val verificationStatus: String, // menunggu, terverifikasi, ditolak
    @SerializedName("cbt_test_date") val cbtTestDate: String? = null,
    @SerializedName("interview_date") val interviewDate: String? = null,
    @SerializedName("final_status") val finalStatus: String? = null, // diterima, cadangan, tidak_lulus
    val notes: String? = null
)

// FASE 37: Rekam Medis UKS
data class UksMedicineAdministered(
    val name: String,
    val dose: String
)

data class UksRecordVisitItem(
    val id: Long,
    @SerializedName("visit_time") val visitTime: String,
    val complaints: String,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val temperature: String? = null,
    @SerializedName("blood_pressure") val bloodPressure: String? = null,
    val medicines: List<UksMedicineAdministered> = emptyList(),
    val action: String? = "kembali_ke_kelas",
    @SerializedName("handler_name") val handlerName: String? = null,
    @SerializedName("parent_notified") val parentNotified: Boolean = false
)

/**
 * Every field but studentId is nullable — a student with no screening
 * result yet, or no blood type recorded, gets a real null rather than a
 * fabricated placeholder value.
 */
data class HealthScreeningData(
    @SerializedName("student_id") val studentId: Long,
    @SerializedName("student_name") val studentName: String? = null,
    val nisn: String? = null,
    @SerializedName("blood_type") val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    @SerializedName("height_cm") val heightCm: Int? = null,
    @SerializedName("weight_kg") val weightKg: Int? = null,
    val bmi: Double? = null,
    @SerializedName("bmi_category") val bmiCategory: String? = null,
    @SerializedName("vision_right") val visionRight: String? = null,
    @SerializedName("vision_left") val visionLeft: String? = null,
    @SerializedName("dental_health") val dentalHealth: String? = null,
    val hearing: String? = null,
    @SerializedName("last_screened_at") val lastScreenedAt: String? = null,
    val screener: String? = null
)

data class SchoolOpsApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
