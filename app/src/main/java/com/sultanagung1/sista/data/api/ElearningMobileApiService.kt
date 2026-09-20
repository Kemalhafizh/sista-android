package com.sultanagung1.sista.data.api

import com.sultanagung1.sista.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ElearningMobileApiService {

    @GET("elearning/classes/student")
    suspend fun getStudentClasses(): Response<ElearningApiResponse<List<ElearningClassItem>>>

    @GET("elearning/classes/{classId}/materials")
    suspend fun getClassMaterials(
        @Path("classId") classId: Long
    ): Response<ElearningApiResponse<List<ElearningMaterialItem>>>

    @GET("elearning/classes/{classId}/assignments")
    suspend fun getClassAssignments(
        @Path("classId") classId: Long
    ): Response<ElearningApiResponse<List<ElearningAssignmentItem>>>

    @Multipart
    @POST("elearning/assignments/{id}/submit")
    suspend fun submitAssignment(
        @Path("id") assignmentId: Long,
        @Part("content") content: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Response<ElearningApiResponse<ElearningSubmissionItem>>

    @GET("elearning/classes/teacher")
    suspend fun getTeacherClasses(): Response<ElearningApiResponse<List<ElearningClassItem>>>

    @POST("elearning/classes/{classId}/materials")
    suspend fun createMaterial(
        @Path("classId") classId: Long,
        @Body request: CreateMaterialRequest
    ): Response<ElearningApiResponse<ElearningMaterialItem>>

    @POST("elearning/classes/{classId}/assignments")
    suspend fun createAssignment(
        @Path("classId") classId: Long,
        @Body request: CreateAssignmentRequest
    ): Response<ElearningApiResponse<ElearningAssignmentItem>>

    @GET("elearning/assignments/{id}/submissions")
    suspend fun getSubmissions(
        @Path("id") assignmentId: Long
    ): Response<ElearningApiResponse<List<ElearningSubmissionItem>>>

    @POST("elearning/submissions/{id}/grade")
    suspend fun gradeSubmission(
        @Path("id") submissionId: Long,
        @Body request: GradeSubmissionRequest
    ): Response<ElearningApiResponse<ElearningSubmissionItem>>
}
