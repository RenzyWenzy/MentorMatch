package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * Mirrors ApiError.java. fieldErrors is populated for @Valid failures
 * (e.g. registration form field errors), null for simple message-only errors
 * such as duplicate email or bad credentials.
 */
@JsonClass(generateAdapter = true)
data class ApiError(
    val message: String?,
    val fieldErrors: Map<String, String>? = null
)
