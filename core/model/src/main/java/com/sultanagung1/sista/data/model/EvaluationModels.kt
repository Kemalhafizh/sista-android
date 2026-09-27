package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

// The real GET evaluations/teachers only returns {id, name, subject} — no
// class name, no is_submitted flag, no per-aspect rating breakdown (the
// backend's anonymous evaluation model deliberately doesn't track
// per-student completion). EvaluationRepository maps this into
// TeacherEvaluationItem below; className/isSubmitted/aspect ratings stay
// at their honest defaults (blank/false/0) until the user actually rates
// this teacher in this session.
data class EvaluableTeacherItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("subject") val subject: String
)

data class TeacherEvaluationItem(
    val id: Long,
    val teacherName: String,
    val subjectName: String,
    val className: String? = null,
    val isSubmitted: Boolean = false,
    val pedagogyRating: Int = 0,
    val punctualityRating: Int = 0,
    val islamicMannerRating: Int = 0
)

// Real GET evaluations/facilities shape: {id, name, category, location} —
// no satisfaction level (that's the user's own not-yet-submitted rating).
data class EvaluableFacilityItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("location") val location: String? = null
)

data class FacilitySurveyItem(
    val id: Long,
    val facilityName: String,
    val satisfactionLevel: Int = 0, // the user's own locally-staged rating, 0 = not yet rated
    val isSubmitted: Boolean = false
)

data class OsisElectionInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("voting_start") val votingStart: String,
    @SerializedName("voting_end") val votingEnd: String,
    @SerializedName("candidates") val candidates: List<OsisCandidateItem> = emptyList()
)

data class OsisCandidateItem(
    @SerializedName("id") val id: Long,
    @SerializedName("candidate_number") val number: Int, // 1, 2, 3
    @SerializedName("president_name") val presidentName: String?,
    @SerializedName("vice_president_name") val vicePresidentName: String?,
    @SerializedName("vision") val vision: String,
    @SerializedName("mission") val mission: String,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("total_votes") val totalVotes: Int = 0,
    @SerializedName("is_voted") val isVoted: Boolean = false
)

// Matches EvaluationMobileApiController::submitTeacherEval's real
// validation exactly: teacher_id (must exist), rating 1-5, comments
// (nullable). The backend has no per-aspect (pedagogy/punctuality/manner)
// columns — those three UI sliders are averaged into [rating] by the
// repository, with the per-aspect breakdown folded into [comments] so it
// isn't silently dropped.
data class SubmitEvaluationRequest(
    @SerializedName("teacher_id") val teacherId: Long,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comments") val comments: String?
)

data class SubmitFacilityEvaluationRequest(
    @SerializedName("facility_id") val facilityId: Long,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comments") val comments: String? = null,
    @SerializedName("is_anonymous") val isAnonymous: Boolean = true
)

data class CastVoteRequest(
    @SerializedName("election_id") val electionId: Long,
    @SerializedName("candidate_id") val candidateId: Long,
    @SerializedName("biometric_signature") val biometricSignature: String
)
