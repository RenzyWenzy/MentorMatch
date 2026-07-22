package edu.cit.estillore.mentormatch.ui.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.estillore.mentormatch.data.model.DayOfWeek
import edu.cit.estillore.mentormatch.data.model.TutorProfile

/**
 * Android equivalent of the tutor-search part of StudentDashboard.jsx (not
 * uploaded — this screen and the booking dialog fields are best-effort;
 * verify the booking payload against the real backend/web form).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorSearchScreen(viewModel: TutorSearchViewModel) {
    val state by viewModel.uiState.collectAsState()
    var bookingTarget by remember { mutableStateOf<TutorProfile?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Find a tutor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var subjectMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subjectMenuExpanded,
                onExpandedChange = { subjectMenuExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                val selectedName = state.subjects.find { it.id == state.subjectFilter }?.name ?: "Any subject"
                OutlinedTextField(
                    value = selectedName, onValueChange = {}, readOnly = true,
                    label = { Text("Subject") }, modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = subjectMenuExpanded, onDismissRequest = { subjectMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Any subject") }, onClick = { viewModel.setSubjectFilter(null); subjectMenuExpanded = false })
                    state.subjects.forEach { s ->
                        DropdownMenuItem(text = { Text(s.name) }, onClick = { viewModel.setSubjectFilter(s.id); subjectMenuExpanded = false })
                    }
                }
            }

            var dayMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = dayMenuExpanded,
                onExpandedChange = { dayMenuExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = state.dayFilter?.let { it.lowercase().replaceFirstChar { c -> c.uppercase() } } ?: "Any day",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Day") }, modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = dayMenuExpanded, onDismissRequest = { dayMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Any day") }, onClick = { viewModel.setDayFilter(null); dayMenuExpanded = false })
                    DayOfWeek.values().forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.name.lowercase().replaceFirstChar { c -> c.uppercase() }) },
                            onClick = { viewModel.setDayFilter(d.name); dayMenuExpanded = false }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
            state.results.isEmpty() -> Text("No tutors match these filters yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.results, key = { it.id }) { tutor ->
                    TutorResultCard(tutor, onBook = { bookingTarget = tutor })
                }
            }
        }
    }

    bookingTarget?.let { tutor ->
        BookSessionDialog(
            tutor = tutor,
            bookingInProgress = state.bookingInProgressForTutorId == tutor.id,
            onDismiss = { bookingTarget = null; viewModel.dismissBookingMessages() },
            onConfirm = { subjectId, date, start, end -> viewModel.book(tutor.id, subjectId, date, start, end) }
        )
    }

    state.bookingSuccessMessage?.let {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBookingMessages(); bookingTarget = null },
            confirmButton = { TextButton(onClick = { viewModel.dismissBookingMessages(); bookingTarget = null }) { Text("OK") } },
            title = { Text("Request sent") },
            text = { Text(it) }
        )
    }
}

@Composable
private fun TutorResultCard(tutor: TutorProfile, onBook: () -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(tutor.fullName ?: "Tutor", style = MaterialTheme.typography.titleMedium)
            if (!tutor.department.isNullOrBlank()) {
                Text(tutor.department, style = MaterialTheme.typography.bodySmall)
            }
            if (tutor.averageRating != null) {
                Text("${"%.1f".format(tutor.averageRating)}★ (${tutor.reviewCount})", style = MaterialTheme.typography.bodySmall)
            }
            if (!tutor.bio.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(tutor.bio, style = MaterialTheme.typography.bodySmall, maxLines = 3)
            }
            if (tutor.subjects.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    tutor.subjects.joinToString(" · ") { "${it.subjectName ?: "Subject"} (${it.proficiencyLevel.name.lowercase()})" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = onBook) { Text("Book a session") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookSessionDialog(
    tutor: TutorProfile,
    bookingInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, date: String, start: String, end: String) -> Unit
) {
    var subjectId by remember { mutableStateOf(tutor.subjects.firstOrNull()?.subjectId) }
    var date by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book with ${tutor.fullName ?: "this tutor"}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = subjectMenuExpanded, onExpandedChange = { subjectMenuExpanded = it }) {
                    val label = tutor.subjects.find { it.subjectId == subjectId }?.subjectName ?: "Select a subject"
                    OutlinedTextField(
                        value = label, onValueChange = {}, readOnly = true,
                        label = { Text("Subject") }, modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = subjectMenuExpanded, onDismissRequest = { subjectMenuExpanded = false }) {
                        tutor.subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.subjectName ?: "Subject") },
                                onClick = { subjectId = s.subjectId; subjectMenuExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start time (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End time (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { subjectId?.let { onConfirm(it, date, start, end) } },
                enabled = !bookingInProgress && subjectId != null && date.isNotBlank() && start.isNotBlank() && end.isNotBlank()
            ) {
                Text(if (bookingInProgress) "Booking…" else "Request booking")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}