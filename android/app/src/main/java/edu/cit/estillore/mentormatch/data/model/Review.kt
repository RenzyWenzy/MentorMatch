package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/** Mirrors ReviewController.java. */
@JsonClass(generateAdapter = true)
data class Review(
    val id: Long,
    val studentName: String? = null,
    val rating: Int,
    val comment: String? = null
)
