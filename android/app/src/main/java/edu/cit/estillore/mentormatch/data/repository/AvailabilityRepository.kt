package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.AvailabilityApi
import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.model.AvailabilityRequest
import edu.cit.estillore.mentormatch.data.model.AvailabilitySlot

class AvailabilityRepository(private val availabilityApi: AvailabilityApi) {

    suspend fun forTutorProfile(tutorProfileId: Long): Result<List<AvailabilitySlot>> = runCatching {
        val response = availabilityApi.forTutorProfile(tutorProfileId)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun replaceMine(slots: List<AvailabilitySlot>): Result<List<AvailabilitySlot>> = runCatching {
        val response = availabilityApi.replaceMine(AvailabilityRequest(slots))
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }
}