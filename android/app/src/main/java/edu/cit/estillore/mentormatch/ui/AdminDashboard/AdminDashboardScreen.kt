package edu.cit.estillore.mentormatch.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.TutorProfile
import edu.cit.estillore.mentormatch.data.model.UserResponse
import edu.cit.estillore.mentormatch.ui.notification.NotificationBell
import edu.cit.estillore.mentormatch.ui.notification.NotificationViewModel
import java.time.LocalDate

private val ROLE_FILTERS = listOf("ALL", "STUDENT", "MENTOR", "ADMIN")

/** Android equivalent of AdminDashboard.jsx. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    notificationViewModel: NotificationViewModel,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin dashboard") },
                actions = {
                    NotificationBell(notificationViewModel)
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Users") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Subjects") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Approvals") })
                Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Reports") })
            }

            when (tab) {
                0 -> UsersTab(state, viewModel)
                1 -> SubjectsTab(state, viewModel)
                2 -> ApprovalsTab(state, viewModel)
                3 -> ReportsTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun UsersTab(state: AdminDashboardUiState, viewModel: AdminDashboardViewModel) {
    val filtered = if (state.roleFilter == "ALL") state.users else state.users.filter { it.role.name == state.roleFilter }

    Column(Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ROLE_FILTERS.forEach { r ->
                FilterChip(selected = state.roleFilter == r, onClick = { viewModel.setRoleFilter(r) }, label = { Text(r) })
            }
        }
        Spacer(Modifier.height(12.dp))

        state.usersError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.userActionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when {
            state.loadingUsers -> CircularProgressIndicator()
            filtered.isEmpty() -> Text("No users match this filter.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { u ->
                    UserRow(u, isPending = state.pendingUserActionId == u.id, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun UserRow(u: UserResponse, isPending: Boolean, viewModel: AdminDashboardViewModel) {
    ElevatedCard {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(u.fullName, style = MaterialTheme.typography.titleSmall)
                    Text(u.email, style = MaterialTheme.typography.bodySmall)
                }
                Text(u.role.name, style = MaterialTheme.typography.labelMedium)
            }
            Text(if (u.active) "Active" else "Deactivated", style = MaterialTheme.typography.labelSmall)

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (u.active) {
                    OutlinedButton(onClick = { viewModel.deactivateUser(u.id) }, enabled = !isPending) { Text("Deactivate") }
                } else {
                    Button(onClick = { viewModel.activateUser(u.id) }, enabled = !isPending) { Text("Activate") }
                }
                OutlinedButton(onClick = { viewModel.removeUser(u.id) }, enabled = !isPending) { Text("Remove") }
            }
        }
    }
}

@Composable
private fun SubjectsTab(state: AdminDashboardUiState, viewModel: AdminDashboardViewModel) {
    var editing by remember { mutableStateOf<Subject?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        state.subjectsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (editing != null) viewModel.updateSubject(editing!!.id, name, description.ifBlank { null })
                    else viewModel.createSubject(name, description.ifBlank { null })
                    editing = null; name = ""; description = ""
                },
                enabled = name.isNotBlank()
            ) { Text(if (editing != null) "Save changes" else "Add subject") }
            if (editing != null) {
                OutlinedButton(onClick = { editing = null; name = ""; description = "" }) { Text("Cancel") }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (state.loadingSubjects) {
            CircularProgressIndicator()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.subjects, key = { it.id }) { s ->
                    ElevatedCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(s.name, style = MaterialTheme.typography.titleSmall)
                                s.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = { editing = s; name = s.name; description = s.description ?: "" }) { Text("Edit") }
                                TextButton(onClick = { viewModel.deleteSubject(s.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovalsTab(state: AdminDashboardUiState, viewModel: AdminDashboardViewModel) {
    var reasonDraftId by remember { mutableStateOf<Long?>(null) }
    var reasonText by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        state.approvalsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when {
            state.loadingApprovals -> CircularProgressIndicator()
            state.pendingProfiles.isEmpty() -> Text("No tutor profiles are waiting for review.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.pendingProfiles, key = { it.id }) { p: TutorProfile ->
                    val isBusy = state.pendingApprovalActionId == p.id
                    val isDrafting = reasonDraftId == p.id
                    ElevatedCard {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.fullName ?: "Mentor", style = MaterialTheme.typography.titleSmall)
                            Text("${p.email ?: ""} · ${p.department ?: "—"}", style = MaterialTheme.typography.bodySmall)
                            p.bio?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }

                            if (!isDrafting) {
                                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.approveProfile(p.id) }, enabled = !isBusy) { Text("Approve") }
                                    OutlinedButton(onClick = { reasonDraftId = p.id; reasonText = "" }, enabled = !isBusy) { Text("Reject") }
                                }
                            } else {
                                OutlinedTextField(
                                    value = reasonText, onValueChange = { reasonText = it },
                                    label = { Text("Reason (optional)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.rejectProfile(p.id, reasonText.ifBlank { null }); reasonDraftId = null },
                                        enabled = !isBusy
                                    ) { Text("Confirm reject") }
                                    OutlinedButton(onClick = { reasonDraftId = null }) { Text("Cancel") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsTab(state: AdminDashboardUiState, viewModel: AdminDashboardViewModel) {
    var startDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1).toString()) }
    var endDate by remember { mutableStateOf(LocalDate.now().toString()) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.generateReport(startDate, endDate) }, enabled = !state.reportLoading) {
            Text(if (state.reportLoading) "Generating…" else "Generate report")
        }

        state.reportError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        state.report?.let { r ->
            Spacer(Modifier.height(16.dp))
            Text("Total sessions: ${r.totalSessions}")
            Text("Completed: ${r.completedCount}")
            Text("Confirmed: ${r.confirmedCount}")
            Text("Active tutors: ${r.activeTutorsCount}")
            Text("Pending requests: ${r.pendingCount}")
            Text("Declined requests: ${r.declinedCount}")
            Text("Cancelled sessions: ${r.cancelledCount}")
            Text("New reviews: ${r.newReviewsCount}")
            Text("Average rating: ${r.averageRating?.let { "%.1f".format(it) } ?: "—"}")
        }
    }
}