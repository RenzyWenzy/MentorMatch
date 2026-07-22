package edu.cit.estillore.mentormatch.ui.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.estillore.mentormatch.data.model.ProficiencyLevel
import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.SubjectProficiency

private fun proficiencyLabel(level: ProficiencyLevel) =
    level.name.lowercase().replaceFirstChar { it.uppercase() }

/**
 * Android equivalent of SubjectProficiencyPicker.jsx — lets a mentor build
 * their subject/proficiency list from the shared catalog. Each subject can
 * only be picked once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectProficiencyPicker(
    subjects: List<Subject>,
    value: List<SubjectProficiency>,
    onChange: (List<SubjectProficiency>) -> Unit
) {
    val subjectById = subjects.associateBy { it.id }
    val pickedIds = value.map { it.subjectId }.toSet()
    val availableForNewRow = subjects.filter { it.id !in pickedIds }

    Column {
        if (value.isEmpty()) {
            Text(
                "No subjects added yet. Add at least one to save your profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        value.forEachIndexed { index, row ->
            val rowOptions = subjects.filter { it.id == row.subjectId || it.id !in pickedIds }
            var subjectMenuExpanded by remember { mutableStateOf(false) }
            var proficiencyMenuExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it },
                    modifier = Modifier.weight(2f)
                ) {
                    OutlinedTextField(
                        value = subjectById[row.subjectId]?.name ?: row.subjectName ?: "Unknown subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = subjectMenuExpanded, onDismissRequest = { subjectMenuExpanded = false }) {
                        rowOptions.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    onChange(value.mapIndexed { i, r -> if (i == index) r.copy(subjectId = s.id, subjectName = s.name) else r })
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = proficiencyMenuExpanded,
                    onExpandedChange = { proficiencyMenuExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = proficiencyLabel(row.proficiencyLevel),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Level") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = proficiencyMenuExpanded, onDismissRequest = { proficiencyMenuExpanded = false }) {
                        ProficiencyLevel.values().forEach { level ->
                            DropdownMenuItem(
                                text = { Text(proficiencyLabel(level)) },
                                onClick = {
                                    onChange(value.mapIndexed { i, r -> if (i == index) r.copy(proficiencyLevel = level) else r })
                                    proficiencyMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = { onChange(value.filterIndexed { i, _ -> i != index }) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove subject")
                }
            }
        }

        TextButton(
            onClick = {
                if (availableForNewRow.isNotEmpty()) {
                    val first = availableForNewRow.first()
                    onChange(value + SubjectProficiency(first.id, first.name, ProficiencyLevel.BEGINNER))
                }
            },
            enabled = availableForNewRow.isNotEmpty()
        ) {
            Text("+ Add subject")
        }
        if (availableForNewRow.isEmpty() && subjects.isNotEmpty()) {
            Text(
                "All catalog subjects added.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}