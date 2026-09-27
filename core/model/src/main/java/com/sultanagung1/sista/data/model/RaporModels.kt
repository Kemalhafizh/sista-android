package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class RaporStudentData(
    val id: Long,
    val name: String,
    // Null when this user has no linked Student profile row yet — the
    // backend no longer fabricates a placeholder NISN/class for everyone.
    val nisn: String? = null,
    @SerializedName("class_name") val className: String? = null
)

data class RaporAcademicSummary(
    @SerializedName("average_score") val averageScore: Double,
    @SerializedName("predikat_umum") val predikatUmum: String,
    @SerializedName("total_subjects") val totalSubjects: Int
)

data class RaporEntryItem(
    val id: Long,
    @SerializedName("subject_name") val subjectName: String? = null,
    val score: Double,
    val predikat: String,
    @SerializedName("capaian_kompetensi") val capaianKompetensi: String? = null,
    @SerializedName("catatan_guru") val catatanGuru: String? = null
)

data class RaporCharacterItem(
    val id: Long,
    val dimension: String,
    val grade: String,
    val description: String? = null
)

data class RaporPdfStatus(
    @SerializedName("is_ready") val isReady: Boolean,
    @SerializedName("file_path") val filePath: String? = null,
    @SerializedName("sha256_hash") val sha256Hash: String? = null,
    @SerializedName("generated_at") val generatedAt: String? = null
)

data class RaporDetailData(
    val student: RaporStudentData,
    @SerializedName("academic_summary") val academicSummary: RaporAcademicSummary,
    val entries: List<RaporEntryItem> = emptyList(),
    val characters: List<RaporCharacterItem> = emptyList(),
    @SerializedName("pdf_status") val pdfStatus: RaporPdfStatus
)

data class RaporResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T
)
