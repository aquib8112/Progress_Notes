package com.example.progressnotes.data.entitie

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithExercise(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "workoutId"
    )
    val exercise: List<Exercise>
)
