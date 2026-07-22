package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.SubjectRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

/** Matches SubjectController.java. Create/update/delete are ADMIN-only server-side. */
interface SubjectApi {

    @GET("api/subjects")
    suspend fun listSubjects(): Response<List<Subject>>

    @POST("api/subjects")
    suspend fun createSubject(@Body request: SubjectRequest): Response<Subject>

    @PUT("api/subjects/{id}")
    suspend fun updateSubject(@Path("id") id: Long, @Body request: SubjectRequest): Response<Subject>

    @DELETE("api/subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: Long): Response<Unit>
}