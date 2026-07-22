package edu.cit.estillore.mentormatch.ui.tutor

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.estillore.mentormatch.data.model.ApprovalStatus
import edu.cit.estillore.mentormatch.data.model.AvailabilitySlot
import edu.cit.estillore.mentormatch.data.model.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private fun approvalLabel(status: ApprovalStatus) = when (status) {
    ApprovalStatus.PENDING -> "Pending review — you're not visible in search yet"
    ApprovalStatus.APPROVED -> "Approved — visible to students in search"
    ApprovalStatus.REJECTED -> "Not approved"
}

/** Android equivalent of MentorProfileEdit.jsx. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorProfileEditScreen(
    viewModel: MentorProfileEditViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Edit my profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            if (state.isNewProfile) "You don't have a tutor profile yet — fill this out so students can find you."
            else "Update your bio, subjects, and weekly availability.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        if (state.loading) {
            CircularProgressIndicator()
            return@Column
        }

        state.loadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            return@Column
        }

        state.approvalStatus?.let { status ->
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(approvalLabel(status), style = MaterialTheme.typography.labelLarge)
                    if (status == ApprovalStatus.REJECTED && state.rejectionReason != null) {
                        Text(state.rejectionReason!!, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                state.savedMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp)) }

                OutlinedTextField(
                    value = state.bio,
                    onValueChange = viewModel::setBio,
                    label = { Text("Bio") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${state.bio.length}/1000",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )

                Spacer(Modifier.height(12.dp))
                Text("Subjects & proficiency", style = MaterialTheme.typography.labelLarge)
                SubjectProficiencyPicker(
                    subjects = state.subjects,
                    value = state.subjectRows,
                    onChange = viewModel::setSubjectRows
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::saveProfile, enabled = !state.submitting) {
                        Text(if (state.submitting) "Saving…" else "Save profile")
                    }
                    OutlinedButton(onClick = onBack) { Text("Back to dashboard") }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly availability", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Set the recurring times you're available for tutoring sessions. Students can only book within these slots.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))

                state.availabilityError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                state.availabilitySaved?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

                if (state.slots.isEmpty()) {
                    Text(
                        "No availability set yet. Add a time slot below.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                state.slots.forEachIndexed { index, slot ->
                    AvailabilitySlotRow(
                        slot = slot,
                        onChange = { viewModel.updateSlot(index, it) },
                        onRemove = { viewModel.removeSlot(index) }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::addSlot) { Text("+ Add time slot") }
                    Button(onClick = viewModel::saveAvailability, enabled = !state.savingAvailability) {
                        Text(if (state.savingAvailability) "Saving…" else "Save availability")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailabilitySlotRow(
    slot: AvailabilitySlot,
    onChange: (AvailabilitySlot) -> Unit,
    onRemove: () -> Unit
) {
    var dayMenuExpanded by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    fun parseTime(raw: String): LocalTime? = try {
        LocalTime.parse(raw, timeFormatter)
    } catch (e: DateTimeParseException) {
        null
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = dayMenuExpanded,
            onExpandedChange = { dayMenuExpanded = it },
            modifier = Modifier.weight(1.3f)
        ) {
            OutlinedTextField(
                value = slot.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Day") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = dayMenuExpanded, onDismissRequest = { dayMenuExpanded = false }) {
                DayOfWeek.values().forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = { onChange(slot.copy(dayOfWeek = day)); dayMenuExpanded = false }
                    )
                }
            }
        }

        val startInteractionSource = remember { MutableInteractionSource() }
        LaunchedEffect(startInteractionSource) {
            startInteractionSource.interactions.collect {
                if (it is PressInteraction.Release) showStartPicker = true
            }
        }
        OutlinedTextField(
            value = slot.startTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Start time") },
            placeholder = { Text("Select a time") },
            interactionSource = startInteractionSource,
            modifier = Modifier.weight(1f)
        )

        val endInteractionSource = remember { MutableInteractionSource() }
        LaunchedEffect(endInteractionSource) {
            endInteractionSource.interactions.collect {
                if (it is PressInteraction.Release) showEndPicker = true
            }
        }
        OutlinedTextField(
            value = slot.endTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("End time") },
            placeholder = { Text("Select a time") },
            interactionSource = endInteractionSource,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Remove slot")
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            initialTime = parseTime(slot.startTime),
            onDismiss = { showStartPicker = false },
            onConfirm = { onChange(slot.copy(startTime = it.format(timeFormatter))); showStartPicker = false }
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            initialTime = parseTime(slot.endTime),
            onDismiss = { showEndPicker = false },
            onConfirm = { onChange(slot.copy(endTime = it.format(timeFormatter))); showEndPicker = false }
        )
    }
}