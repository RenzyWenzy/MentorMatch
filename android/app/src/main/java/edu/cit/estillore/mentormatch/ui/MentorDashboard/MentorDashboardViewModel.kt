package edu.cit.estillore.mentormatch.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.Booking
import edu.cit.estillore.mentormatch.data.model.Review
import edu.cit.estillore.mentormatch.data.model.TutorProfile
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import edu.cit.estillore.mentormatch.data.repository.BookingRepository
import edu.cit.estillore.mentormatch.data.repository.ReviewRepository
import edu.cit.estillore.mentormatch.data.repository.TutorProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MentorDashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val bookings: List<Booking> = emptyList(),
    val profile: TutorProfile? = null,
    val reviews: List<Review> = emptyList()
)

/** Android equivalent of MentorDashboard.jsx. */
class MentorDashboardViewModel(
    private val bookingRepository: BookingRepository,
    private val tutorProfileRepository: TutorProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorDashboardUiState())
    val uiState: StateFlow<MentorDashboardUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
        loadRatingInfo()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            bookingRepository.myBookingsAsTutor()
                .onSuccess { _uiState.value = _uiState.value.copy(loading = false, bookings = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "Failed to load bookings.") }
        }
    }

    private fun loadRatingInfo() {
        viewModelScope.launch {
            tutorProfileRepository.myProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(profile = profile)
                    if (profile != null) {
                        reviewRepository.forTutorProfile(profile.id).onSuccess {
                            _uiState.value = _uiState.value.copy(reviews = it)
                        }
                    }
                }
                .onFailure {
                    // No tutor profile yet — leave the rating section empty, same as the web dashboard.
                    _uiState.value = _uiState.value.copy(profile = null)
                }
        }
    }

    fun confirm(id: Long) = act { bookingRepository.confirm(id) }
    fun decline(id: Long) = act { bookingRepository.decline(id) }
    fun complete(id: Long) = act { bookingRepository.complete(id) }

    private fun act(action: suspend () -> Result<Booking>) {
        viewModelScope.launch {
            action()
                .onSuccess { loadBookings() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Action failed.") }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }

    class Factory(
        private val bookingRepository: BookingRepository,
        private val tutorProfileRepository: TutorProfileRepository,
        private val reviewRepository: ReviewRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MentorDashboardViewModel(bookingRepository, tutorProfileRepository, reviewRepository, authRepository) as T
        }
    }
}