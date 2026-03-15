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
    suspend fun insertExerciseEntry(entry: ExerciseEntry)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC LIMIT 3")
    fun getRecentSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSessionByType(type: String): WorkoutSession?

    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getEntriesForSession(sessionId: Long): Flow<List<ExerciseEntry>>
    
    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    suspend fun getEntriesForSessionSync(sessionId: Long): List<ExerciseEntry>

    @Transaction
    @Query("""
        SELECT * FROM exercise_entries 
        WHERE sessionId = (
            SELECT id FROM workout_sessions 
            WHERE type = :type AND id != :excludeSessionId
            ORDER BY timestamp DESC LIMIT 1
        )
    """)
    fun getPreviousWorkoutEntriesByType(type: String, excludeSessionId: Long): Flow<List<ExerciseEntry>>
}
