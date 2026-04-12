package com.example.gymapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "workout_sessions")
@Serializable
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)
