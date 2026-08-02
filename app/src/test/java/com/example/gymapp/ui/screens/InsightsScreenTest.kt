package com.example.gymapp.ui.screens

import com.example.gymapp.data.model.WorkoutSession
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsScreenTest {
    @Test
    fun totalWorkoutsCountsDistinctWorkoutDaysAcrossAllMonths() {
        val today = date(2026, Calendar.JULY, 12)
        val sessions = listOf(
            WorkoutSession(id = 1, type = "Chest", timestamp = date(2026, Calendar.JULY, 2).timeInMillis),
            WorkoutSession(id = 2, type = "Back", timestamp = date(2026, Calendar.JULY, 2).timeInMillis),
            WorkoutSession(id = 3, type = "Legs", timestamp = date(2026, Calendar.JUNE, 28).timeInMillis)
        )

        val summary = calculateWorkoutSummary(
            sessions = sessions,
            year = 2026,
            month = Calendar.JULY,
            today = today
        )

        assertEquals(2, summary.totalWorkouts)
        assertEquals(1, summary.workoutDays)
    }

    @Test
    fun emptyHistoryProducesZeroedSummary() {
        val summary = calculateWorkoutSummary(
            sessions = emptyList(),
            year = 2026,
            month = Calendar.AUGUST,
            today = date(2026, Calendar.AUGUST, 2),
        )

        assertEquals(0, summary.totalWorkouts)
        assertEquals(0, summary.workoutDays)
        assertEquals(0.0, summary.averagePerWeek, 0.0)
    }

    @Test
    fun currentMonthAverageUsesOnlyElapsedDays() {
        val sessions = listOf(
            WorkoutSession(id = 1, type = "Legs", timestamp = date(2026, Calendar.AUGUST, 1).timeInMillis),
            WorkoutSession(id = 2, type = "Back", timestamp = date(2026, Calendar.AUGUST, 8).timeInMillis),
        )

        val summary = calculateWorkoutSummary(
            sessions = sessions,
            year = 2026,
            month = Calendar.AUGUST,
            today = date(2026, Calendar.AUGUST, 14),
        )

        assertEquals(1.0, summary.averagePerWeek, 0.0001)
    }

    @Test
    fun historicalMonthAverageUsesFullLeapMonth() {
        val sessions = listOf(1, 8, 15, 22).mapIndexed { index, day ->
            WorkoutSession(
                id = index.toLong() + 1,
                type = "Legs",
                timestamp = date(2024, Calendar.FEBRUARY, day).timeInMillis,
            )
        }

        val summary = calculateWorkoutSummary(
            sessions = sessions,
            year = 2024,
            month = Calendar.FEBRUARY,
            today = date(2026, Calendar.AUGUST, 2),
        )

        assertEquals(28.0 / 29.0, summary.averagePerWeek, 0.0001)
        assertTrue(summary.averagePerWeek < 1.0)
    }

    private fun date(year: Int, month: Int, day: Int): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }
}
