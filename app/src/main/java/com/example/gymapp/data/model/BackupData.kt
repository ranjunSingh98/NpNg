package com.example.gymapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GymAppData(
    val version: Int,
    val sessions: List<WorkoutSession>,
    val entries: List<ExerciseEntry>
)
