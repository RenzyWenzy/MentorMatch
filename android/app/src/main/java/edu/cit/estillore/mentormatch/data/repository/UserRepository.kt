package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.UserApi
import edu.cit.estillore.mentormatch.data.model.UserResponse

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
}
