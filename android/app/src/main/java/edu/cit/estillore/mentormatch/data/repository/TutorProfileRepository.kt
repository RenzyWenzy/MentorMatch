package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.TutorProfileApi
import edu.cit.estillore.mentormatch.data.model.RejectTutorProfileRequest
import edu.cit.estillore.mentormatch.data.model.SubjectProficiency
import edu.cit.estillore.mentormatch.data.model.TutorProfile
import edu.cit.estillore.mentormatch.data.model.TutorProfileRequest
import retrofit2.Response

class TutorProfileRepository(private val tutorProfileApi: TutorProfileApi) {

    /**
     * The web client treats HTTP 400 on GET /me as "no profile yet" rather
     * than a real error (see MentorProfileEdit.jsx). NoProfileYet lets the
     * UI distinguish that from an actual failure.
     */
    object NoProfileYet : Throwable("No tutor profile yet.")

    suspend fun myProfile(): Result<TutorProfile?> = runCatching {
        val response = tutorProfileApi.myProfile()
        when {
            response.isSuccessful -> response.body()
            response.code() == 400 -> throw NoProfileYet
            else -> throw IllegalStateException(ErrorParser.parse(response))
        }
    }

    suspend fun saveMyProfile(bio: String?, subjects: List<SubjectProficiency>): Result<TutorProfile> = runCatching {
        val response = tutorProfileApi.saveMyProfile(TutorProfileRequest(bio, subjects))
        unwrap(response)
    }

    suspend fun listAll(): Result<List<TutorProfile>> = runCatching {
        val response = tutorProfileApi.listAll()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    /** FR-004: pass null/blank for any filter you don't want applied. */
    suspend fun search(
        subjectId: Long? = null,
        dayOfWeek: String? = null,
        startTime: String? = null,
        endTime: String? = null
    ): Result<List<TutorProfile>> = runCatching {
        val response = tutorProfileApi.search(subjectId, dayOfWeek, startTime, endTime)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun pending(): Result<List<TutorProfile>> = runCatching {
        val response = tutorProfileApi.pending()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun approve(id: Long): Result<TutorProfile> = runCatching {
        unwrap(tutorProfileApi.approve(id))
    }

    suspend fun reject(id: Long, reason: String?): Result<TutorProfile> = runCatching {
        unwrap(tutorProfileApi.reject(id, RejectTutorProfileRequest(reason)))
    }

    private fun unwrap(response: Response<TutorProfile>): TutorProfile {
        if (response.isSuccessful) return response.body() ?: error("Empty response from server.")
        throw IllegalStateException(ErrorParser.parse(response))
    }
}