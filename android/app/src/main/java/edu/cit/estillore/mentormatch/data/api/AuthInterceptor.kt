package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds "Authorization: Bearer <token>" to every request, mirroring what
 * JwtAuthenticationFilter.java expects on the backend. Requests under
 * api slash auth are permitAll, but sending the header there is harmless
 * since there's no token yet before login/register.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.currentToken() }

        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
