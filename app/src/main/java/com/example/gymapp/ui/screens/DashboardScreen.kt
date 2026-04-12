package com.example.gymapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.gymapp.ui.WorkoutCategory
import com.example.gymapp.ui.components.WorkoutCategoryCard
import com.example.gymapp.ui.components.WorkoutSessionCard
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WorkoutViewModel,
    onWorkoutTypeSelected: (String) -> Unit,
    onViewHistory: () -> Unit,
    onViewInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recentSessions by viewModel.recentSessions.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val showAddCustomDialog = remember { mutableStateOf(false) }
    var customWorkoutName by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val orderedCategories by viewModel.orderedCategories.collectAsState()
    val lastWorkoutDates = remember { mutableStateMapOf<String, Long?>() }

    val hasSeenUpdate03 by viewModel.hasSeenUpdate03.collectAsState()

    // Activity Result Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportDataToJson()
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Export Successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val json = inputStream.bufferedReader().use { reader -> reader.readText() }
                    viewModel.importDataFromJson(
                        jsonString = json,
                        onSuccess = {
                            Toast.makeText(context, "Import Successful", Toast.LENGTH_SHORT).show()
                        },
                        onError = { e ->
                            Toast.makeText(context, "Import Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Use a stable SnapshotStateList that doesn't get replaced every time orderedCategories emits.
    val categories = remember {
        mutableStateListOf<WorkoutCategory>().apply { addAll(orderedCategories) }
    }

    LaunchedEffect(orderedCategories) {
        val currentNames = categories.map { it.name }
        val newNames = orderedCategories.map { it.name }
        if (currentNames != newNames) {
            categories.clear()
            categories.addAll(orderedCategories)
        }
    }

    LaunchedEffect(orderedCategories) {
        orderedCategories.forEach { category ->
            lastWorkoutDates[category.name] = viewModel.getLastWorkoutDate(category.name)
        }
    }

    if (!hasSeenUpdate03) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdate03() },
            title = { Text("What's New in v0.3") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• Data Portability: You can now export and import your workout history from the top-right menu.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Safety: Prevent data loss during app updates by keeping a manual backup of your progress.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUpdate03() }) {
                    Text("Got it!")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.seedDatabase()
                    viewModel.dismissUpdate03()
                }) {
                    Text("Load Demo Data")
                }
            }
        )
    }

    if (showAddCustomDialog.value) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog.value = false },
            title = { Text("Custom Workout") },
            text = {
                Column {
                    Text("Enter a name for your custom workout:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customWorkoutName,
                        onValueChange = { customWorkoutName = it },
                        label = { Text("Workout Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customWorkoutName.isNotBlank()) {
                            onWorkoutTypeSelected(customWorkoutName)
                            showAddCustomDialog.value = false
                            customWorkoutName = ""
                        }
                    }
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NpNg",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    IconButton(onClick = onViewInsights) {
                        Icon(Icons.Default.Insights, contentDescription = "Insights")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Data") },
                                onClick = {
                                    showMenu = false
                                    val fileNameDate = SimpleDateFormat("M_d_yy", Locale.getDefault()).format(Date())
                                    exportLauncher.launch("gymapp_backup_$fileNameDate.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Data") },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomDialog.value = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Workout")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            val listState = rememberLazyListState()
            val haptic = LocalHapticFeedback.current

            val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
                categories.add(to.index, categories.removeAt(from.index))
                viewModel.saveCategoryOrder(categories.toList())
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.name }) { category ->
                    ReorderableItem(reorderableLazyListState, key = category.name) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                        val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")

                        WorkoutCategoryCard(
                            category = category,
                            lastWorkoutDate = lastWorkoutDates[category.name],
                            dateFormat = dateFormat,
                            onClick = { onWorkoutTypeSelected(category.name) },
                            modifier = Modifier
                                .longPressDraggableHandle(
                                    onDragStarted = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragStopped = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = elevation.toPx()
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent History",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onViewHistory) {
                    Text("See All")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentSessions.forEach { session ->
                    WorkoutSessionCard(session, viewModel)
                }
            }
        }
    }
}
