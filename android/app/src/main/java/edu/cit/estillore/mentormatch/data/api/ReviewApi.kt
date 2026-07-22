package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.Review
import edu.cit.estillore.mentormatch.data.model.ReviewRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Matches ReviewController.java. */
interface ReviewApi {

    @POST("api/bookings/{bookingId}/review")
    suspend fun submit(@Path("bookingId") bookingId: Long, @Body request: ReviewRequest): Response<Review>

    @GET("api/tutor-profiles/{tutorProfileId}/reviews")
    suspend fun forTutorProfile(@Path("tutorProfileId") tutorProfileId: Long): Response<List<Review>>
}