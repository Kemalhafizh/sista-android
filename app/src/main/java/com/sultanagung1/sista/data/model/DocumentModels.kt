package com.sultanagung1.sista.data.model

data class SchoolDocumentItem(
    val id: String,
    val title: String,
    val category: String, // "Rapor", "Sertifikat", "Surat Edaran", "Modul Ajar"
    val academicYear: String,
    val semester: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val fileUrl: String,
    val issuedDate: String,
    val isSigned: Boolean = false,
    val signerName: String? = null
)

data class DownloadTaskItem(
    val id: String,
    val title: String,
    val fileType: String, // "PDF", "DOCX", "IMAGE", "AUDIO"
    val sizeBytes: Long,
    val progress: Float = 0f, // 0.0 to 1.0
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val localFilePath: String? = null,
    val downloadedAt: String = "Hari ini"
)

data class OcrScanResult(
    val scannedText: String,
    val confidence: Float,
    val detectedLanguage: String = "id",
    val extractedFields: Map<String, String> = emptyMap()
)

data class SignatureSubmission(
    val documentId: String,
    val signerRole: String, // "Guru", "Wali Murid", "Kepala Sekolah"
    val signerName: String,
    val signaturePngBase64: String,
    val signedTimestamp: Long = System.currentTimeMillis()
)
