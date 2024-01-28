package com.example.progressnotes.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val workoutId : Long = 0,
    val workoutName : String,
    val day : String,
    val date : String
){
    // Add a no-argument constructor
    constructor() : this(0, "", "", "")
}
