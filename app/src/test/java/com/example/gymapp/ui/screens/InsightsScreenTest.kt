package com.example.gymapp.ui.screens

import com.example.gymapp.data.model.WorkoutSession
import java.util.Calendar
import org.junit.Assert.assertEquals
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

    private fun date(year: Int, month: Int, day: Int): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }
}
