package com.example.gymapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    val recentSessions: Flow<List<WorkoutSession>> = repository.recentSessions
    val allSessions: Flow<List<WorkoutSession>> = repository.allSessions

    private val _currentSessionId = MutableStateFlow<Long?>(null)

    suspend fun startSession(type: String): Long {
        val id = repository.createSession(type)
        _currentSessionId.value = id
        return id
    }

    fun addEntry(sessionId: Long, exerciseName: String, weight: Double, reps: Int, setNumber: Int) {
        viewModelScope.launch {
            val entry = ExerciseEntry(
                sessionId = sessionId,
                exerciseName = exerciseName.trim(),
                weight = weight,
                reps = reps,
                setNumber = setNumber
            )
            repository.addExerciseEntry(entry)
        }
    }

    fun discardCurrentSession(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            if (session != null) {
                repository.deleteSession(session)
            }
            _currentSessionId.value = null
        }
    }

    fun finishCurrentSession() {
        _currentSessionId.value = null
    }

    fun getPreviousWorkoutEntries(type: String, excludeSessionId: Long): Flow<List<ExerciseEntry>> {
        return repository.getPreviousWorkoutEntriesByType(type, excludeSessionId)
    }

    fun getEntriesForSession(sessionId: Long): Flow<List<ExerciseEntry>> {
        return repository.getEntriesForSession(sessionId)
    }

    suspend fun getLastWorkoutDate(workoutType: String): Long? {
        return repository.getLastWorkoutTimestampByType(workoutType)
    }

    fun getExerciseNamesByType(workoutType: String): Flow<List<String>> {
        return repository.getExerciseNamesByType(workoutType).map { names ->
            names.map { name ->
                name.lowercase().split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }.distinct()
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkoutViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
