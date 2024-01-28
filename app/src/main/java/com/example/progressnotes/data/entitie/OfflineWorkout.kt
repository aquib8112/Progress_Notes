package com.example.progressnotes.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineWorkout(
    @PrimaryKey(autoGenerate = true)
    val workoutId : Long = 0,
    val workoutName : String,
    val day : String,
    val date : String,
    val isDeleted: Boolean = false,
    val isUpdated: Boolean = false
)
