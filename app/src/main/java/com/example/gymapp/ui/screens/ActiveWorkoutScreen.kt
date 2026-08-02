package com.example.gymapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutWithEntries
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveWorkoutScreen(
    workoutType: String,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialSessionId: Long? = null,
) {
    var exerciseName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }
    var lastTimeExpanded by remember { mutableStateOf(false) }
    var activeSessionId by remember { mutableStateOf<Long?>(initialSessionId) }
    var previousWorkoutIndex by rememberSaveable(workoutType, activeSessionId) {
        mutableStateOf(0)
    }
    var expandedAutocomplete by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var isBackActionProcessing by remember { mutableStateOf(false) }
    var originalResumedEntries by remember(initialSessionId) { mutableStateOf<List<ExerciseEntry>?>(null) }

    val exerciseNameFocusRequester = remember { FocusRequester() }
    val weightFocusRequester = remember { FocusRequester() }
    val repsFocusRequester = remember { FocusRequester() }
    val durationFocusRequester = remember { FocusRequester() }

    val isCardio = workoutType.equals("Cardio", ignoreCase = true)

    val historyExerciseNamesFlow = remember(workoutType) { viewModel.getExerciseNamesByType(workoutType) }
    val historyExerciseNames by historyExerciseNamesFlow.collectAsState(initial = emptyList())

    val filteredNames = remember(exerciseName, historyExerciseNames) {
        val normalizedQuery = exerciseName.trim().replace("\\s+".toRegex(), " ")
        if (normalizedQuery.isBlank()) emptyList()
        else historyExerciseNames.filter { it.contains(normalizedQuery, ignoreCase = true) && it != normalizedQuery }
    }

    LaunchedEffect(workoutType, initialSessionId) {
        if (activeSessionId == null) {
            val newSessionId = viewModel.startSession(workoutType)
            activeSessionId = newSessionId
        }
    }

    LaunchedEffect(initialSessionId) {
        if (initialSessionId != null && originalResumedEntries == null) {
            originalResumedEntries = viewModel.getSessionEntriesSnapshot(initialSessionId)
        }
    }

    val previousWorkouts by remember(workoutType, activeSessionId) {
        if (activeSessionId != null) {
            viewModel.getPreviousWorkouts(workoutType, activeSessionId!!)
        } else {
            flowOf(emptyList<WorkoutWithEntries>())
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(previousWorkouts.size) {
        previousWorkoutIndex = previousWorkoutIndex.coerceIn(
            minimumValue = 0,
            maximumValue = previousWorkouts.lastIndex.coerceAtLeast(0),
        )
    }

    val previousWorkout = previousWorkouts.getOrNull(previousWorkoutIndex)

    val currentEntries by remember(activeSessionId) {
        if (activeSessionId != null) {
            viewModel.getEntriesForSession(activeSessionId!!)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val groupedPreviousEntries = remember(previousWorkout) {
        previousWorkout?.entries?.groupBy { it.exerciseName }.orEmpty()
    }

    val scope = rememberCoroutineScope()

    suspend fun performBackAction() {
        if (isBackActionProcessing) return
        
        if (isEditMode) {
            isEditMode = false
            return
        }

        isBackActionProcessing = true

        val delayNeeded = lastTimeExpanded
        if (delayNeeded) {
            lastTimeExpanded = false
        }

        if (currentEntries.isEmpty()) {
            activeSessionId?.let { viewModel.discardCurrentSession(it) }
            if (delayNeeded) {
                delay(300)
            }
            onBack()
        } else {
            showExitDialog = true
            if (delayNeeded) {
                delay(300)
            }
            // Reset if we're showing a dialog and not actually navigating back yet
            isBackActionProcessing = false
        }
    }

    BackHandler {
        scope.launch {
            performBackAction()
        }
    }

    if (showExitDialog) {
        val isResumed = initialSessionId != null
        AlertDialog(
            onDismissRequest = { 
                showExitDialog = false
                isBackActionProcessing = false
            },
            title = { Text(if (isResumed) "Exit Workout?" else "Finish Workout?") },
            text = { 
                Text(
                    if (isResumed) "Would you like to save your changes before exiting?" 
                    else "Do you want to save this workout or discard it?"
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.finishCurrentSession()
                        onBack()
                    }
                ) {
                    Text(if (isResumed) "Save" else "Save and Exit")
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { 
                            showExitDialog = false
                            isBackActionProcessing = false 
                        }
                    ) {
                        Text("Cancel")
                    }
                    if (isResumed) {
                        TextButton(
                            onClick = {
                                val originalEntries = originalResumedEntries
                                if (originalEntries != null) {
                                    viewModel.restoreSessionEntries(initialSessionId, originalEntries)
                                }
                                onBack()
                            }
                        ) {
                            Text("Don't Save")
                        }
                    } else {
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
                    .padding(top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (previousWorkout != null) {
                    val lastWorkoutDateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { lastTimeExpanded = !lastTimeExpanded }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (previousWorkoutIndex < previousWorkouts.lastIndex) {
                                        IconButton(
                                            onClick = { previousWorkoutIndex += 1 },
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "Show older workout",
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = lastWorkoutDateFormat.format(
                                        Date(previousWorkout.session.timestamp)
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (previousWorkoutIndex > 0) {
                                        IconButton(
                                            onClick = { previousWorkoutIndex -= 1 },
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Show newer workout",
                                            )
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Icon(
                                    imageVector = if (lastTimeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (lastTimeExpanded) "Collapse History" else "Expand History",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimatedVisibility(visible = lastTimeExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                key(previousWorkout.session.id) {
                                    groupedPreviousEntries.forEach { (name, entries) ->
                                        CollapsibleExerciseCard(exerciseName = name, entries = entries)
                                    }
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
                        label = { Text(if (isCardio) "Activity" else "Exercise Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(exerciseNameFocusRequester)
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                        trailingIcon = if (exerciseName.isNotBlank()) {
                            {
                                IconButton(onClick = { 
                                    exerciseName = ""
                                    expandedAutocomplete = false
                                    exerciseNameFocusRequester.requestFocus()
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

                if (isCardio) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (min)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(durationFocusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = if (duration.isNotBlank()) {
                            {
                                IconButton(onClick = { 
                                    duration = "" 
                                    durationFocusRequester.requestFocus()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Duration")
                                }
                            }
                        } else null
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (lbs)") },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(weightFocusRequester),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = if (weight.isNotBlank()) {
                                {
                                    IconButton(onClick = { 
                                        weight = "" 
                                        weightFocusRequester.requestFocus()
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Weight")
                                    }
                                }
                            } else null
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps") },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(repsFocusRequester),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = if (reps.isNotBlank()) {
                                {
                                    IconButton(onClick = { 
                                        reps = "" 
                                        repsFocusRequester.requestFocus()
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Reps")
                                    }
                                }
                            } else null
                        )
                    }
                }

                Button(
                    onClick = {
                        expandedAutocomplete = false
                        val repsInt = reps.toIntOrNull() ?: 0
                        val durationInt = duration.toIntOrNull() ?: 0
                        
                        if (exerciseName.isNotBlank() && (
                            (isCardio && durationInt > 0) || 
                            (!isCardio && weight.isNotBlank() && repsInt > 0)
                        )) {
                            val nextSetNumber = currentEntries.filter { it.exerciseName.equals(exerciseName, ignoreCase = true) }.size + 1
                            activeSessionId?.let {
                                viewModel.addEntry(
                                    sessionId = it,
                                    exerciseName = exerciseName,
                                    weight = weight.toDoubleOrNull() ?: 0.0,
                                    reps = repsInt,
                                    setNumber = nextSetNumber,
                                    durationSeconds = if (isCardio) durationInt * 60 else null
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isCardio) "Add Activity" else "Add Set")
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
                                .clip(RoundedCornerShape(16.dp))
                                .combinedClickable(
                                    onLongClick = { isEditMode = true },
                                    onClick = { if (isEditMode) isEditMode = false }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                                if (isCardio) {
                                    Text(text = entry.exerciseName)
                                } else {
                                    Text(text = "${entry.exerciseName} (Set ${entry.setNumber})")
                                }
                            }
                            val durationSeconds = entry.durationSeconds
                            if (isCardio && durationSeconds != null) {
                                Text(text = "${durationSeconds / 60} min", fontWeight = FontWeight.Bold)
                            } else {
                                Text(text = "${entry.weight} lbs x ${entry.reps}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (isBackActionProcessing) return@Button
                    isBackActionProcessing = true
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
    var expanded by remember { mutableStateOf(true) }
    val shape = RoundedCornerShape(12.dp)
    Card(
        shape = shape,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { expanded = !expanded }
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (expanded) {
                entries.forEach { entry ->
                    val durationSeconds = entry.durationSeconds
                    val text = if (durationSeconds != null) {
                        "${durationSeconds / 60}m"
                    } else {
                        val weightStr = if (entry.weight % 1.0 == 0.0) entry.weight.toInt().toString() else entry.weight.toString()
                        "${weightStr}lb×${entry.reps}"
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        )
                    }
                }
            } else {
                Text(
                    text = "${entries.size} sets",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
