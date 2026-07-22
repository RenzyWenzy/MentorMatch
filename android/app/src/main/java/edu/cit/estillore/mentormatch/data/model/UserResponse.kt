package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * Mirrors UserResponseRequest.java returned by the backend.
 * `active` was added to support AdminDashboard's activate/deactivate flow —
 * verify it's the actual field name on the backend DTO.
 */
@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: Role,
    val studentNumber: String?,
    val program: String?,
    val expertise: String?,
    val department: String?,
    val active: Boolean = true
)