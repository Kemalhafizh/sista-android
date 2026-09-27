package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ProfileBiodata(
    val id: Long = 0L,
    val name: String = "",
    val nisn: String? = null,
    @SerializedName("class_name") val className: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val email: String? = null
)

data class ProfileAcademicSummary(
    @SerializedName("average_score") val averageScore: Double = 0.0,
    @SerializedName("rank_in_class") val rankInClass: Int = 0,
    @SerializedName("total_class_students") val totalClassStudents: Int = 0,
    @SerializedName("strongest_subject") val strongestSubject: String? = null,
    @SerializedName("improvement_needed") val improvementNeeded: String? = null,
    val trend: List<Double> = emptyList()
)

data class ProfileIbadahSummary(
    @SerializedName("tahfidz_juz_completed") val tahfidzJuzCompleted: Int = 0,
    @SerializedName("target_juz") val targetJuz: Int = 0,
    @SerializedName("tahfidz_progress_percent") val tahfidzProgressPercent: Int = 0,
    @SerializedName("current_surah") val currentSurah: String? = null,
    @SerializedName("mutabaah_weekly_streak") val mutabaahWeeklyStreak: Int = 0,
    @SerializedName("sholat_jamaah_percent") val sholatJamaahPercent: Double = 0.0
)

data class ProfileDisciplineSummary(
    @SerializedName("total_positive_points") val totalPositivePoints: Int = 0,
    @SerializedName("total_violation_points") val totalViolationPoints: Int = 0,
    @SerializedName("net_points") val netPoints: Int = 0,
    val category: String = "Aman",
    @SerializedName("active_sanctions") val activeSanctions: Int = 0
)

data class ProfileExtracurricularItem(
    val name: String,
    val role: String,
    @SerializedName("joined_year") val joinedYear: String? = null
)

data class ProfileAchievementItem(
    val title: String,
    val level: String,
    val year: Int? = null,
    val category: String? = null
)

data class ProfileHealthSummary(
    @SerializedName("blood_type") val bloodType: String? = null,
    @SerializedName("height_cm") val heightCm: Double? = null,
    @SerializedName("weight_kg") val weightKg: Double? = null,
    val allergies: List<String> = emptyList(),
    @SerializedName("total_uks_visits") val totalUksVisits: Int = 0,
    @SerializedName("last_visit_date") val lastVisitDate: String? = null
)

data class StudentProfile360Data(
    val biodata: ProfileBiodata = ProfileBiodata(),
    @SerializedName("akademik_summary") val academicSummary: ProfileAcademicSummary = ProfileAcademicSummary(),
    @SerializedName("ibadah_summary") val ibadahSummary: ProfileIbadahSummary = ProfileIbadahSummary(),
    @SerializedName("disiplin_summary") val disciplineSummary: ProfileDisciplineSummary = ProfileDisciplineSummary(),
    @SerializedName("ekskul_list") val extracurricularList: List<ProfileExtracurricularItem> = emptyList(),
    @SerializedName("prestasi_list") val achievementList: List<ProfileAchievementItem> = emptyList(),
    @SerializedName("kesehatan_summary") val healthSummary: ProfileHealthSummary = ProfileHealthSummary()
)

data class StudentProfileApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
