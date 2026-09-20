package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ChildProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("nisn") val nisn: String,
    @SerializedName("classroom") val classroom: String,
    @SerializedName("attendance_today") val attendanceToday: String,
    @SerializedName("current_gpa") val currentGpa: Double,
    @SerializedName("unpaid_billings_count") val unpaidBillingsCount: Int
)

data class TeacherClassroom(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("grade_level") val gradeLevel: String,
    @SerializedName("total_students") val totalStudents: Int
)

data class StudentRosterItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("nisn") val nisn: String,
    var attendanceStatus: String = "H" // H, S, I, A
)

data class BlockchainCredentialItem(
    @SerializedName("id") val id: Long,
    @SerializedName("certificate_number") val certificateNumber: String,
    @SerializedName("title") val title: String,
    @SerializedName("issuer") val issuer: String,
    @SerializedName("issue_date") val issueDate: String,
    @SerializedName("blockchain_tx_hash") val txHash: String,
    @SerializedName("is_verified") val isVerified: Boolean
)

data class EnterpriseModuleItem(
    val id: String,
    val phaseNumber: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val route: String,
    val iconName: String,
    val isReady: Boolean = true
)
