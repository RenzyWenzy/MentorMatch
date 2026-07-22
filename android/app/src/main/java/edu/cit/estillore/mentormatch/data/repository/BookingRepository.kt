package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.BookingApi
import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.model.Booking
import edu.cit.estillore.mentormatch.data.model.BookingRequest
import retrofit2.Response

class BookingRepository(private val bookingApi: BookingApi) {

    suspend fun create(request: BookingRequest): Result<Booking> = runCatching {
        unwrap(bookingApi.create(request))
    }

    suspend fun myBookingsAsStudent(): Result<List<Booking>> = runCatching {
        val response = bookingApi.myBookingsAsStudent()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun myBookingsAsTutor(): Result<List<Booking>> = runCatching {
        val response = bookingApi.myBookingsAsTutor()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun confirm(id: Long): Result<Booking> = runCatching { unwrap(bookingApi.confirm(id)) }
    suspend fun decline(id: Long): Result<Booking> = runCatching { unwrap(bookingApi.decline(id)) }
    suspend fun cancel(id: Long): Result<Booking> = runCatching { unwrap(bookingApi.cancel(id)) }
    suspend fun complete(id: Long): Result<Booking> = runCatching { unwrap(bookingApi.complete(id)) }

    private fun unwrap(response: Response<Booking>): Booking {
        if (response.isSuccessful) return response.body() ?: error("Empty response from server.")
        throw IllegalStateException(ErrorParser.parse(response))
    }
}