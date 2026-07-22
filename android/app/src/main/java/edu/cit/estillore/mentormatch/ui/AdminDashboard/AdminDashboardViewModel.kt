package edu.cit.estillore.mentormatch.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.AdminReport
import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.TutorProfile
import edu.cit.estillore.mentormatch.data.model.UserResponse
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import edu.cit.estillore.mentormatch.data.repository.ReportRepository
import edu.cit.estillore.mentormatch.data.repository.SubjectRepository
import edu.cit.estillore.mentormatch.data.repository.TutorProfileRepository
import edu.cit.estillore.mentormatch.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val loadingUsers: Boolean = true,
    val usersError: String? = null,
    val users: List<UserResponse> = emptyList(),
    val roleFilter: String = "ALL",
    val userActionError: String? = null,
    val pendingUserActionId: Long? = null,

    val subjects: List<Subject> = emptyList(),
    val loadingSubjects: Boolean = true,
    val subjectsError: String? = null,

    val pendingProfiles: List<TutorProfile> = emptyList(),
    val loadingApprovals: Boolean = true,
    val approvalsError: String? = null,
    val pendingApprovalActionId: Long? = null,

    val report: AdminReport? = null,
    val reportLoading: Boolean = false,
    val reportError: String? = null
)

/** Android equivalent of AdminDashboard.jsx (user management + SubjectManager + TutorApprovalQueue + AdminReport). */
class AdminDashboardViewModel(
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository,
    private val tutorProfileRepository: TutorProfileRepository,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
        loadSubjects()
        loadPendingApprovals()
    }

    // ---- Users ----

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingUsers = true, usersError = null)
            userRepository.listUsers()
                .onSuccess { _uiState.value = _uiState.value.copy(loadingUsers = false, users = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loadingUsers = false, usersError = it.message ?: "Could not load users.") }
        }
    }

    fun setRoleFilter(role: String) { _uiState.value = _uiState.value.copy(roleFilter = role) }

    fun activateUser(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pendingUserActionId = id, userActionError = null)
            userRepository.activateUser(id)
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        pendingUserActionId = null,
                        users = _uiState.value.users.map { if (it.id == id) updated else it }
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(pendingUserActionId = null, userActionError = it.message ?: "Could not activate this account.") }
        }
    }

    fun deactivateUser(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pendingUserActionId = id, userActionError = null)
            userRepository.deactivateUser(id)
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        pendingUserActionId = null,
                        users = _uiState.value.users.map { if (it.id == id) updated else it }
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(pendingUserActionId = null, userActionError = it.message ?: "Could not deactivate this account.") }
        }
    }

    fun removeUser(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pendingUserActionId = id, userActionError = null)
            userRepository.removeUser(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(pendingUserActionId = null, users = _uiState.value.users.filter { it.id != id })
                }
                .onFailure { _uiState.value = _uiState.value.copy(pendingUserActionId = null, userActionError = it.message ?: "Could not remove this account.") }
        }
    }

    // ---- Subjects ----

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingSubjects = true, subjectsError = null)
            subjectRepository.list()
                .onSuccess { _uiState.value = _uiState.value.copy(loadingSubjects = false, subjects = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loadingSubjects = false, subjectsError = it.message ?: "Could not load subjects.") }
        }
    }

    fun createSubject(name: String, description: String?) {
        viewModelScope.launch {
            subjectRepository.create(name, description)
                .onSuccess { created -> _uiState.value = _uiState.value.copy(subjects = _uiState.value.subjects + created) }
                .onFailure { _uiState.value = _uiState.value.copy(subjectsError = it.message ?: "Could not save subject.") }
        }
    }

    fun updateSubject(id: Long, name: String, description: String?) {
        viewModelScope.launch {
            subjectRepository.update(id, name, description)
                .onSuccess { updated -> _uiState.value = _uiState.value.copy(subjects = _uiState.value.subjects.map { if (it.id == id) updated else it }) }
                .onFailure { _uiState.value = _uiState.value.copy(subjectsError = it.message ?: "Could not save subject.") }
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            subjectRepository.delete(id)
                .onSuccess { _uiState.value = _uiState.value.copy(subjects = _uiState.value.subjects.filter { it.id != id }) }
                .onFailure { _uiState.value = _uiState.value.copy(subjectsError = it.message ?: "Could not delete subject.") }
        }
    }

    // ---- Tutor approvals ----

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingApprovals = true, approvalsError = null)
            tutorProfileRepository.pending()
                .onSuccess { _uiState.value = _uiState.value.copy(loadingApprovals = false, pendingProfiles = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loadingApprovals = false, approvalsError = it.message ?: "Could not load pending tutor profiles.") }
        }
    }

    fun approveProfile(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pendingApprovalActionId = id, approvalsError = null)
            tutorProfileRepository.approve(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(pendingApprovalActionId = null, pendingProfiles = _uiState.value.pendingProfiles.filter { p -> p.id != id })
                }
                .onFailure { _uiState.value = _uiState.value.copy(pendingApprovalActionId = null, approvalsError = it.message ?: "Could not approve this profile.") }
        }
    }

    fun rejectProfile(id: Long, reason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pendingApprovalActionId = id, approvalsError = null)
            tutorProfileRepository.reject(id, reason)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(pendingApprovalActionId = null, pendingProfiles = _uiState.value.pendingProfiles.filter { p -> p.id != id })
                }
                .onFailure { _uiState.value = _uiState.value.copy(pendingApprovalActionId = null, approvalsError = it.message ?: "Could not reject this profile.") }
        }
    }

    // ---- Reports ----

    fun generateReport(startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(reportLoading = true, reportError = null)
            reportRepository.generate(startDate, endDate)
                .onSuccess { _uiState.value = _uiState.value.copy(reportLoading = false, report = it) }
                .onFailure { _uiState.value = _uiState.value.copy(reportLoading = false, report = null, reportError = it.message ?: "Could not generate report.") }
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
        private val subjectRepository: SubjectRepository,
        private val tutorProfileRepository: TutorProfileRepository,
        private val reportRepository: ReportRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminDashboardViewModel(userRepository, subjectRepository, tutorProfileRepository, reportRepository, authRepository) as T
        }
    }
}