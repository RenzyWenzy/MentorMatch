package edu.cit.estillore.mentormatch.data.api

import com.squareup.moshi.Moshi
import edu.cit.estillore.mentormatch.data.model.ApiError
import retrofit2.Response

/**
 * Extracts the human-readable message from a failed response's body, which
 * GlobalExceptionHandler.java shapes as an ApiError (message + optional
 * per-field validation errors).
 */
object ErrorParser {

    private val moshi = Moshi.Builder().build()

    fun parse(response: Response<*>): String {
        val raw = response.errorBody()?.string()
        if (raw.isNullOrBlank()) return "Something went wrong (HTTP ${response.code()})."

        return try {
            val error = moshi.adapter(ApiError::class.java).fromJson(raw)
            when {
                error?.fieldErrors?.isNotEmpty() == true ->
                    error.fieldErrors.values.joinToString("\n")
                !error?.message.isNullOrBlank() -> error!!.message!!
                else -> "Something went wrong (HTTP ${response.code()})."
            }
        } catch (e: Exception) {
            "Something went wrong (HTTP ${response.code()})."
        }
    }
}
