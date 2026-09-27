package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class UksVisit(
    val id: String,
    val studentName: String,
    val className: String? = null,
    val time: String,
    val complaint: String,
    val action: String,
    val temperature: Float? = null,
    val bloodPressure: String? = null,
    val diagnosis: String? = null,
    val medicines: String? = null,
    val notes: String? = null,
    val officer: String? = null,
    val parentNotified: Boolean = false
)

data class MedicineItem(
    val id: String,
    val name: String,
    val quantity: Int
)

data class UksVisitRequest(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("complaint") val complaint: String,
    @SerializedName("temperature") val temperature: Float?,
    @SerializedName("blood_pressure") val bloodPressure: String?,
    @SerializedName("diagnosis") val diagnosis: String?,
    @SerializedName("actions") val actions: List<String>,
    @SerializedName("medicines") val medicines: List<UksMedicineSelection>,
    @SerializedName("notes") val notes: String?
)

/** [id] is the real uks_medicines.id (as a string, matching MedicineItem). */
data class UksMedicineSelection(
    val id: String,
    val quantity: Int = 1
)

data class UksStudentSearchItem(
    val id: Long,
    val name: String,
    val nisn: String?,
    @SerializedName("classroom_name") val classroomName: String?
)

data class HealthRecord(
    val id: String,
    val date: String,
    val complaint: String,
    val diagnosis: String?,
    val action: String,
    val medicines: String,
    val officer: String?
)

/**
 * All fields nullable except identity — a real student may genuinely have
 * no blood type on file, no screening result yet, etc. The previous version
 * of this class forced non-null with hardcoded fallback values wherever the
 * ViewModel couldn't get a real one.
 */
data class HealthScreeningSummary(
    val studentName: String?,
    val nisn: String?,
    val bloodType: String?,
    val heightCm: Int?,
    val weightKg: Int?,
    val bmi: Double?,
    val bmiCategory: String?,
    val visionRight: String?,
    val visionLeft: String?,
    val dentalHealth: String?,
    val hearing: String?,
    val allergies: List<String> = emptyList(),
    val lastScreenedAt: String?,
    val screener: String?
)
