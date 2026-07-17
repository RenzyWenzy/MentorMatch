package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * Mirrors RegistrationRequest.java. studentNumber/program are only meaningful
 * for Role.STUDENT; expertise/department only for Role.MENTOR. The backend
 * validates this conditionally, so we just send whichever pair is relevant
 * and leave the other pair null.
 */
@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: Role,
    val studentNumber: String? = null,
    val program: String? = null,
    val expertise: String? = null,
    val department: String? = null
)
