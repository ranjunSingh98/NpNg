package com.example.gymapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.ui.WorkoutCategory
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutSessionCard(
    session: WorkoutSession,
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }
    val category = remember(session.type) { WorkoutCategory.getByName(session.type) }
    
    val entries by viewModel.getEntriesForSession(session.id).collectAsState(initial = emptyList())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (category?.iconRes != null) {
                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = null,
                            tint = if (category.tintIcon) category.accentColor else Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (category?.imageVector != null) {
                        Icon(
                            imageVector = category.imageVector,
                            contentDescription = null,
                            tint = if (category.tintIcon) category.accentColor else Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = session.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = category?.accentColor ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = dateFormat.format(Date(session.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                if (entries.isEmpty()) {
                    Text("No activities recorded.", style = MaterialTheme.typography.bodySmall)
                } else {
                    entries.forEach { entry ->
                        val text = if (entry.durationSeconds != null) {
                            "${entry.exerciseName}: ${entry.durationSeconds / 60} min"
                        } else {
                            "${entry.exerciseName}: ${entry.weight}lbs x ${entry.reps}"
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
