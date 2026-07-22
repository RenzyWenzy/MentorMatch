package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TutorProfileRequest(
    val bio: String? = null,
    val subjects: List<SubjectProficiency> = emptyList()
)
