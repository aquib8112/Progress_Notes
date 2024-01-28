package com.example.progressnotes.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.justdoit.util.isInternetAvailable
import com.example.progressnotes.data.dao.OfflineWorkoutDAO
import com.example.progressnotes.data.dao.WorkoutDAO
import com.example.progressnotes.data.entitie.OfflineWorkout
import com.example.progressnotes.data.entitie.Workout
import com.example.progressnotes.data.fireStore.FireStoreCO

class HomeRepository(
    private val workoutDAO: WorkoutDAO,
    private val offlineWorkoutDAO: OfflineWorkoutDAO,
) {

    suspend fun insertWorkout(workout: Workout, context: Context) {
        workoutDAO.insertWorkout(workout)
        val lastWorkout = workoutDAO.getLastWorkout()
        val newWorkout =
            Workout(lastWorkout.workoutId, workout.workoutName, workout.day, workout.date)
        if (!isInternetAvailable(context)) {
            val offlineWorkout = OfflineWorkout(
                lastWorkout.workoutId,
                workout.workoutName,
                workout.day,
                workout.date,
                isDeleted = false,
                isUpdated = false
            )
            offlineWorkoutDAO.insertOfflineWorkout(offlineWorkout)
        } else {
            FireStoreCO().addWorkoutInFirestore(newWorkout)
        }
    }

    fun getWorkouts(): LiveData<List<Workout>> {
        return workoutDAO.getAllWorkouts()
    }

}