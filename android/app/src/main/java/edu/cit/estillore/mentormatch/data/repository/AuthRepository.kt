package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.AuthApi
import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.local.TokenManager
import edu.cit.estillore.mentormatch.data.model.AuthResponse
import edu.cit.estillore.mentormatch.data.model.LoginRequest
import edu.cit.estillore.mentormatch.data.model.RegistrationRequest

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    suspend fun register(request: RegistrationRequest): Result<AuthResponse> =
        runCatching {
            val response = authApi.register(request)
            if (response.isSuccessful) {
                val body = response.body() ?: error("Empty response from server.")
                tokenManager.saveToken(body.token)
                body
            } else {
                throw IllegalStateException(ErrorParser.parse(response))
            }
        }

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        runCatching {
            val response = authApi.login(LoginRequest(email.trim(), password))
            if (response.isSuccessful) {
                val body = response.body() ?: error("Empty response from server.")
                tokenManager.saveToken(body.token)
                body
            } else {
                throw IllegalStateException(ErrorParser.parse(response))
            }
        }

    suspend fun logout() {
        tokenManager.clearToken()
    }

    suspend fun isLoggedIn(): Boolean = !tokenManager.currentToken().isNullOrBlank()
}
