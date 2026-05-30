package com.example.gymapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.ui.WorkoutCategory
import java.util.Calendar

@Composable
fun WorkoutHeatmap(
    workoutStats: Map<Int, List<String>>, // Day -> List of workout types
    year: Int,
    month: Int,
    highlightedWorkoutType: String? = null,
    modifier: Modifier = Modifier
) {
    val calendar = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon...

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Days of Week Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar Grid
        val totalDays = daysInMonth + (firstDayOfWeek - 1)
        val numRows = (totalDays + 6) / 7

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until numRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 1..7) {
                        val dayIndex = row * 7 + col
                        val dayOfMonth = dayIndex - (firstDayOfWeek - 1)

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (dayOfMonth in 1..daysInMonth) {
                                val workouts = workoutStats[dayOfMonth] ?: emptyList()
                                HeatmapBlock(
                                    day = dayOfMonth,
                                    workouts = workouts,
                                    highlightedWorkoutType = highlightedWorkoutType
                                )
                            } else {
                                // Empty spacer for days outside the month
                                Spacer(modifier = Modifier.aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapBlock(
    day: Int,
    workouts: List<String>,
    highlightedWorkoutType: String?
) {
    val isActive = workouts.isNotEmpty()
    val activityLevel = workouts.size.coerceAtMost(3)
    val highlightColor = highlightedWorkoutType
        ?.let(WorkoutCategory::getByName)
        ?.accentColor
        ?: MaterialTheme.colorScheme.primary
    val matchesHighlight = highlightedWorkoutType != null &&
        workouts.any { it.equals(highlightedWorkoutType, ignoreCase = true) }

    val backgroundColor = if (!isActive) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
    } else if (highlightedWorkoutType != null) {
        if (matchesHighlight) {
            highlightColor.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        }
    } else {
        when (activityLevel) {
            1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f)
            2 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
        }
    }

    val textColor = if (isActive) {
        if (matchesHighlight) {
            highlightColor
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val borderColor = when {
        matchesHighlight -> highlightColor.copy(alpha = 0.75f)
        isActive && highlightedWorkoutType != null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(width = 0.75.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )

        if (isActive) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                workouts.take(activityLevel).forEach { workoutType ->
                    val categoryColor = WorkoutCategory.getByName(workoutType)?.accentColor
                    val dotColor = when {
                        highlightedWorkoutType == null -> categoryColor ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                        matchesHighlight -> categoryColor ?: highlightColor.copy(alpha = 0.95f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    }
                    Box(
                        modifier = Modifier
                            .size(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(dotColor)
                    )
                }
            }
        }
    }
}
