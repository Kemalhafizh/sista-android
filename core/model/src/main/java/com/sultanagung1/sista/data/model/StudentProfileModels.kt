package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ProfileBiodata(
    val id: Long = 1L,
    val name: String = "Ahmad Faiz Abdullah",
    val nisn: String = "1058291048",
    @SerializedName("class_name") val className: String = "XII MIPA 1",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val email: String = "faiz@sultanagung1.sch.id"
)

data class ProfileAcademicSummary(
    @SerializedName("average_score") val averageScore: Double = 88.4,
    @SerializedName("rank_in_class") val rankInClass: Int = 3,
    @SerializedName("total_class_students") val totalClassStudents: Int = 36,
    @SerializedName("strongest_subject") val strongestSubject: String = "Pendidikan Agama Islam",
    @SerializedName("improvement_needed") val improvementNeeded: String = "Kimia",
    val trend: List<Double> = listOf(84.2, 86.0, 87.5, 88.4)
)

data class ProfileIbadahSummary(
    @SerializedName("tahfidz_juz_completed") val tahfidzJuzCompleted: Int = 3,
    @SerializedName("target_juz") val targetJuz: Int = 5,
    @SerializedName("tahfidz_progress_percent") val tahfidzProgressPercent: Int = 60,
    @SerializedName("current_surah") val currentSurah: String = "Al-Mulk (Ayat 1-30)",
    @SerializedName("mutabaah_weekly_streak") val mutabaahWeeklyStreak: Int = 18,
    @SerializedName("sholat_jamaah_percent") val sholatJamaahPercent: Double = 96.5
)

data class ProfileDisciplineSummary(
    @SerializedName("total_positive_points") val totalPositivePoints: Int = 45,
    @SerializedName("total_violation_points") val totalViolationPoints: Int = 5,
    @SerializedName("net_points") val netPoints: Int = 40,
    val category: String = "Teladan",
    @SerializedName("active_sanctions") val activeSanctions: Int = 0
)

data class ProfileExtracurricularItem(
    val name: String,
    val role: String,
    @SerializedName("joined_year") val joinedYear: String
)

data class ProfileAchievementItem(
    val title: String,
    val level: String,
    val year: Int,
    val category: String
)

data class ProfileHealthSummary(
    @SerializedName("blood_type") val bloodType: String = "O+",
    @SerializedName("height_cm") val heightCm: Int = 172,
    @SerializedName("weight_kg") val weightKg: Int = 63,
    val allergies: List<String> = listOf("Tidak ada riwayat alergi berat"),
    @SerializedName("total_uks_visits") val totalUksVisits: Int = 1,
    @SerializedName("last_visit_date") val lastVisitDate: String = "2026-08-14"
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
