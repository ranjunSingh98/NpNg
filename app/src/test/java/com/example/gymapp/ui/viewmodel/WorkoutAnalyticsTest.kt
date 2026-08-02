package com.example.gymapp.ui.viewmodel

import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.ui.WorkoutCategory
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutAnalyticsTest {
    @Test
    fun latestWorkoutDatesSelectsNewestSessionPerType() {
        val sessions = listOf(
            WorkoutSession(id = 1, type = "Legs", timestamp = 100),
            WorkoutSession(id = 2, type = "Chest", timestamp = 300),
            WorkoutSession(id = 3, type = "Legs", timestamp = 200),
        )

        assertEquals(
            mapOf("Legs" to 200L, "Chest" to 300L),
            latestWorkoutDatesByType(sessions),
        )
    }

    @Test
    fun workoutDaysGroupByCalendarMonthAndDeduplicateSameDay() {
        val sessions = listOf(
            session(1, "Legs", 2026, Calendar.JULY, 31),
            session(2, "Chest", 2026, Calendar.JULY, 31),
            session(3, "Back", 2026, Calendar.AUGUST, 1),
        )

        assertEquals(
            mapOf(
                "2026-6" to setOf(31),
                "2026-7" to setOf(1),
            ),
            workoutDaysByMonth(sessions),
        )
    }

    @Test
    fun workoutStatsKeepDifferentTypesButDeduplicateRepeatedTypeOnSameDay() {
        val sessions = listOf(
            session(1, "Legs", 2026, Calendar.AUGUST, 2),
            session(2, "Legs", 2026, Calendar.AUGUST, 2),
            session(3, "Cardio", 2026, Calendar.AUGUST, 2),
        )

        assertEquals(
            listOf("Legs", "Cardio"),
            workoutStatsByMonth(sessions).getValue("2026-7").getValue(2),
        )
    }

    @Test
    fun categoryOrderingIgnoresUnknownsAndAppendsMissingCategoriesOnce() {
        val ordered = orderedWorkoutCategories(
            listOf("cardio", "Unknown", "Legs", "CARDIO")
        )

        assertEquals(listOf("Cardio", "Legs"), ordered.take(2).map { it.name })
        assertEquals(WorkoutCategory.categories.size, ordered.size)
        assertEquals(ordered.size, ordered.map { it.name.lowercase() }.distinct().size)
        assertTrue(ordered.map { it.name }.containsAll(WorkoutCategory.categories.map { it.name }))
    }

    private fun session(
        id: Long,
        type: String,
        year: Int,
        month: Int,
        day: Int,
    ): WorkoutSession = WorkoutSession(
        id = id,
        type = type,
        timestamp = Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }.timeInMillis,
    )
}
