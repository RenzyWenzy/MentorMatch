package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val user: UserResponse
)
