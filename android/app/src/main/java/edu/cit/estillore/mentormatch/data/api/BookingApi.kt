package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.Booking
import edu.cit.estillore.mentormatch.data.model.BookingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Matches BookingController.java. */
interface BookingApi {

    @POST("api/bookings")
    suspend fun create(@Body request: BookingRequest): Response<Booking>

    @GET("api/bookings/me")
    suspend fun myBookingsAsStudent(): Response<List<Booking>>

    @GET("api/bookings/tutor/me")
    suspend fun myBookingsAsTutor(): Response<List<Booking>>

    @PUT("api/bookings/{id}/confirm")
    suspend fun confirm(@Path("id") id: Long): Response<Booking>

    @PUT("api/bookings/{id}/decline")
    suspend fun decline(@Path("id") id: Long): Response<Booking>

    @PUT("api/bookings/{id}/cancel")
    suspend fun cancel(@Path("id") id: Long): Response<Booking>

    @PUT("api/bookings/{id}/complete")
    suspend fun complete(@Path("id") id: Long): Response<Booking>
}