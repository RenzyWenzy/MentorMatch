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
import edu.cit.estillore.mentormatch.data.model.ApprovalStatus
import edu.cit.estillore.mentormatch.data.model.Booking
import edu.cit.estillore.mentormatch.data.model.BookingStatus
import edu.cit.estillore.mentormatch.ui.notification.NotificationBell
import edu.cit.estillore.mentormatch.ui.notification.NotificationViewModel

private fun approvalBannerText(status: ApprovalStatus) = when (status) {
    ApprovalStatus.PENDING -> "Your tutor profile is pending admin review. Students won't see you in search until it's approved."
    ApprovalStatus.REJECTED -> "Your tutor profile was not approved. Update it and it will go back into the review queue."
    ApprovalStatus.APPROVED -> null
}

/** Android equivalent of MentorDashboard.jsx. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorDashboardScreen(
    viewModel: MentorDashboardViewModel,
    notificationViewModel: NotificationViewModel,
    onEditProfile: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val ratingLabel = state.profile?.averageRating?.let { "%.1f★ (%d)".format(it, state.profile?.reviewCount ?: 0) } ?: "No ratings yet"
    val bannerText = state.profile?.approvalStatus?.let { approvalBannerText(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mentor dashboard") },
                actions = {
                    NotificationBell(notificationViewModel)
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Manage your mentees and availability.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onEditProfile) { Text("Edit my profile") }
            }

            bannerText?.let {
                Spacer(Modifier.height(12.dp))
                Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(it, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Average rating: $ratingLabel", style = MaterialTheme.typography.titleSmall)

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

            Spacer(Modifier.height(16.dp))
            Text("Session requests", style = MaterialTheme.typography.titleMedium)

            when {
                state.loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                state.bookings.isEmpty() -> Text("No booking requests yet.", modifier = Modifier.padding(top = 8.dp))
                else -> LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.bookings, key = { it.id }) { booking ->
                        MentorBookingCard(
                            booking = booking,
                            onConfirm = { viewModel.confirm(booking.id) },
                            onDecline = { viewModel.decline(booking.id) },
                            onComplete = { viewModel.complete(booking.id) }
                        )
                    }
                }
            }

            if (state.profile != null) {
                Spacer(Modifier.height(16.dp))
                Text("My reviews", style = MaterialTheme.typography.titleMedium)
                if (state.reviews.isEmpty()) {
                    Text("No reviews yet.", modifier = Modifier.padding(top = 4.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 240.dp)) {
                        items(state.reviews, key = { it.id }) { review ->
                            ElevatedCard {
                                Column(Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(review.studentName ?: "Student", style = MaterialTheme.typography.labelLarge)
                                        Text("★".repeat(review.rating) + "☆".repeat(5 - review.rating))
                                    }
                                    review.comment?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
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
private fun MentorBookingCard(
    booking: Booking,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    onComplete: () -> Unit
) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(booking.studentName ?: "Student", style = MaterialTheme.typography.titleMedium)
            Text(booking.subjectName, style = MaterialTheme.typography.bodySmall)
            Text("${booking.sessionDate} · ${booking.startTime}–${booking.endTime}", style = MaterialTheme.typography.bodySmall)
            Text(booking.status.name, style = MaterialTheme.typography.labelMedium)

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (booking.status == BookingStatus.PENDING) {
                    Button(onClick = onConfirm) { Text("Confirm") }
                    OutlinedButton(onClick = onDecline) { Text("Decline") }
                }
                if (booking.status == BookingStatus.CONFIRMED) {
                    Button(onClick = onComplete) { Text("Mark completed") }
                }
            }
        }
    }
}