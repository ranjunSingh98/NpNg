package com.example.gymapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    val recentSessions: Flow<List<WorkoutSession>> = repository.recentSessions
    val allSessions: Flow<List<WorkoutSession>> = repository.allSessions

    private val _currentSessionId = MutableStateFlow<Long?>(null)

    fun startSession(type: String) {
        viewModelScope.launch {
            val id = repository.createSession(type)
            _currentSessionId.value = id
        }
    }

    fun addEntry(exerciseName: String, weight: Double, reps: Int, setNumber: Int) {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            val entry = ExerciseEntry(
                sessionId = sessionId,
                exerciseName = exerciseName,
                weight = weight,
                reps = reps,
                setNumber = setNumber
            )
            repository.addExerciseEntry(entry)
        }
    }

    fun discardCurrentSession() {
        val sessionId = _currentSessionId.value ?: return
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPreviousWorkoutEntries(type: String): Flow<List<ExerciseEntry>> {
        return _currentSessionId.flatMapLatest { currentId ->
            if (currentId == null) flowOf(emptyList())
            else repository.getPreviousWorkoutEntriesByType(type, currentId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentSessionEntries(): Flow<List<ExerciseEntry>> {
        return _currentSessionId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getEntriesForSession(id)
        }
    }

    fun getEntriesForSession(sessionId: Long): Flow<List<ExerciseEntry>> {
        return repository.getEntriesForSession(sessionId)
    }

    suspend fun getLastWorkoutDate(workoutType: String): Long? {
        return repository.getLastWorkoutTimestampByType(workoutType)
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
