package com.example.progressnotes.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val exerciseId : Long = 0,
    val exerciseName : String,
    val sets : String,
    val reps : String,
    val weights : String,
    val workoutId : Long
){
    // Add a no-argument constructor
    constructor() : this(0, "", "", "","",0)
}