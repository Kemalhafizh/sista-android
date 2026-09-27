package com.sultanagung1.sista.data.model

data class UksVisit(
    val id: String,
    val studentName: String,
    val className: String = "XI-A 1",
    val time: String,
    val complaint: String,
    val action: String,
    val temperature: Float? = 36.8f,
    val bloodPressure: String? = "110/70",
    val diagnosis: String? = null,
    val medicines: String? = null,
    val notes: String? = null,
    val officer: String = "Petugas UKS & PMR"
)

data class MedicineItem(
    val id: String,
    val name: String,
    val quantity: Int
)

data class UksVisitRequest(
    val studentId: String,
    val complaint: String,
    val temperature: Float,
    val diagnosis: String,
    val actions: List<String>,
    val medicines: List<MedicineItem>,
    val notes: String
)

data class HealthRecord(
    val id: String,
    val date: String,
    val complaint: String,
    val diagnosis: String,
    val action: String,
    val medicines: String,
    val officer: String = "dr. Hj. Siti Aminah / Tim Medis UKS"
)

data class HealthScreeningSummary(
    val studentName: String,
    val nisn: String,
    val bloodType: String,
    val heightCm: Int,
    val weightKg: Int,
    val bmi: Double,
    val bmiCategory: String,
    val visionRight: String,
    val visionLeft: String,
    val dentalHealth: String,
    val hearing: String,
    val allergies: List<String>,
    val lastScreenedAt: String,
    val screener: String
)
