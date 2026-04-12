package com.example.gymapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.GymAppData
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.data.repository.UserPreferencesRepository
import com.example.gymapp.data.repository.WorkoutRepository
import com.example.gymapp.ui.WorkoutCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val recentSessions: Flow<List<WorkoutSession>> = repository.recentSessions
    val allSessions: Flow<List<WorkoutSession>> = repository.allSessions

    suspend fun exportDataToJson(): String {
        val (sessions, entries) = repository.getAllData()
        val data = GymAppData(version = 3, sessions = sessions, entries = entries)
        return Json.encodeToString(data)
    }

    fun importDataFromJson(jsonString: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val data = Json.decodeFromString<GymAppData>(jsonString)
                repository.restoreData(data.sessions, data.entries)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    val orderedCategories: StateFlow<List<WorkoutCategory>> = userPreferencesRepository.categoryOrder
        .map { order ->
            if (order.isEmpty()) {
                WorkoutCategory.categories
            } else {
                order.mapNotNull { name -> WorkoutCategory.getByName(name) }
                    .let { ordered ->
                        // Add any categories that aren't in the saved order yet
                        val missing = WorkoutCategory.categories.filter { cat ->
                            !ordered.any { it.name.equals(cat.name, ignoreCase = true) }
                        }
                        (ordered + missing).distinctBy { it.name.lowercase() }
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutCategory.categories
        )

    val hasSeenUpdate03: StateFlow<Boolean> = userPreferencesRepository.hasSeenUpdate03
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun saveCategoryOrder(categories: List<WorkoutCategory>) {
        viewModelScope.launch {
            userPreferencesRepository.saveCategoryOrder(categories.map { it.name })
        }
    }

    fun dismissUpdate03() {
        viewModelScope.launch {
            userPreferencesRepository.setHasSeenUpdate03(true)
        }
    }

    fun seedDatabase() {
        viewModelScope.launch {
            // Only seed if empty
            val current = allSessions.first()
            if (current.isNotEmpty()) return@launch

            // 1. Legs Session (Yesterday)
            val legsId = repository.createSession("Legs")
            addEntry(legsId, "Squat", 225.0, 5, 1)
            addEntry(legsId, "Squat", 225.0, 5, 2)
            addEntry(legsId, "Leg Press", 400.0, 12, 1)

            // 2. Push Session (2 days ago)
            val pushId = repository.createSession("Push")
            addEntry(pushId, "Bench Press", 185.0, 8, 1)
            addEntry(pushId, "Bench Press", 185.0, 8, 2)
            addEntry(pushId, "Overhead Press", 115.0, 10, 1)

            // 3. Cardio Session (3 days ago)
            val cardioId = repository.createSession("Cardio")
            addEntry(cardioId, "Running", 0.0, 0, 1, durationSeconds = 1800)
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
