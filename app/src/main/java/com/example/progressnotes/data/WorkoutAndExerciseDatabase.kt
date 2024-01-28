package com.example.progressnotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.progressnotes.data.dao.ExerciseDAO
import com.example.progressnotes.data.dao.OfflineExerciseDAO
import com.example.progressnotes.data.dao.OfflineWorkoutDAO
import com.example.progressnotes.data.dao.WorkoutDAO
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.OfflineExercise
import com.example.progressnotes.data.entitie.OfflineWorkout
import com.example.progressnotes.data.entitie.Workout

@Database(entities = [Workout::class,Exercise::class,OfflineWorkout::class,OfflineExercise::class], version = 3)
abstract class WorkoutAndExerciseDatabase : RoomDatabase() {
    abstract fun workoutDAO() : WorkoutDAO
    abstract fun exerciseDAO() : ExerciseDAO
    abstract fun offlineWorkoutDAO() : OfflineWorkoutDAO
    abstract fun offlineExerciseDAO() : OfflineExerciseDAO

    companion object{
        @Volatile
        private var INSTANCE: WorkoutAndExerciseDatabase? = null

        fun getDatabase(context: Context):WorkoutAndExerciseDatabase{
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        WorkoutAndExerciseDatabase::class.java,
                        "Workout"
                    ).addMigrations(MIGRATION_2_3)
                        .build()
                }
            }
            return INSTANCE!!
        }

        val MIGRATION_2_3 = object : Migration(2, 3) { // Place migration here
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add SQL statements to modify the schema as needed
                db.execSQL("ALTER TABLE Exercise ADD COLUMN newColumn TEXT")
            }
        }

    }
}