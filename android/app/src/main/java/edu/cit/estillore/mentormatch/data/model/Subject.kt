package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/** Mirrors SubjectController.java's Subject entity. */
@JsonClass(generateAdapter = true)
data class Subject(
    val id: Long,
    val name: String,
    val description: String? = null
)
