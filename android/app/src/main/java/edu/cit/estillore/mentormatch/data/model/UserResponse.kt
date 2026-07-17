package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/** Mirrors UserResponseRequest.java returned by the backend. */
@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: Role,
    val studentNumber: String?,
    val program: String?,
    val expertise: String?,
    val department: String?
)
