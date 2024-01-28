package com.example.progressnotes.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineExercise(
    @PrimaryKey(autoGenerate = true)
    val exerciseId : Long = 0,
    val exerciseName : String,
    val sets : String,
    val reps : String,
    val weights : String,
    val workoutId : Long,
    val isDeleted: Boolean = false,
    val isUpdated: Boolean = false
)