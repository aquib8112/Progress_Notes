package com.example.progressnotes.data.fireStore

import android.content.Context
import android.util.Log
import com.example.justdoit.util.isInternetAvailable
import com.example.progressnotes.data.WorkoutAndExerciseDatabase
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface FireStoreDAO {
    private suspend fun getUserDocumentId(): String?{
        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser!!.uid
        val userQuery = db.collection("users")
            .whereEqualTo("uid",user)
            .get()
            .await()
        return if (!userQuery.isEmpty) {
            userQuery.documents[0].id
        } else {
            Log.d("getWorkoutDocumentId", "Workout not found.") // Workout not found
            null
        }
    }

    private suspend fun getWorkoutAndUserDocumentId(workoutId: Long): Pair<String?, String?> {
        val db = Firebase.firestore
        try {
            val userDocumentId = getUserDocumentId()
            val workoutQuery = db.collection("users") // Find the workout using workoutId
                .document(userDocumentId!!)
                .collection("workouts")
                .whereEqualTo("workoutId", workoutId)
                .get()
                .await()

                val workoutDocumentId = if (!workoutQuery.isEmpty) workoutQuery.documents[0].id else null
                return Pair(userDocumentId, workoutDocumentId)

        } catch (e: Exception) {
            Log.e("getWorkoutDocumentId", "Error getting workout document ID: ${e.message}")
        }
        return Pair(null, null)
    }

    private suspend fun getUserWorkoutAndExerciseDocumentIds(workoutId: Long, exerciseId: Long): Array<String?> {
        val db = Firebase.firestore
        try {
            // Find the workout document
            val (userDocumentId, workoutDocumentId) = getWorkoutAndUserDocumentId(workoutId)
            if (workoutDocumentId != null && userDocumentId != null) {
                // Find the exercise document within the workout
                val exerciseQuery = db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .collection("exercises")
                    .whereEqualTo("exerciseId",exerciseId)
                    .get()
                    .await()

                val exerciseDocumentId = if (!exerciseQuery.isEmpty) exerciseQuery.documents[0].id else null
                return arrayOf(userDocumentId, workoutDocumentId, exerciseDocumentId)
            }
        } catch (e: Exception) {
            Log.e("getWorkoutAndExerciseDocumentIds", "Error getting workout and exercise document IDs: ${e.message}")
        }
        return emptyArray()
    }

    suspend fun userExist(uid:String): String?{
        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser!!.uid
        val userQuery = db.collection("users")
            .whereEqualTo("uid",user)
            .get()
            .await()
        return if (!userQuery.isEmpty) {
            userQuery.documents[0].id
        } else {
            Log.d("getWorkoutDocumentId", "Workout not found.") // Workout not found
            null
        }
    }

    suspend fun downloadUserData(context: Context) {
        val db = Firebase.firestore
        val dao = WorkoutAndExerciseDatabase.getDatabase(context)
        val userDocumentId = getUserDocumentId()

        Log.i("downloadUserData", "Starting download process...")

        try {
            if (userDocumentId != null) {
                Log.i("downloadUserData", "User document ID found: $userDocumentId")

                // Download workouts and exercises
                db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .get()
                    .await()
                    .documents
                    .forEach { workoutDocument ->
                        Log.d("downloadUserData", "Processing workout document: ${workoutDocument.id}")
                        val workout = workoutDocument.toObject(Workout::class.java)
                        workout?.let {
                            Log.d("downloadUserData", "Inserting workout: $it")
                            dao.workoutDAO().insertWorkout(it)

                            // Get exercises directly using workoutDocument
                            val exerciseQuery = workoutDocument.reference.collection("exercises").get().await()
                            Log.d("downloadUserData", "Retrieved ${exerciseQuery.documents.size} exercise for workout ${it.workoutId}")
                            exerciseQuery.documents
                                .mapNotNull { exerciseDocument -> exerciseDocument.toObject(Exercise::class.java) }
                                .forEach { exercise ->
                                    Log.d("downloadUserData", "Inserting exercise: $exercise")
                                    dao.exerciseDAO().insertExercise(exercise)
                                }
                        }
                    }

                Log.i("downloadUserData", "Data download completed successfully.")

                // Notify UI on the main thread (uncomment when ready for UI updates)
                // withContext(Dispatchers.Main) {
                //     // Update UI elements to reflect the downloaded data
                // }
            } else {
                Log.w("downloadUserData", "User data not found.")
            }
        } catch (e: Exception) {
            Log.e("downloadUserData", "Error downloading user data: ${e.message}")
        }
    }

    suspend fun addWorkoutInFirestore(workout: Workout) {
        val db = Firebase.firestore
        val userDocumentId = getUserDocumentId()// Find the workout document
        if (userDocumentId != null) {
            db.collection("users")
                .document(userDocumentId)
                .collection("workouts")
                .add(workout)
        }
    }

    suspend fun deleteWorkoutAndExerciseInFirestore(workoutId: Long) {
        val db = Firebase.firestore
        try {
            val documentIds = getWorkoutAndUserDocumentId(workoutId)
            val userDocumentId = documentIds.first
            val workoutDocumentId = documentIds.second

            if (workoutDocumentId != null && userDocumentId != null) {
                // Delete all exercise of the workout first
                db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .collection("exercises")
                    .get()
                    .await()
                    .documents
                    .forEach { exerciseDocument ->
                        exerciseDocument.reference.delete().await()
                    }

                // Then delete the workout itself
                db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .delete()
                    .await()

                Log.i("deleteWorkoutAndExerciseInFirestore", "Workout and exercise deleted successfully.")
            }
        } catch (e: Exception) {
            Log.e("deleteWorkoutAndExerciseInFirestore", "Error deleting Workout and exercise: ${e.message}")
        }
    }

    suspend fun updateWorkoutInFirestore(workout: Workout) {
        val db = Firebase.firestore
        try {
            val documentIds = getWorkoutAndUserDocumentId(workout.workoutId) // Get the workout document ID
            val userDocumentId = documentIds.first
            val workoutDocumentId = documentIds.second
            if (workoutDocumentId != null && userDocumentId != null) {
                db.collection("users") // Update the workout
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .set(workout) // Set the updated workout
                    .await()
            }
        } catch (e: Exception) {
            Log.e("updateWorkoutInFirestore", "Error updating workout: ${e.message}")
        }
    }

    suspend fun syncWorkout(context: Context) {
        val dao = WorkoutAndExerciseDatabase.getDatabase(context).offlineWorkoutDAO()
        if (isInternetAvailable(context)) {
            try {
                val offlineWorkouts = dao.getAllOfflineWorkout()
                for (offlineWorkout in offlineWorkouts) {
                    if (offlineWorkout.isDeleted) {
                        deleteWorkoutAndExerciseInFirestore(offlineWorkout.workoutId)
                    } else if (offlineWorkout.isUpdated) {
                        val newWorkout = Workout(
                            offlineWorkout.workoutId,
                            offlineWorkout.workoutName,
                            offlineWorkout.date,
                            offlineWorkout.day
                        )
                        updateWorkoutInFirestore(newWorkout)
                    } else {
                        val newWorkout = Workout(
                            offlineWorkout.workoutId,
                            offlineWorkout.workoutName,
                            offlineWorkout.date,
                            offlineWorkout.day
                        )
                        addWorkoutInFirestore(newWorkout)
                    }
                    dao.deleteOfflineWorkout(offlineWorkout)
                }
            } catch (e: Exception) {
                Log.e("syncWorkouts", "Error syncing workouts: ${e.message}")
            }
        }
    }

    suspend fun addExerciseInFirestore(exercise: Exercise, context: Context) {
        val db = Firebase.firestore
        try {
            val documentIds = getWorkoutAndUserDocumentId(exercise.workoutId) // Get the workout document ID
            val userDocumentId = documentIds.first
            val workoutDocumentId = documentIds.second// Find the workout document
            if (workoutDocumentId != null && userDocumentId != null) {
                // Add the exercise as a sub-collection of the specified workout
                db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .collection("exercises")
                    .add(exercise)
                    .await()
            }
        } catch (e: Exception) {
            Log.e("addExerciseInFirestore", "Error adding exercise: ${e.message}")
        }
    }

    suspend fun deleteExerciseInFirestore(workoutId: Long, exerciseId: Long) {
        val db = Firebase.firestore
        try {
            val documentIds = getUserWorkoutAndExerciseDocumentIds(workoutId,exerciseId)// Find the workout and exercise documents
            val userDocumentId = documentIds[0]
            val workoutDocumentId = documentIds[1]
            val exerciseDocumentId = documentIds[2]
            if (workoutDocumentId != null && exerciseDocumentId != null && userDocumentId != null) {
                // Delete the exercise document
                db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .collection("exercises")
                    .document(exerciseDocumentId)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            Log.e("deleteExerciseInFirestore", "Error deleting exercise: ${e.message}")
        }
    }

    suspend fun updateExerciseInFirestore(exercise: Exercise) {
        val db = Firebase.firestore
        try {
            val documentIds = getUserWorkoutAndExerciseDocumentIds(exercise.workoutId,exercise.exerciseId)// Find the workout and exercise documents
            val userDocumentId = documentIds[0]
            val workoutDocumentId = documentIds[1]
            val exerciseDocumentId = documentIds[2]
            if (workoutDocumentId != null && exerciseDocumentId != null && userDocumentId != null) { // Update the exercise document
                db.collection("users")
                    .document(userDocumentId)
                    .collection("workouts")
                    .document(workoutDocumentId)
                    .collection("exercises")
                    .document(exerciseDocumentId)
                    .update("exerciseName", exercise.exerciseName,
                        "sets", exercise.sets,
                        "reps",exercise.reps,
                        "weights",exercise.weights)
                    .await()
            }
        } catch (e: Exception) {
            Log.e("updateExerciseInFirestore", "Error updating exercise: ${e.message}")
        }
    }

    suspend fun syncExercise(context: Context) {
        val dao = WorkoutAndExerciseDatabase.getDatabase(context).offlineExerciseDAO()
        if (isInternetAvailable(context)) {
            val offlineExercises = dao.getAllOfflineExercise()
            for (offlineExercise in offlineExercises) {
                if (offlineExercise.isDeleted) {
                    deleteExerciseInFirestore(offlineExercise.workoutId, offlineExercise.exerciseId)
                } else if (offlineExercise.isUpdated) {
                    val newExercise = Exercise(
                        offlineExercise.exerciseId,
                        offlineExercise.exerciseName,
                        offlineExercise.sets,
                        offlineExercise.reps,
                        offlineExercise.weights,
                        offlineExercise.workoutId
                    )
                    updateExerciseInFirestore(newExercise)
                } else {
                    val newExercise = Exercise(
                        offlineExercise.exerciseId,
                        offlineExercise.exerciseName,
                        offlineExercise.sets,
                        offlineExercise.reps,
                        offlineExercise.weights,
                        offlineExercise.workoutId
                    )
                    addExerciseInFirestore(newExercise, context)
                }
                dao.deleteOfflineExercise(offlineExercise)
            }
        }
    }
}