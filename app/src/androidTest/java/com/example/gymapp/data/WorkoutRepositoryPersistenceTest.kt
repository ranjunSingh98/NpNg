package com.example.gymapp.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryPersistenceTest {
    private lateinit var database: WorkoutDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutDatabase::class.java,
        ).build()
        repository = WorkoutRepository(database.workoutDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun previousWorkoutsIncludeAllMatchingNonEmptySessionsInRecentOrder() = runBlocking {
        val sessions = listOf(
            WorkoutSession(id = 1, type = "Legs", timestamp = 1_000),
            WorkoutSession(id = 2, type = "Legs", timestamp = 2_000),
            WorkoutSession(id = 3, type = "Chest", timestamp = 2_500),
            WorkoutSession(id = 4, type = "Legs", timestamp = 2_750),
            WorkoutSession(id = 5, type = "Legs", timestamp = 3_000),
        )
        val entries = listOf(
            entry(id = 1, sessionId = 1, name = "Back Squat"),
            entry(id = 2, sessionId = 2, name = "Calf Raise"),
            entry(id = 3, sessionId = 3, name = "Bench Press"),
        )
        repository.restoreData(sessions, entries)

        val history = repository.getPreviousWorkoutsBefore("Legs", 5).first()

        assertEquals(listOf(2L, 1L), history.map { it.session.id })
        assertEquals(listOf("Calf Raise"), history[0].entries.map { it.exerciseName })
        assertEquals(listOf("Back Squat"), history[1].entries.map { it.exerciseName })
    }

    @Test
    fun previousWorkoutOrderingUsesIdToBreakEqualTimestamps() = runBlocking {
        val sessions = listOf(
            WorkoutSession(id = 1, type = "Legs", timestamp = 1_000),
            WorkoutSession(id = 2, type = "Legs", timestamp = 1_000),
            WorkoutSession(id = 3, type = "Legs", timestamp = 1_000),
        )
        repository.restoreData(
            sessions,
            listOf(
                entry(id = 1, sessionId = 1, name = "Squat"),
                entry(id = 2, sessionId = 2, name = "Lunge"),
            ),
        )

        val history = repository.getPreviousWorkoutsBefore("Legs", 3).first()

        assertEquals(listOf(2L, 1L), history.map { it.session.id })
    }

    @Test
    fun deletingSessionCascadesItsExerciseEntries() = runBlocking {
        val sessionId = repository.createSession("Legs")
        repository.addExerciseEntry(entry(id = 1, sessionId = sessionId, name = "Squat"))

        repository.deleteSession(requireNotNull(repository.getSessionById(sessionId)))

        assertNull(repository.getSessionById(sessionId))
        assertTrue(repository.getEntriesForSessionSnapshot(sessionId).isEmpty())
    }

    @Test
    fun restoreDataReplacesExistingHistoryAtomically() = runBlocking {
        val originalId = repository.createSession("Chest")
        repository.addExerciseEntry(entry(id = 1, sessionId = originalId, name = "Bench Press"))
        val restoredSession = WorkoutSession(id = 20, type = "Legs", timestamp = 20_000)
        val restoredEntry = entry(id = 20, sessionId = 20, name = "Leg Press")

        repository.restoreData(listOf(restoredSession), listOf(restoredEntry))

        assertNull(repository.getSessionById(originalId))
        assertEquals(listOf(restoredSession), repository.allSessions.first())
        assertEquals(listOf(restoredEntry), repository.getEntriesForSessionSnapshot(20))
    }

    private fun entry(
        id: Long,
        sessionId: Long,
        name: String,
    ) = ExerciseEntry(
        id = id,
        sessionId = sessionId,
        exerciseName = name,
        weight = 100.0,
        reps = 10,
        setNumber = 1,
    )
}
