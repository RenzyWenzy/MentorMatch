package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.UserApi
import edu.cit.estillore.mentormatch.data.model.UserResponse
import retrofit2.Response

class UserRepository(private val userApi: UserApi) {

    suspend fun me(): Result<UserResponse> = runCatching {
        val response = userApi.me()
        if (response.isSuccessful) {
            response.body() ?: error("Empty response from server.")
        } else {
            throw IllegalStateException(ErrorParser.parse(response))
        }
    }

    suspend fun roleDashboard(role: edu.cit.estillore.mentormatch.data.model.Role): Result<UserResponse> =
        runCatching {
            val response = when (role) {
                edu.cit.estillore.mentormatch.data.model.Role.STUDENT -> userApi.studentDashboard()
                edu.cit.estillore.mentormatch.data.model.Role.MENTOR -> userApi.mentorDashboard()
                edu.cit.estillore.mentormatch.data.model.Role.ADMIN -> userApi.adminDashboard()
            }
            if (response.isSuccessful) {
                response.body() ?: error("Empty response from server.")
            } else {
                throw IllegalStateException(ErrorParser.parse(response))
            }
        }

    suspend fun listUsers(): Result<List<UserResponse>> = runCatching {
        val response = userApi.listUsers()
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw IllegalStateException(ErrorParser.parse(response))
        }
    }

    suspend fun getUser(id: Long): Result<UserResponse> = runCatching { unwrap(userApi.getUser(id)) }

    /** BR — ADMIN-only: re-enables a deactivated account. */
    suspend fun activateUser(id: Long): Result<UserResponse> = runCatching { unwrap(userApi.activateUser(id)) }

    /** BR — ADMIN-only: disables an account without deleting it. */
    suspend fun deactivateUser(id: Long): Result<UserResponse> = runCatching { unwrap(userApi.deactivateUser(id)) }

    /** BR — ADMIN-only: permanently removes an account. */
    suspend fun removeUser(id: Long): Result<Unit> = runCatching {
        val response = userApi.deleteUser(id)
        if (!response.isSuccessful) throw IllegalStateException(ErrorParser.parse(response))
    }

    private fun unwrap(response: Response<UserResponse>): UserResponse {
        if (response.isSuccessful) return response.body() ?: error("Empty response from server.")
        throw IllegalStateException(ErrorParser.parse(response))
    }
}