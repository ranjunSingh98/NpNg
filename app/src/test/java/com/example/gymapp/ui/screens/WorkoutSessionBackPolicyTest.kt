package com.example.gymapp.ui.screens

import com.example.gymapp.data.model.ExerciseEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutSessionBackPolicyTest {
    @Test
    fun unchangedResumedWorkoutExitsWithoutPrompt() {
        val entries = listOf(entry(id = 1))

        assertEquals(
            SessionBackAction.ExitWithoutPrompt,
            resolveSessionBackAction(
                isResumedSession = true,
                originalResumedEntries = entries,
                currentEntries = entries,
            ),
        )
    }

    @Test
    fun resumedWorkoutWithAddedSetRequiresConfirmation() {
        assertEquals(
            SessionBackAction.ConfirmExit,
            resolveSessionBackAction(
                isResumedSession = true,
                originalResumedEntries = listOf(entry(id = 1)),
                currentEntries = listOf(entry(id = 1), entry(id = 2)),
            ),
        )
    }

    @Test
    fun resumedWorkoutWithEverySetDeletedRequiresConfirmation() {
        assertEquals(
            SessionBackAction.ConfirmExit,
            resolveSessionBackAction(
                isResumedSession = true,
                originalResumedEntries = listOf(entry(id = 1)),
                currentEntries = emptyList(),
            ),
        )
    }

    @Test
    fun emptyNewWorkoutIsDiscardedWithoutPrompt() {
        assertEquals(
            SessionBackAction.DiscardEmptySessionAndExit,
            resolveSessionBackAction(
                isResumedSession = false,
                originalResumedEntries = null,
                currentEntries = emptyList(),
            ),
        )
    }

    @Test
    fun populatedNewWorkoutRequiresConfirmation() {
        assertEquals(
            SessionBackAction.ConfirmExit,
            resolveSessionBackAction(
                isResumedSession = false,
                originalResumedEntries = null,
                currentEntries = listOf(entry(id = 1)),
            ),
        )
    }

    @Test
    fun resumedWorkoutWaitsForOriginalSnapshotBeforeSkippingPrompt() {
        assertEquals(
            SessionBackAction.ConfirmExit,
            resolveSessionBackAction(
                isResumedSession = true,
                originalResumedEntries = null,
                currentEntries = listOf(entry(id = 1)),
            ),
        )
    }

    private fun entry(id: Long) = ExerciseEntry(
        id = id,
        sessionId = 10,
        exerciseName = "Squat",
        weight = 185.0,
        reps = 8,
        setNumber = id.toInt(),
    )
}
