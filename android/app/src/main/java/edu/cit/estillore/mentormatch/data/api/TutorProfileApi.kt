package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.RejectTutorProfileRequest
import edu.cit.estillore.mentormatch.data.model.TutorProfile
import edu.cit.estillore.mentormatch.data.model.TutorProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Matches TutorProfileController.java. Pending/approve/reject are ADMIN-only server-side. */
interface TutorProfileApi {

    @GET("api/tutor-profiles/me")
    suspend fun myProfile(): Response<TutorProfile>

    @PUT("api/tutor-profiles/me")
    suspend fun saveMyProfile(@Body request: TutorProfileRequest): Response<TutorProfile>

    @GET("api/tutor-profiles")
    suspend fun listAll(): Response<List<TutorProfile>>

    /** FR-004: subjectId/dayOfWeek/startTime/endTime are all optional filters. */
    @GET("api/tutor-profiles/search")
    suspend fun search(
        @Query("subjectId") subjectId: Long?,
        @Query("dayOfWeek") dayOfWeek: String?,
        @Query("startTime") startTime: String?,
        @Query("endTime") endTime: String?
    ): Response<List<TutorProfile>>

    @GET("api/tutor-profiles/pending")
    suspend fun pending(): Response<List<TutorProfile>>

    @PUT("api/tutor-profiles/{id}/approve")
    suspend fun approve(@Path("id") id: Long): Response<TutorProfile>

    @PUT("api/tutor-profiles/{id}/reject")
    suspend fun reject(@Path("id") id: Long, @Body request: RejectTutorProfileRequest): Response<TutorProfile>
}