package com.example.gymapp.data.repository

import com.example.gymapp.data.dao.WorkoutDao
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val recentSessions: Flow<List<WorkoutSession>> = workoutDao.getRecentSessions()
    val allSessions: Flow<List<WorkoutSession>> = workoutDao.getAllSessions()

    suspend fun getAllData(): Pair<List<WorkoutSession>, List<ExerciseEntry>> {
        val sessions = workoutDao.getAllSessionsList()
        val entries = workoutDao.getAllEntriesList()
        return Pair(sessions, entries)
    }

    suspend fun restoreData(sessions: List<WorkoutSession>, entries: List<ExerciseEntry>) {
        workoutDao.deleteAllEntries()
        workoutDao.deleteAllSessions()
        workoutDao.insertSessions(sessions)
        workoutDao.insertExerciseEntries(entries)
    }

    suspend fun createSession(type: String): Long {
        val session = WorkoutSession(type = type)
        return workoutDao.insertSession(session)
    }

    suspend fun addExerciseEntry(entry: ExerciseEntry) {
        workoutDao.insertExerciseEntry(entry)
    }

    suspend fun deleteExerciseEntry(entry: ExerciseEntry) {
        workoutDao.deleteExerciseEntry(entry)
    }

    suspend fun deleteSession(session: WorkoutSession) {
        workoutDao.deleteSession(session)
    }

    suspend fun getSessionById(id: Long): WorkoutSession? {
        return workoutDao.getSessionById(id)
    }

    fun getPreviousSessionByType(type: String, excludeSessionId: Long): Flow<WorkoutSession?> {
        return workoutDao.getPreviousSessionByType(type, excludeSessionId)
    }

    fun getPreviousWorkoutEntriesByType(type: String, excludeSessionId: Long): Flow<List<ExerciseEntry>> {
        return workoutDao.getPreviousWorkoutEntriesByType(type, excludeSessionId)
    }

    fun getEntriesForSession(sessionId: Long): Flow<List<ExerciseEntry>> {
        return workoutDao.getEntriesForSession(sessionId)
    }

    suspend fun getLastWorkoutTimestampByType(workoutType: String): Long? {
        return workoutDao.getLastWorkoutTimestampByType(workoutType)
    }

    fun getExerciseNamesByType(workoutType: String): Flow<List<String>> {
        return workoutDao.getExerciseNamesByType(workoutType)
    }
}
