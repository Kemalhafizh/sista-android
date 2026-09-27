package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * GET documents/reports (ReportCardPdf) and documents/certificates
 * (Achievement with a certificate_path) — mirrors DocumentMobileApiController
 * exactly. Both endpoints only return data for the student the row belongs
 * to; other roles get an empty list rather than someone else's documents.
 */
data class SchoolDocumentItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String? = null, // reports: "academic_report"
    @SerializedName("issuer") val issuer: String? = null, // certificates only
    @SerializedName("level") val level: String? = null, // certificates only
    @SerializedName("semester") val semester: String? = null, // reports only
    @SerializedName("academic_year") val academicYear: String? = null, // reports only
    @SerializedName("date") val date: String? = null,
    @SerializedName("sha256_hash") val sha256Hash: String? = null, // reports only
    @SerializedName("verification_url") val verificationUrl: String? = null, // reports only
    @SerializedName("file_url") val fileUrl: String? = null
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

/** POST documents/ocr/scan response — the job is queued, so this is only an ack, not a result. */
data class OcrScanStartResponse(
    @SerializedName("task_id") val taskId: Long,
    @SerializedName("status") val status: String
)

/**
 * GET documents/ocr/{taskId} response. status is "pending" | "processing" |
 * "completed" | "failed" (OcrResult model). extracted_text is only present
 * once status is "completed" — the backend's OCR step (ProcessOcrDocument
 * job) is itself a simulated placeholder pending a real OCR provider
 * integration (Tesseract / Google Cloud Vision), not real text recognition,
 * so this text is honestly a stand-in even once the pipeline around it is
 * fully real.
 */
data class OcrScanResult(
    @SerializedName("task_id") val taskId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("extracted_text") val extractedText: String? = null
)

/** POST documents/signature/submit request — mirrors DigitalSignature exactly. */
data class SignatureSubmission(
    @SerializedName("document_type") val documentType: String,
    @SerializedName("document_id") val documentId: Long,
    @SerializedName("signature_data") val signatureData: String
)
