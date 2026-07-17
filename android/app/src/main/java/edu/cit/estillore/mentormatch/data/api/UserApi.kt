package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Matches UserController.java. All of these require a valid Bearer token
 * (added automatically by AuthInterceptor); role-specific routes are also
 * enforced server-side in SecurityConfig, so a wrong-role call will come
 * back as 403 regardless of what the app does.
 */
interface UserApi {

    @GET("api/users/me")
    suspend fun me(): Response<UserResponse>

    @GET("api/users/dashboard/student")
    suspend fun studentDashboard(): Response<UserResponse>

    @GET("api/users/dashboard/mentor")
    suspend fun mentorDashboard(): Response<UserResponse>

    @GET("api/users/dashboard/admin")
    suspend fun adminDashboard(): Response<UserResponse>

    @GET("api/users")
    suspend fun listUsers(): Response<List<UserResponse>>
}
