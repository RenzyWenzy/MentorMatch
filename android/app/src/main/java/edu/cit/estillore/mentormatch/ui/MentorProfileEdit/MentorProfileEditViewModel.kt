package edu.cit.estillore.mentormatch.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.ApprovalStatus
import edu.cit.estillore.mentormatch.data.model.AvailabilitySlot
import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.SubjectProficiency
import edu.cit.estillore.mentormatch.data.repository.AvailabilityRepository
import edu.cit.estillore.mentormatch.data.repository.SubjectRepository
import edu.cit.estillore.mentormatch.data.repository.TutorProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MentorProfileUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val subjects: List<Subject> = emptyList(),
    val bio: String = "",
    val subjectRows: List<SubjectProficiency> = emptyList(),
    val slots: List<AvailabilitySlot> = emptyList(),
    val isNewProfile: Boolean = false,
    val approvalStatus: ApprovalStatus? = null,
    val rejectionReason: String? = null,
    val submitting: Boolean = false,
    val formError: String? = null,
    val savedMessage: String? = null,
    val savingAvailability: Boolean = false,
    val availabilityError: String? = null,
    val availabilitySaved: String? = null
)

/** Android equivalent of MentorProfileEdit.jsx. */
class MentorProfileEditViewModel(
    private val tutorProfileRepository: TutorProfileRepository,
    private val subjectRepository: SubjectRepository,
    private val availabilityRepository: AvailabilityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorProfileUiState())
    val uiState: StateFlow<MentorProfileUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, loadError = null)

            val catalogResult = subjectRepository.list()
            val catalog = catalogResult.getOrElse {
                _uiState.value = _uiState.value.copy(loading = false, loadError = it.message ?: "Could not load your profile.")
                return@launch
            }

            tutorProfileRepository.myProfile()
                .onSuccess { profile ->
                    if (profile == null) {
                        _uiState.value = _uiState.value.copy(loading = false, subjects = catalog, isNewProfile = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            subjects = catalog,
                            bio = profile.bio ?: "",
                            subjectRows = profile.subjects,
                            slots = profile.availability,
                            approvalStatus = profile.approvalStatus,
                            rejectionReason = profile.rejectionReason,
                            isNewProfile = false
                        )
                    }
                }
                .onFailure { err ->
                    if (err is TutorProfileRepository.NoProfileYet) {
                        _uiState.value = _uiState.value.copy(loading = false, subjects = catalog, isNewProfile = true)
                    } else {
                        _uiState.value = _uiState.value.copy(loading = false, loadError = err.message ?: "Could not load your profile.")
                    }
                }
        }
    }

    fun setBio(bio: String) { _uiState.value = _uiState.value.copy(bio = bio) }
    fun setSubjectRows(rows: List<SubjectProficiency>) { _uiState.value = _uiState.value.copy(subjectRows = rows) }

    fun addSlot() {
        _uiState.value = _uiState.value.copy(
            slots = _uiState.value.slots + AvailabilitySlot(edu.cit.estillore.mentormatch.data.model.DayOfWeek.MONDAY, "", "")
        )
    }

    fun updateSlot(index: Int, slot: AvailabilitySlot) {
        _uiState.value = _uiState.value.copy(
            slots = _uiState.value.slots.mapIndexed { i, s -> if (i == index) slot else s }
        )
    }

    fun removeSlot(index: Int) {
        _uiState.value = _uiState.value.copy(slots = _uiState.value.slots.filterIndexed { i, _ -> i != index })
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.subjectRows.isEmpty()) {
            _uiState.value = state.copy(formError = "Add at least one subject before saving.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitting = true, formError = null, savedMessage = null)
            tutorProfileRepository.saveMyProfile(state.bio, state.subjectRows)
                .onSuccess { saved ->
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        isNewProfile = false,
                        approvalStatus = saved.approvalStatus,
                        rejectionReason = saved.rejectionReason,
                        savedMessage = if (saved.approvalStatus == ApprovalStatus.PENDING)
                            "Profile saved. An admin will review it before it appears in search."
                        else "Profile saved."
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(submitting = false, formError = err.message ?: "Could not save your profile.")
                }
        }
    }

    fun saveAvailability() {
        val state = _uiState.value
        if (state.isNewProfile) {
            _uiState.value = state.copy(availabilityError = "Save your profile (bio and subjects) before setting availability.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(savingAvailability = true, availabilityError = null, availabilitySaved = null)
            availabilityRepository.replaceMine(state.slots)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(savingAvailability = false, availabilitySaved = "Availability saved.")
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(savingAvailability = false, availabilityError = err.message ?: "Could not save availability.")
                }
        }
    }

    class Factory(
        private val tutorProfileRepository: TutorProfileRepository,
        private val subjectRepository: SubjectRepository,
        private val availabilityRepository: AvailabilityRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MentorProfileEditViewModel(tutorProfileRepository, subjectRepository, availabilityRepository) as T
        }
    }
}