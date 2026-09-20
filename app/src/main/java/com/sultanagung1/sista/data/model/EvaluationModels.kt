package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class TeacherEvaluationItem(
    @SerializedName("id") val id: Long,
    @SerializedName("teacher_name") val teacherName: String, // "Drs. H. Ahmad Fauzi, M.Pd"
    @SerializedName("subject_name") val subjectName: String, // "Matematika Peminatan"
    @SerializedName("class_name") val className: String, // "XII MIPA 1"
    @SerializedName("is_submitted") val isSubmitted: Boolean = false,
    @SerializedName("pedagogy_rating") val pedagogyRating: Int = 0,
    @SerializedName("punctuality_rating") val punctualityRating: Int = 0,
    @SerializedName("islamic_manner_rating") val islamicMannerRating: Int = 0
)

data class FacilitySurveyItem(
    @SerializedName("id") val id: Long,
    @SerializedName("facility_name") val facilityName: String, // "Kebersihan & Kenyamanan Toilet Siswa", "Konektivitas Wi-Fi & Smart Classroom", "Kualitas Makanan Kantin Halal"
    @SerializedName("satisfaction_level") val satisfactionLevel: Int = 0 // 1 to 5
)

data class OsisCandidateItem(
    @SerializedName("id") val id: Long,
    @SerializedName("number") val number: Int, // 1, 2, 3
    @SerializedName("president_name") val presidentName: String,
    @SerializedName("vice_president_name") val vicePresidentName: String,
    @SerializedName("vision") val vision: String,
    @SerializedName("mission") val mission: String,
    @SerializedName("total_votes") val totalVotes: Int = 0,
    @SerializedName("is_voted") val isVoted: Boolean = false
)

data class SubmitEvaluationRequest(
    @SerializedName("evaluation_id") val evaluationId: Long,
    @SerializedName("pedagogy") val pedagogy: Int,
    @SerializedName("punctuality") val punctuality: Int,
    @SerializedName("islamic_manner") val islamicManner: Int,
    @SerializedName("anonymous_feedback") val anonymousFeedback: String
)

data class CastVoteRequest(
    @SerializedName("candidate_id") val candidateId: Long,
    @SerializedName("voter_hash") val voterHash: String
)
