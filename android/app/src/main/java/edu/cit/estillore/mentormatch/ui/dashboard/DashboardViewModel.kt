package edu.cit.estillore.mentormatch.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.Role
import edu.cit.estillore.mentormatch.data.model.UserResponse
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import edu.cit.estillore.mentormatch.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Loaded(val user: UserResponse) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun load(role: Role) {
        _uiState.value = DashboardUiState.Loading
        viewModelScope.launch {
            // The role-specific route is guarded server-side too (SecurityConfig),
            // so this doubles as a sanity check that the token's role still matches.
            userRepository.roleDashboard(role)
                .onSuccess { _uiState.value = DashboardUiState.Loaded(it) }
                .onFailure { _uiState.value = DashboardUiState.Error(it.message ?: "Failed to load dashboard.") }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(userRepository, authRepository) as T
        }
    }
}
