package com.example.gymapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.model.ExerciseEntry
import com.example.gymapp.data.model.GymAppData
import com.example.gymapp.data.model.ThemeMode
import com.example.gymapp.data.model.WeightUnit
import com.example.gymapp.data.model.WorkoutSession
import com.example.gymapp.data.model.WorkoutWithEntries
import com.example.gymapp.data.repository.UserPreferencesRepository
import com.example.gymapp.data.repository.WorkoutRepository
import com.example.gymapp.ui.WorkoutCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val recentSessions: Flow<List<WorkoutSession>> = repository.recentSessions
    val allSessions: Flow<List<WorkoutSession>> = repository.allSessions

    val lastWorkoutDatesByType: StateFlow<Map<String, Long>> = allSessions
        .map(::latestWorkoutDatesByType)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val workoutDaysByMonth: StateFlow<Map<String, Set<Int>>> = allSessions
        .map(::workoutDaysByMonth)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val workoutStatsByMonth: StateFlow<Map<String, Map<Int, List<String>>>> = allSessions
        .map(::workoutStatsByMonth)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun seedData() {
        viewModelScope.launch {
            val sessions = mutableListOf<WorkoutSession>()
            val entries = mutableListOf<ExerciseEntry>()
            val cal = Calendar.getInstance()
            val categories = WorkoutCategory.categories.map { it.name }
            
            // Start from today and go back 60 days
            for (i in 0 until 60) {
                // 70% chance of workout
                if (Math.random() < 0.7) {
                    // 30% chance of multiple workouts
                    val workoutCount = if (Math.random() < 0.3) 2 else 1
                    
                    val usedCategories = mutableSetOf<String>()
                    repeat(workoutCount) {
                        var type: String
                        do {
                            type = categories.random()
                        } while (usedCategories.contains(type))
                        usedCategories.add(type)
                        
                        val sessionId = (sessions.size + 1).toLong()
                        sessions.add(WorkoutSession(id = sessionId, type = type, timestamp = cal.timeInMillis))
                        
                        // Add 2-3 exercises
                        repeat((2..3).random()) { exIdx ->
                            val exName = when(type) {
                                "Legs" -> listOf("Squat", "Leg Press", "Lunge").random()
                                "Chest" -> listOf("Bench Press", "Incline Fly", "Dips").random()
                                "Back" -> listOf("Pull Ups", "Deadlift", "Rows").random()
                                else -> "Exercise $exIdx"
                            }
                            // Add 3 sets per exercise
                            repeat(3) { setNum ->
                                entries.add(ExerciseEntry(
                                    sessionId = sessionId,
                                    exerciseName = exName,
                                    weight = (50..200).random().toDouble(),
                                    reps = (8..12).random(),
                                    setNumber = setNum + 1
                                ))
                            }
                        }
                    }
                }
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            repository.restoreData(sessions, entries)
        }
    }

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
        .map(::orderedWorkoutCategories)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val hasSeenUpdate03: StateFlow<Boolean> = userPreferencesRepository.hasSeenUpdate03
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.Dark,
        )

    val weightUnit: StateFlow<WeightUnit> = userPreferencesRepository.weightUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightUnit.Pounds,
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

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            userPreferencesRepository.setWeightUnit(weightUnit)
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

    suspend fun getSessionEntriesSnapshot(sessionId: Long): List<ExerciseEntry> {
        return repository.getEntriesForSessionSnapshot(sessionId)
    }

    fun restoreSessionEntries(sessionId: Long, entries: List<ExerciseEntry>) {
        viewModelScope.launch {
            repository.restoreSessionEntries(sessionId, entries)
        }
    }

    fun getPreviousWorkouts(
        type: String,
        currentSessionId: Long,
    ): Flow<List<WorkoutWithEntries>> =
        repository.getPreviousWorkoutsBefore(type, currentSessionId)

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

internal fun latestWorkoutDatesByType(
    sessions: List<WorkoutSession>,
): Map<String, Long> = sessions
    .groupBy { it.type }
    .mapValues { (_, groupedSessions) -> groupedSessions.maxOf { it.timestamp } }

internal fun workoutDaysByMonth(
    sessions: List<WorkoutSession>,
): Map<String, Set<Int>> {
    val daysByMonth = mutableMapOf<String, MutableSet<Int>>()
    val calendar = Calendar.getInstance()
    sessions.forEach { session ->
        calendar.timeInMillis = session.timestamp
        val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
        daysByMonth.getOrPut(key) { mutableSetOf() }
            .add(calendar.get(Calendar.DAY_OF_MONTH))
    }
    return daysByMonth
}

internal fun workoutStatsByMonth(
    sessions: List<WorkoutSession>,
): Map<String, Map<Int, List<String>>> {
    val statsByMonth = mutableMapOf<String, MutableMap<Int, MutableList<String>>>()
    val calendar = Calendar.getInstance()
    sessions.forEach { session ->
        calendar.timeInMillis = session.timestamp
        val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
        val workoutTypes = statsByMonth
            .getOrPut(key) { mutableMapOf() }
            .getOrPut(calendar.get(Calendar.DAY_OF_MONTH)) { mutableListOf() }
        if (session.type !in workoutTypes) {
            workoutTypes.add(session.type)
        }
    }
    return statsByMonth
}

internal fun orderedWorkoutCategories(
    savedOrder: List<String>,
): List<WorkoutCategory> {
    if (savedOrder.isEmpty()) return WorkoutCategory.categories

    val ordered = savedOrder.mapNotNull(WorkoutCategory::getByName)
    val missing = WorkoutCategory.categories.filter { category ->
        ordered.none { it.name.equals(category.name, ignoreCase = true) }
    }
    return (ordered + missing).distinctBy { it.name.lowercase() }
}
