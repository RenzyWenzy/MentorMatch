package edu.cit.estillore.mentormatch.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.BookingRequest
import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.TutorProfile
import edu.cit.estillore.mentormatch.data.repository.BookingRepository
import edu.cit.estillore.mentormatch.data.repository.SubjectRepository
import edu.cit.estillore.mentormatch.data.repository.TutorProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TutorSearchUiState(
    val subjects: List<Subject> = emptyList(),
    val subjectFilter: Long? = null,
    val dayFilter: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<TutorProfile> = emptyList(),
    val bookingInProgressForTutorId: Long? = null,
    val bookingError: String? = null,
    val bookingSuccessMessage: String? = null
)

/**
 * ASSUMPTION: no matching StudentDashboard/search screen was uploaded, so
 * this screen and its booking payload are inferred from tutorProfile.js /
 * bookings.js. Confirm the BookingRequest field names against the backend
 * (or StudentDashboard.jsx if you can send it over) before wiring this up.
 */
class TutorSearchViewModel(
    private val tutorProfileRepository: TutorProfileRepository,
    private val subjectRepository: SubjectRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TutorSearchUiState())
    val uiState: StateFlow<TutorSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subjectRepository.list().onSuccess { _uiState.value = _uiState.value.copy(subjects = it) }
        }
        search()
    }

    fun setSubjectFilter(subjectId: Long?) {
        _uiState.value = _uiState.value.copy(subjectFilter = subjectId)
        search()
    }

    fun setDayFilter(day: String?) {
        _uiState.value = _uiState.value.copy(dayFilter = day)
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val state = _uiState.value
            tutorProfileRepository.search(subjectId = state.subjectFilter, dayOfWeek = state.dayFilter)
                .onSuccess { _uiState.value = _uiState.value.copy(loading = false, results = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "Could not load tutors.") }
        }
    }

    fun book(tutorProfileId: Long, subjectId: Long, sessionDate: String, startTime: String, endTime: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(bookingInProgressForTutorId = tutorProfileId, bookingError = null, bookingSuccessMessage = null)
            bookingRepository.create(BookingRequest(tutorProfileId, subjectId, sessionDate, startTime, endTime))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(bookingInProgressForTutorId = null, bookingSuccessMessage = "Booking request sent.")
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(bookingInProgressForTutorId = null, bookingError = it.message ?: "Could not book this session.")
                }
        }
    }

    fun dismissBookingMessages() {
        _uiState.value = _uiState.value.copy(bookingError = null, bookingSuccessMessage = null)
    }

    class Factory(
        private val tutorProfileRepository: TutorProfileRepository,
        private val subjectRepository: SubjectRepository,
        private val bookingRepository: BookingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TutorSearchViewModel(tutorProfileRepository, subjectRepository, bookingRepository) as T
        }
    }
}