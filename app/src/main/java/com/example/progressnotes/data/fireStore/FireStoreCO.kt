package com.example.progressnotes.data.fireStore

import android.content.Context
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.Workout


class FireStoreCO : FireStoreDAO {
    override suspend fun addWorkoutInFirestore(workout: Workout) {
        super.addWorkoutInFirestore(workout)
    }

    override suspend fun deleteWorkoutAndExerciseInFirestore(workoutId: Long) {
        super.deleteWorkoutAndExerciseInFirestore(workoutId)
    }

    override suspend fun updateWorkoutInFirestore(workout: Workout) {
        super.updateWorkoutInFirestore(workout)
    }

    override suspend fun syncWorkout(context: Context) {
        super.syncWorkout(context)
    }

    override suspend fun addExerciseInFirestore(exercise: Exercise, context: Context) {
        super.addExerciseInFirestore(exercise, context)
    }

    override suspend fun deleteExerciseInFirestore(workoutId: Long, exerciseId: Long) {
        super.deleteExerciseInFirestore(workoutId, exerciseId)
    }

    override suspend fun updateExerciseInFirestore(exercise: Exercise) {
        super.updateExerciseInFirestore(exercise)
    }

    override suspend fun syncExercise(context: Context) {
        super.syncExercise(context)
    }

    override suspend fun downloadUserData(context: Context) {
        super.downloadUserData(context)
    }

    override suspend fun userExist(uid: String): String? {
        return super.userExist(uid)
    }
}