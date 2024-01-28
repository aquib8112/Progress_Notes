package com.example.progressnotes.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.justdoit.util.isInternetAvailable
import com.example.progressnotes.data.dao.ExerciseDAO
import com.example.progressnotes.data.dao.OfflineExerciseDAO
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.OfflineExercise
import com.example.progressnotes.data.entitie.WorkoutWithExercise
import com.example.progressnotes.data.fireStore.FireStoreCO

class ExerciseRepository(
    private val exerciseDAO: ExerciseDAO,
    private val offlineExerciseDAO: OfflineExerciseDAO,
) {

    suspend fun insertExercise(exercise: Exercise, context: Context) {
        exerciseDAO.insertExercise(exercise)
        val lastExercise = exerciseDAO.getLastExercise()
        val newExercise = Exercise(
            lastExercise.exerciseId,
            exercise.exerciseName,
            exercise.sets,
            exercise.reps,
            exercise.weights,
            exercise.workoutId
        )
        if (isInternetAvailable(context)) {
            FireStoreCO().addExerciseInFirestore(newExercise, context)
        } else {
            val offlineExercise = OfflineExercise(
                exercise.exerciseId,
                exercise.exerciseName,
                exercise.sets,
                exercise.reps,
                exercise.weights,
                exercise.workoutId,
                isDeleted = false,
                isUpdated = false
            )
            offlineExerciseDAO.insertOfflineExercise(offlineExercise)
        }
    }

    fun getAllExercise(workoutId: Long): LiveData<WorkoutWithExercise> {
        return exerciseDAO.getWorkoutWithExercises(workoutId)
    }

}