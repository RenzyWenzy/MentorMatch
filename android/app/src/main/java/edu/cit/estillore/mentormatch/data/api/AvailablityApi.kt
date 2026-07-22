package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.AvailabilityRequest
import edu.cit.estillore.mentormatch.data.model.AvailabilitySlot
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** Matches AvailabilityController.java. */
interface AvailabilityApi {

    @GET("api/availability/tutor-profile/{id}")
    suspend fun forTutorProfile(@Path("id") tutorProfileId: Long): Response<List<AvailabilitySlot>>

    @PUT("api/availability/me")
    suspend fun replaceMine(@Body request: AvailabilityRequest): Response<List<AvailabilitySlot>>
}