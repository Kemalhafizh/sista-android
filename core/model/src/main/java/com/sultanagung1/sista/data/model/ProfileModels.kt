package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * GET me (ApiAuthController::me). Who is signed in, as the school records
 * them. Every block the server leaves out is null: a student has no
 * [employeeData], a parent's [children] is a list (maybe empty), anyone else's
 * is null. Nothing here has a made-up default.
 */
data class MeProfile(
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("role_label") val roleLabel: String? = null,
    @SerializedName("student_data") val studentData: MeStudentData? = null,
    @SerializedName("employee_data") val employeeData: MeEmployeeData? = null,
    @SerializedName("children") val children: List<MeChild>? = null,
    @SerializedName("homeroom_classrooms") val homeroomClassrooms: List<MeClassroom>? = null,
    @SerializedName("academic_year") val academicYear: MeAcademicYear? = null,
)

data class MeStudentData(
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("nis") val nis: String? = null,
    @SerializedName("nisn") val nisn: String? = null,
    @SerializedName("classroom") val classroom: String? = null,
    @SerializedName("classroom_id") val classroomId: Long? = null,
)

data class MeEmployeeData(
    @SerializedName("employee_number") val employeeNumber: String? = null,
    @SerializedName("position") val position: String? = null,
)

data class MeChild(
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("classroom") val classroom: String? = null,
)

data class MeClassroom(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
)

data class MeAcademicYear(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("semester") val semester: String? = null,
)

/** mobile/config → "school": App\Support\School on the server. */
data class SchoolIdentity(
    @SerializedName("name") val name: String? = null,
    @SerializedName("short_name") val shortName: String? = null,
    @SerializedName("foundation") val foundation: String? = null,
    @SerializedName("npsn") val npsn: String? = null,
    @SerializedName("accreditation") val accreditation: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("website") val website: String? = null,
)
