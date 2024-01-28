package com.example.progressnotes.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.justdoit.util.isInternetAvailable
import com.example.progressnotes.R
import com.example.progressnotes.activity.ExerciseActivity
import com.example.progressnotes.data.WorkoutAndExerciseDatabase
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.OfflineExercise
import com.example.progressnotes.data.fireStore.FireStoreCO
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseAdapter : ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(DiffUtil()) {

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.exerciseId == newItem.exerciseId
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        return ExerciseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.exercise_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val exerciseName = view.findViewById<TextView>(R.id.exercise_Name)
        private val sets = view.findViewById<TextView>(R.id.sets_tv)
        private val reps = view.findViewById<TextView>(R.id.reps_tv)
        private val weights = view.findViewById<TextView>(R.id.weights_tv)
        fun bind(exercise: Exercise) {
            exerciseName.text = exercise.exerciseName
            sets.text = exercise.sets
            reps.text = exercise.reps
            weights.text = exercise.weights

            itemView.setOnClickListener {
                val bottomSheetDialog = BottomSheetDialog(itemView.context)
                val bottomSheetView = LayoutInflater.from(itemView.context).inflate(R.layout.edit_delete_card, null)
                bottomSheetDialog.setContentView(bottomSheetView)

                bottomSheetView.findViewById<TextView>(R.id.edit_text_tv).setOnClickListener {
                    val activity = itemView.context as ExerciseActivity
                    bottomSheetDialog.dismiss()
                    activity.exerciseRV.visibility = View.GONE
                    activity.updateExerciseCard.visibility = View.VISIBLE
                    activity.updateExerciseNameET.setText(exerciseName.text.toString())
                    activity.updateSetsET.setText(sets.text.toString())
                    activity.updateRepsET.setText(reps.text.toString())
                    activity.updateWeightsET.setText(weights.text.toString())

                    activity.updateExerciseButton.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedExercise = Exercise(exercise.exerciseId,
                                activity.updateExerciseNameET.text.toString(),
                                activity.updateSetsET.text.toString(),
                                activity.updateRepsET.text.toString(),
                                activity.updateWeightsET.text.toString(),
                                exercise.workoutId)

                            updateExerciseInRoom(updatedExercise,itemView.context)
                            if(isInternetAvailable(itemView.context)){
                                FireStoreCO().updateExerciseInFirestore(updatedExercise)
                            }
                            withContext(Dispatchers.Main) {
                                activity.updateExerciseCard.visibility = View.INVISIBLE
                                activity.updateExerciseNameET.setText("")
                                activity.updateSetsET.setText("")
                                activity.updateRepsET.setText("")
                                activity.updateWeightsET.setText("")
                                activity.exerciseRV.visibility = View.VISIBLE
                            }
                        }
                    }
                    activity.cancelUpdateExerciseButton.setOnClickListener {
                        activity.updateExerciseCard.visibility = View.INVISIBLE
                        activity.updateExerciseNameET.setText("")
                        activity.updateSetsET.setText("")
                        activity.updateRepsET.setText("")
                        activity.updateWeightsET.setText("")
                        activity.exerciseRV.visibility = View.VISIBLE
                    }
                }

                bottomSheetView.findViewById<TextView>(R.id.delete_text_tv).setOnClickListener {
                    val activity = itemView.context as ExerciseActivity
                    activity.exerciseDeleteConfirmationCard.visibility = View.VISIBLE

                    activity.exerciseDeleteConfirmButton.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            deleteExerciseInRoom(exercise,itemView.context)
                            if (isInternetAvailable(itemView.context)){
                                FireStoreCO().deleteExerciseInFirestore(exercise.workoutId,exercise.exerciseId)
                            }
                        }
                        activity.exerciseDeleteConfirmationCard.visibility = View.GONE
                    }

                    activity.cancelExerciseDeleteButton.setOnClickListener {
                        activity.exerciseDeleteConfirmationCard.visibility = View.GONE
                    }
                    bottomSheetDialog.dismiss()
                }

                bottomSheetDialog.show()
            }

        }

    }
}

private suspend fun updateExerciseInRoom(exercise: Exercise, context: Context) {
    val database = WorkoutAndExerciseDatabase.getDatabase(context)
    if (!isInternetAvailable(context)){
        updateExerciseInOfflineExercise(exercise, context)
    }
    database.exerciseDAO().updateExercise(exercise)
}

private suspend fun deleteExerciseInRoom(exercise: Exercise, context: Context){
    val database = WorkoutAndExerciseDatabase.getDatabase(context)
    if(!isInternetAvailable(context)){
        deleteExerciseInOfflineExercise(exercise,context)
    }
    database.exerciseDAO().deleteExercise(exercise)
}

private suspend fun updateExerciseInOfflineExercise(exercise: Exercise, context: Context) {
    val database = WorkoutAndExerciseDatabase.getDatabase(context).offlineExerciseDAO()
    if (database.getOfflineExerciseById(exercise.exerciseId) != null) {
        val offlineExercise = OfflineExercise(
            exercise.exerciseId,
            exercise.exerciseName,
            exercise.sets,
            exercise.reps,
            exercise.weights,
            exercise.workoutId,
            isDeleted = false,
            isUpdated = false)
        database.updateOfflineExercise(offlineExercise)
    } else {
        val offlineExercise = OfflineExercise(
            exercise.exerciseId,
            exercise.exerciseName,
            exercise.sets,
            exercise.reps,
            exercise.weights,
            exercise.workoutId,
            isDeleted = false,
            isUpdated = true)
        database.insertOfflineExercise(offlineExercise)
    }
}

private suspend fun deleteExerciseInOfflineExercise(exercise: Exercise, context: Context) {
    val database = WorkoutAndExerciseDatabase.getDatabase(context).offlineExerciseDAO()
    if (database.getOfflineExerciseById(exercise.exerciseId) != null) {
        val offlineExercise = OfflineExercise(
            exercise.exerciseId,
            exercise.exerciseName,
            exercise.sets,
            exercise.reps,
            exercise.weights,
            exercise.workoutId,
            isDeleted = false,
            isUpdated = false)
        database.deleteOfflineExercise(offlineExercise)
    } else {
        val offlineExercise = OfflineExercise(
            exercise.exerciseId,
            exercise.exerciseName,
            exercise.sets,
            exercise.reps,
            exercise.weights,
            exercise.workoutId,
            isDeleted = true,
            isUpdated = false)
        database.insertOfflineExercise(offlineExercise)
    }
}