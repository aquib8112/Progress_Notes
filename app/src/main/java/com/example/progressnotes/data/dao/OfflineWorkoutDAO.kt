package com.example.progressnotes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.progressnotes.data.entitie.OfflineWorkout

@Dao
interface OfflineWorkoutDAO {
    @Insert
    suspend fun insertOfflineWorkout (workout: OfflineWorkout)

    @Delete
    suspend fun deleteOfflineWorkout (workout: OfflineWorkout)

    @Upsert
    suspend fun updateOfflineWorkout (workout: OfflineWorkout)

    @Query("SELECT * FROM offlineworkout")
    suspend fun getAllOfflineWorkout(): List<OfflineWorkout>

    @Query("SELECT * FROM offlineworkout WHERE workoutId = :workoutId")
    suspend fun getOfflineWorkoutById(workoutId: Long): OfflineWorkout?
}