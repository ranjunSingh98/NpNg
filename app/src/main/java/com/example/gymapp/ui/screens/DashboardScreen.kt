package com.example.gymapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gymapp.ui.WorkoutCategory
import com.example.gymapp.ui.components.WorkoutCategoryCard
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: WorkoutViewModel,
    onWorkoutTypeSelected: (String) -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentSessions by viewModel.recentSessions.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val lastWorkoutDates = remember { mutableStateMapOf<String, Long?>() }

    // Fetch last workout dates for each type
    LaunchedEffect(WorkoutCategory.categories) {
        WorkoutCategory.categories.forEach { category ->
            lastWorkoutDates[category.name] = viewModel.getLastWorkoutDate(category.name)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NpNg",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkoutCategory.categories.forEach { category ->
                WorkoutCategoryCard(
                    category = category,
                    lastWorkoutDate = lastWorkoutDates[category.name],
                    dateFormat = dateFormat,
                    onClick = { onWorkoutTypeSelected(category.name) }
                )
            }
            // Plus button for custom workouts
            WorkoutCategoryCard(
                category = WorkoutCategory("Add Custom", Color.Gray, imageVector = Icons.Default.Add),
                lastWorkoutDate = null,
                dateFormat = dateFormat,
                onClick = { /* TODO: Implement custom workout creation */ }
            )
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recentSessions) { session ->
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = session.type, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = dateFormat.format(Date(session.timestamp)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
