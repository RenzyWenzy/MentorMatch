package edu.cit.estillore.mentormatch

import android.content.Context
import edu.cit.estillore.mentormatch.data.api.NetworkModule
import edu.cit.estillore.mentormatch.data.local.TokenManager
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import edu.cit.estillore.mentormatch.data.repository.UserRepository

/**
 * Lightweight hand-rolled dependency container (no Hilt/Dagger needed for
 * an app this size). Created once in MainActivity and threaded down to
 * the ViewModels via their factories.
 */
class AppContainer(context: Context) {

    val tokenManager = TokenManager(context.applicationContext)

    private val authApi = NetworkModule.provideAuthApi(tokenManager)
    private val userApi = NetworkModule.provideUserApi(tokenManager)

    val authRepository = AuthRepository(authApi, tokenManager)
    val userRepository = UserRepository(userApi)
}
