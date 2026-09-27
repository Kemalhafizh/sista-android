package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ElearningClassItem(
    val id: Long,
    val name: String,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    val room: String? = null,
    @SerializedName("academic_year") val academicYear: String? = null,
    @SerializedName("materials_count") val materialsCount: Int = 0,
    @SerializedName("assignments_count") val assignmentsCount: Int = 0
)

data class ElearningMaterialItem(
    val id: Long,
    @SerializedName("elearning_class_id") val classId: Long,
    val title: String,
    val content: String? = null,
    val type: String? = "document",
    @SerializedName("file_path") val filePath: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ElearningAssignmentItem(
    val id: Long,
    @SerializedName("elearning_class_id") val classId: Long,
    val title: String,
    val description: String? = null,
    @SerializedName("due_at") val dueAt: String,
    @SerializedName("allow_late") val allowLate: Boolean = true,
    @SerializedName("is_submitted") val isSubmitted: Boolean = false,
    val submission: ElearningSubmissionItem? = null
)

data class ElearningSubmissionItem(
    val id: Long,
    @SerializedName("elearning_assignment_id") val assignmentId: Long,
    @SerializedName("student_id") val studentId: Long,
    val content: String? = null,
    @SerializedName("file_path") val filePath: String? = null,
    @SerializedName("submitted_at") val submittedAt: String? = null,
    val score: Int? = null,
    val feedback: String? = null,
    @SerializedName("is_late") val isLate: Boolean = false,
    @SerializedName("student_name") val studentName: String? = null
)

data class CreateMaterialRequest(
    val title: String,
    val content: String? = null,
    val type: String? = "document"
)

data class CreateAssignmentRequest(
    val title: String,
    val description: String? = null,
    @SerializedName("due_at") val dueAt: String,
    @SerializedName("allow_late") val allowLate: Boolean = true
)

data class GradeSubmissionRequest(
    val score: Int,
    val feedback: String? = null
)

data class ElearningApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// A real picked file's bytes, held here (pure Kotlin, no android.net.Uri)
// so :core:model stays free of Android framework dependencies; the actual
// ContentResolver read happens in the Screen composable.
data class ElearningAttachment(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String
)
