package edu.cit.estillore.mentormatch.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.Booking
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import edu.cit.estillore.mentormatch.data.repository.BookingRepository
import edu.cit.estillore.mentormatch.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentDashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val bookings: List<Booking> = emptyList(),
    val reviewSubmittingForBookingId: Long? = null,
    val reviewError: String? = null
)

/** Android equivalent of the booking-list portion of StudentDashboard.jsx (not uploaded). */
class StudentDashboardViewModel(
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDashboardUiState())
    val uiState: StateFlow<StudentDashboardUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            bookingRepository.myBookingsAsStudent()
                .onSuccess { _uiState.value = _uiState.value.copy(loading = false, bookings = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "Failed to load bookings.") }
        }
    }

    fun cancelBooking(id: Long) {
        viewModelScope.launch {
            bookingRepository.cancel(id)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Could not cancel booking.") }
        }
    }

    fun submitReview(bookingId: Long, rating: Int, comment: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(reviewSubmittingForBookingId = bookingId, reviewError = null)
            reviewRepository.submit(bookingId, rating, comment)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(reviewSubmittingForBookingId = null)
                    load()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(reviewSubmittingForBookingId = null, reviewError = it.message ?: "Could not submit review.")
                }
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
        private val reviewRepository: ReviewRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentDashboardViewModel(bookingRepository, reviewRepository, authRepository) as T
        }
    }
}