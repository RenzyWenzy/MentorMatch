package edu.cit.estillore.mentormatch.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.estillore.mentormatch.data.model.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: Role,
    viewModel: DashboardViewModel,
    onLoggedOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(role) {
        viewModel.load(role)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dashboardTitle(role)) },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DashboardUiState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DashboardUiState.Loaded -> {
                    val user = state.user
                    Column {
                        Text("Welcome, ${user.fullName}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        InfoRow("Email", user.email)
                        InfoRow("Role", user.role.name)
                        when (user.role) {
                            Role.STUDENT -> {
                                InfoRow("Student number", user.studentNumber ?: "—")
                                InfoRow("Program", user.program ?: "—")
                            }
                            Role.MENTOR -> {
                                InfoRow("Expertise", user.expertise ?: "—")
                                InfoRow("Department", user.department ?: "—")
                            }
                            Role.ADMIN -> {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Admin tools (user management, etc.) go here.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun dashboardTitle(role: Role): String = when (role) {
    Role.STUDENT -> "Student Dashboard"
    Role.MENTOR -> "Mentor Dashboard"
    Role.ADMIN -> "Admin Dashboard"
}
