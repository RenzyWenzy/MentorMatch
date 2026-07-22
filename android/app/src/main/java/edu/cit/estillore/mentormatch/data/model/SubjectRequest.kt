package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubjectRequest(
    val name: String,
    val description: String? = null
)
