package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSessions(sessions: List<WorkoutSession>)

    @Insert
    suspend fun insertExerciseEntries(entries: List<ExerciseEntry>)

    @Insert
    suspend fun insertExerciseEntry(entry: ExerciseEntry)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM exercise_entries")
    suspend fun deleteAllEntries()

    @Delete
    suspend fun deleteExerciseEntry(entry: ExerciseEntry)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC LIMIT 3")
    fun getRecentSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions")
    suspend fun getAllSessionsList(): List<WorkoutSession>

    @Query("SELECT * FROM exercise_entries")
    suspend fun getAllEntriesList(): List<ExerciseEntry>

    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getEntriesForSession(sessionId: Long): Flow<List<ExerciseEntry>>

    @Query("SELECT timestamp FROM workout_sessions WHERE type = :workoutType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastWorkoutTimestampByType(workoutType: String): Long?

    @Query("""
        SELECT * FROM workout_sessions 
        WHERE type = :type AND id != :excludeSessionId
        ORDER BY timestamp DESC LIMIT 1
    """)
    fun getPreviousSessionByType(type: String, excludeSessionId: Long): Flow<WorkoutSession?>

    @Transaction
    @Query("""
        SELECT * FROM exercise_entries 
        WHERE sessionId = (
            SELECT id FROM workout_sessions 
            WHERE type = :type AND id != :excludeSessionId
            ORDER BY timestamp DESC LIMIT 1
        )
        ORDER BY id ASC
    """)
    fun getPreviousWorkoutEntriesByType(type: String, excludeSessionId: Long): Flow<List<ExerciseEntry>>

    @Query("""
        SELECT DISTINCT TRIM(LOWER(exerciseName))
        FROM exercise_entries 
        INNER JOIN workout_sessions ON exercise_entries.sessionId = workout_sessions.id
        WHERE workout_sessions.type = :workoutType
        ORDER BY exerciseName ASC
    """)
    fun getExerciseNamesByType(workoutType: String): Flow<List<String>>
}
