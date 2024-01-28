package com.example.progressnotes.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.WorkoutWithExercise

@Dao
interface ExerciseDAO {

    @Upsert
    suspend fun insertExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Upsert
    suspend fun updateExercise(exercise: Exercise)

    @Query("SELECT * FROM Exercise ORDER BY exerciseId DESC LIMIT 1")
    suspend fun getLastExercise(): Exercise

    @Query("SELECT * FROM Exercise JOIN Workout ON Exercise.workoutId = Workout.workoutId WHERE Exercise.workoutId = :workoutId")
    fun getWorkoutWithExercises(workoutId: Long): LiveData<WorkoutWithExercise>
}