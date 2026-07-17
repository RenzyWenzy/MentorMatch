package edu.cit.estillore.mentormatch.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mentor_match_prefs")

/**
 * Persists the JWT returned by /api/auth/login and /api/auth/register so
 * subsequent app launches stay signed in until the user logs out (or the
 * token expires, which the backend / JwtAuthenticationFilter will reject).
 */
class TokenManager(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("jwt_token")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }

    suspend fun currentToken(): String? = tokenFlow.first()

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[Keys.TOKEN] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(Keys.TOKEN) }
    }
}
