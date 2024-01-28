package com.example.progressnotes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.progressnotes.data.entitie.OfflineExercise

@Dao
interface OfflineExerciseDAO {
    @Insert
    suspend fun insertOfflineExercise(exercise: OfflineExercise)

    @Upsert
    suspend fun updateOfflineExercise(exercise: OfflineExercise)

    @Delete
    suspend fun deleteOfflineExercise(exercise: OfflineExercise)

    @Query("SELECT * FROM offlineexercise")
    suspend fun getAllOfflineExercise(): List<OfflineExercise>

    @Query("SELECT * FROM offlineexercise WHERE exerciseId = :exerciseId")
    suspend fun getOfflineExerciseById(exerciseId: Long): OfflineExercise?
}