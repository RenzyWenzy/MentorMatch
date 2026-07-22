package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.ReviewApi
import edu.cit.estillore.mentormatch.data.model.Review
import edu.cit.estillore.mentormatch.data.model.ReviewRequest

class ReviewRepository(private val reviewApi: ReviewApi) {

    suspend fun submit(bookingId: Long, rating: Int, comment: String?): Result<Review> = runCatching {
        val response = reviewApi.submit(bookingId, ReviewRequest(rating, comment))
        if (response.isSuccessful) response.body() ?: error("Empty response from server.")
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun forTutorProfile(tutorProfileId: Long): Result<List<Review>> = runCatching {
        val response = reviewApi.forTutorProfile(tutorProfileId)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }
}