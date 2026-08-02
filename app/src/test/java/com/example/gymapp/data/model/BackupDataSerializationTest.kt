package com.example.gymapp.data.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupDataSerializationTest {
    @Test
    fun backupRoundTripPreservesSessionsAndStrengthAndCardioEntries() {
        val data = GymAppData(
            version = 3,
            sessions = listOf(
                WorkoutSession(id = 1, type = "Legs", timestamp = 1_000),
                WorkoutSession(id = 2, type = "Cardio", timestamp = 2_000),
            ),
            entries = listOf(
                ExerciseEntry(
                    id = 1,
                    sessionId = 1,
                    exerciseName = "Back Squat",
                    weight = 185.0,
                    reps = 8,
                    setNumber = 1,
                ),
                ExerciseEntry(
                    id = 2,
                    sessionId = 2,
                    exerciseName = "Run",
                    weight = 0.0,
                    reps = 0,
                    setNumber = 1,
                    durationSeconds = 1_800,
                ),
            ),
        )

        val restored = Json.decodeFromString<GymAppData>(Json.encodeToString(data))

        assertEquals(data, restored)
    }

    @Test
    fun malformedBackupIsRejected() {
        assertThrows(Exception::class.java) {
            Json.decodeFromString<GymAppData>("{\"version\":3,\"sessions\":[]}")
        }
    }
}
