package com.example.gymapp.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithEntries(
    @Embedded val session: WorkoutSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val entries: List<ExerciseEntry>,
)
