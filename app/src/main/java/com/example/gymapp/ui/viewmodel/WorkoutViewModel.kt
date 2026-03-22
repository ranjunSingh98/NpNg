package com.example.gymapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.data.repository.UserPreferencesRepository
import com.example.gymapp.data.repository.WorkoutRepository
import com.example.gymapp.ui.WorkoutCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val recentSessions: Flow<List<WorkoutSession>> = repository.recentSessions
    val allSessions: Flow<List<WorkoutSession>> = repository.allSessions

    val orderedCategories: StateFlow<List<WorkoutCategory>> = userPreferencesRepository.categoryOrder
        .map { order ->
            if (order.isEmpty()) {
                WorkoutCategory.categories
            } else {
                order.mapNotNull { name -> WorkoutCategory.getByName(name) }
                    .let { ordered ->
                        // Add any categories that aren't in the saved order yet
                        val missing = WorkoutCategory.categories.filter { cat ->
                            !ordered.any { it.name == cat.name }
                        }
                        ordered + missing
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutCategory.categories
        )

    fun saveCategoryOrder(categories: List<WorkoutCategory>) {
        viewModelScope.launch {
            userPreferencesRepository.saveCategoryOrder(categories.map { it.name })
        }
    }

    suspend fun startSession(type: String): Long {
        return repository.createSession(type)
    }

    fun addEntry(
        sessionId: Long,
        exerciseName: String,
        weight: Double,
        reps: Int,
        setNumber: Int,
        durationSeconds: Int? = null
    ) {
        viewModelScope.launch {
            val entry = ExerciseEntry(
                sessionId = sessionId,
                exerciseName = exerciseName.trim().replace("\\s+".toRegex(), " "),
                weight = weight,
                reps = reps,
                setNumber = setNumber,
                durationSeconds = durationSeconds
            )
            repository.addExerciseEntry(entry)
        }
    }

    fun deleteEntry(entry: ExerciseEntry) {
        viewModelScope.launch {
            repository.deleteExerciseEntry(entry)
        }
    }

    fun discardCurrentSession(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            if (session != null) {
                repository.deleteSession(session)
            }
        }
    }

    fun finishCurrentSession() {
        // This is a placeholder if we need to do anything when finishing a session
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
                name.lowercase().trim().split("\\s+".toRegex()).joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }.distinct()
        }
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkoutViewModel(repository, userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
