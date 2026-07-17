package edu.cit.estillore.mentormatch.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.AuthResponse
import edu.cit.estillore.mentormatch.data.model.RegistrationRequest
import edu.cit.estillore.mentormatch.data.model.Role
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val auth: AuthResponse) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password are required.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Login failed.") }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: Role,
        studentNumber: String? = null,
        program: String? = null,
        expertise: String? = null,
        department: String? = null
    ) {
        if (listOf(fullName, email, password, confirmPassword).any { it.isBlank() }) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val request = RegistrationRequest(
                fullName = fullName.trim(),
                email = email.trim(),
                password = password,
                confirmPassword = confirmPassword,
                role = role,
                studentNumber = if (role == Role.STUDENT) studentNumber else null,
                program = if (role == Role.STUDENT) program else null,
                expertise = if (role == Role.MENTOR) expertise else null,
                department = if (role == Role.MENTOR) department else null
            )
            authRepository.register(request)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Registration failed.") }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
