package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.AuthResponse
import edu.cit.estillore.mentormatch.data.model.LoginRequest
import edu.cit.estillore.mentormatch.data.model.RegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Matches AuthController.java — both endpoints are permitAll on the backend. */
interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegistrationRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
