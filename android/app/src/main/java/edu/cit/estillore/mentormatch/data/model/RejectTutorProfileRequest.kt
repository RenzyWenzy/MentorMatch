package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RejectTutorProfileRequest(
    val reason: String? = null
)
