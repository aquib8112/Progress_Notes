package com.example.progressnotes.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.progressnotes.data.entitie.Workout

@Dao
interface WorkoutDAO {
    @Upsert
    suspend fun insertWorkout(workout: Workout)

    @Upsert
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT * FROM workout ORDER BY workoutId DESC LIMIT 1")
    suspend fun getLastWorkout(): Workout

    @Query("SELECT * FROM workout ORDER BY workoutId DESC")
    fun getAllWorkouts(): LiveData<List<Workout>>

    @Query("DELETE FROM Exercise WHERE workoutId = :workoutId")
    suspend fun deleteExerciseForWorkout(workoutId: Long)
}