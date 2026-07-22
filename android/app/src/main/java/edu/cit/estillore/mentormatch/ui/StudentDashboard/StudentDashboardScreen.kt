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
import edu.cit.estillore.mentormatch.data.model.Booking
import edu.cit.estillore.mentormatch.data.model.BookingStatus
import edu.cit.estillore.mentormatch.ui.notification.NotificationBell
import edu.cit.estillore.mentormatch.ui.notification.NotificationViewModel
import edu.cit.estillore.mentormatch.ui.tutor.TutorSearchScreen
import edu.cit.estillore.mentormatch.ui.tutor.TutorSearchViewModel

/** Android equivalent of StudentDashboard.jsx (not uploaded — layout inferred from the pattern used by MentorDashboard/AdminDashboard). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: StudentDashboardViewModel,
    tutorSearchViewModel: TutorSearchViewModel,
    notificationViewModel: NotificationViewModel,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var reviewTarget by remember { mutableStateOf<Booking?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student dashboard") },
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
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("My bookings") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Find a tutor") })
            }

            when (tab) {
                0 -> Column(Modifier.padding(16.dp)) {
                    when {
                        state.loading -> CircularProgressIndicator()
                        state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        state.bookings.isEmpty() -> Text("No bookings yet — find a tutor to get started.")
                        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.bookings, key = { it.id }) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onCancel = { viewModel.cancelBooking(booking.id) },
                                    onRate = { reviewTarget = booking }
                                )
                            }
                        }
                    }
                }
                1 -> TutorSearchScreen(tutorSearchViewModel)
            }
        }
    }

    reviewTarget?.let { booking ->
        ReviewDialog(
            booking = booking,
            submitting = state.reviewSubmittingForBookingId == booking.id,
            error = state.reviewError,
            onDismiss = { reviewTarget = null },
            onSubmit = { rating, comment -> viewModel.submitReview(booking.id, rating, comment) }
        )
    }
}

@Composable
private fun BookingCard(booking: Booking, onCancel: () -> Unit, onRate: () -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(booking.subjectName, style = MaterialTheme.typography.titleMedium)
            booking.tutorName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text("${booking.sessionDate} · ${booking.startTime}–${booking.endTime}", style = MaterialTheme.typography.bodySmall)
            Text(booking.status.name, style = MaterialTheme.typography.labelMedium)

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.CONFIRMED) {
                    OutlinedButton(onClick = onCancel) { Text("Cancel") }
                }
                if (booking.status == BookingStatus.COMPLETED) {
                    Button(onClick = onRate) { Text("Rate this session") }
                }
            }
        }
    }
}

@Composable
private fun ReviewDialog(
    booking: Booking,
    submitting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate ${booking.subjectName}") },
        text = {
            Column {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                Row {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Text(if (star <= rating) "★" else "☆", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(rating, comment.ifBlank { null }) }, enabled = !submitting) {
                Text(if (submitting) "Submitting…" else "Submit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}