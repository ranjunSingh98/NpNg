package com.example.gymapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveWorkoutScreen(
    workoutType: String,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exerciseName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }
    var lastTimeExpanded by remember { mutableStateOf(false) }
    var activeSessionId by remember { mutableStateOf<Long?>(null) }
    var expandedAutocomplete by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    val historyExerciseNamesFlow = remember(workoutType) { viewModel.getExerciseNamesByType(workoutType) }
    val historyExerciseNames by historyExerciseNamesFlow.collectAsState(initial = emptyList())

    val filteredNames = remember(exerciseName, historyExerciseNames) {
        val normalizedQuery = exerciseName.trim().replace("\\s+".toRegex(), " ")
        if (normalizedQuery.isBlank()) emptyList()
        else historyExerciseNames.filter { it.contains(normalizedQuery, ignoreCase = true) && it != normalizedQuery }
    }

    LaunchedEffect(workoutType) {
        val newSessionId = viewModel.startSession(workoutType)
        activeSessionId = newSessionId
    }

    // Collect last workout entries based on activeSessionId
    val lastWorkoutEntries by remember(workoutType, activeSessionId) {
        if (activeSessionId != null) {
            viewModel.getPreviousWorkoutEntries(workoutType, activeSessionId!!)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val currentEntries by remember(activeSessionId) {
        if (activeSessionId != null) {
            viewModel.getEntriesForSession(activeSessionId!!)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val groupedLastEntries = remember(lastWorkoutEntries) {
        lastWorkoutEntries.groupBy { it.exerciseName }
    }

    val scope = rememberCoroutineScope()

    suspend fun performBackAction() {
        if (isEditMode) {
            isEditMode = false
            return
        }

        val wasLastTimeExpanded = lastTimeExpanded
        if (wasLastTimeExpanded) {
            lastTimeExpanded = false
        }

        if (currentEntries.isEmpty()) {
            activeSessionId?.let { viewModel.discardCurrentSession(it) }
            if (wasLastTimeExpanded) {
                delay(300)
            }
            onBack()
        } else {
            showExitDialog = true
            if (wasLastTimeExpanded) {
                delay(300)
            }
        }
    }

    BackHandler {
        scope.launch {
            performBackAction()
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Finish Workout?") },
            text = { Text("Do you want to save this workout or discard it?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.finishCurrentSession()
                    onBack()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        activeSessionId?.let { viewModel.discardCurrentSession(it) }
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutType) },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            performBackAction()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isEditMode) isEditMode = false
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (lastWorkoutEntries.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { lastTimeExpanded = !lastTimeExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Last time:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (lastTimeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (lastTimeExpanded) "Collapse History" else "Expand History",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = lastTimeExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                groupedLastEntries.forEach { (name, entries) ->
                                    CollapsibleExerciseCard(exerciseName = name, entries = entries)
                                }
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedAutocomplete && filteredNames.isNotEmpty(),
                    onExpandedChange = { expandedAutocomplete = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = {
                            exerciseName = it
                            expandedAutocomplete = it.isNotBlank()
                        },
                        label = { Text("Exercise Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                        trailingIcon = if (exerciseName.isNotBlank()) {
                            {
                                IconButton(onClick = { 
                                    exerciseName = ""
                                    expandedAutocomplete = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        } else null
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedAutocomplete && filteredNames.isNotEmpty(),
                        onDismissRequest = { expandedAutocomplete = false }
                    ) {
                        filteredNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    exerciseName = name
                                    expandedAutocomplete = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (lbs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = if (weight.isNotBlank()) {
                            {
                                IconButton(onClick = { weight = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Weight")
                                }
                            }
                        } else null
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = if (reps.isNotBlank()) {
                            {
                                IconButton(onClick = { reps = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Reps")
                                }
                            }
                        } else null
                    )
                }

                Button(
                    onClick = {
                        expandedAutocomplete = false
                        if (exerciseName.isNotBlank() && weight.isNotBlank() && reps.isNotBlank()) {
                            val nextSetNumber = currentEntries.filter { it.exerciseName.equals(exerciseName, ignoreCase = true) }.size + 1
                            activeSessionId?.let {
                                viewModel.addEntry(
                                    sessionId = it,
                                    exerciseName = exerciseName,
                                    weight = weight.toDoubleOrNull() ?: 0.0,
                                    reps = reps.toIntOrNull() ?: 0,
                                    setNumber = nextSetNumber
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Set")
                }

                HorizontalDivider()

                Text(
                    text = "Current Log",
                    style = MaterialTheme.typography.titleMedium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentEntries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = { isEditMode = true },
                                    onClick = { if (isEditMode) isEditMode = false }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                AnimatedVisibility(
                                    visible = isEditMode,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteEntry(entry) },
                                        modifier = Modifier.size(32.dp).padding(end = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Set",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Text(text = "${entry.exerciseName} (Set ${entry.setNumber})")
                            }
                            Text(text = "${entry.weight} lbs x ${entry.reps}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (currentEntries.isEmpty()) {
                        activeSessionId?.let { viewModel.discardCurrentSession(it) }
                    } else {
                        viewModel.finishCurrentSession()
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Finish Workout")
            }
        }
    }
}

@Composable
fun CollapsibleExerciseCard(exerciseName: String, entries: List<ExerciseEntry>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    entries.forEach { entry ->
                        Text(
                            text = "Set ${entry.setNumber}: ${entry.weight}lbs x ${entry.reps}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
