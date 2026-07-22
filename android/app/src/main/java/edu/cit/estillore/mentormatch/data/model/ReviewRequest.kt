package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReviewRequest(
    val rating: Int,
    val comment: String? = null
)
