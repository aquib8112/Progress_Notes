package com.example.progressnotes.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.justdoit.util.isInternetAvailable
import com.example.progressnotes.R
import com.example.progressnotes.activity.ExerciseActivity
import com.example.progressnotes.activity.HomeActivity
import com.example.progressnotes.data.WorkoutAndExerciseDatabase
import com.example.progressnotes.data.entitie.OfflineWorkout
import com.example.progressnotes.data.entitie.Workout
import com.example.progressnotes.data.fireStore.FireStoreCO
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class WorkoutAdapter: ListAdapter<Workout, WorkoutAdapter.WorkoutViewHolder>(DiffUtil()) {

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.workoutId == newItem.workoutId
        }

        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        return WorkoutViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.workout_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val workoutName = view.findViewById<TextView>(R.id.exercise_day_name_tv)
        private val day = view.findViewById<TextView>(R.id.day_name_tv)
        private val date = view.findViewById<TextView>(R.id.date_tv)

        fun bind(workout: Workout) {
            val activity = itemView.context as HomeActivity
            workoutName.text = workout.workoutName
            day.text = workout.day
            date.text = workout.date

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ExerciseActivity::class.java)
                intent.putExtra("id", workout.workoutId)
                intent.putExtra("exerciseName", workout.workoutName)
                itemView.context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                val bottomSheetDialog = BottomSheetDialog(itemView.context)
                val bottomSheetView =
                    LayoutInflater.from(itemView.context).inflate(R.layout.edit_delete_card, null)
                bottomSheetDialog.setContentView(bottomSheetView)

                bottomSheetView.findViewById<TextView>(R.id.edit_text_tv).setOnClickListener {
                    var dayOfWeekName = day.text.toString()
                    bottomSheetDialog.dismiss()
                    activity.workoutRV.visibility = View.GONE
                    activity.updateWorkoutCard.visibility = View.VISIBLE
                    activity.updatedWorkoutName.setText(workoutName.text.toString())
                    activity.updatedDate.setText(date.text.toString())
                    activity.updatedDate.setOnClickListener {
                        activity.updateWorkoutCard.visibility =
                            View.GONE;activity.calender.visibility = View.VISIBLE
                    }

                    activity.updateCalenderButton.setOnClickListener {
                        activity.updateWorkoutCard.visibility = View.GONE
                        activity.calender.visibility = View.VISIBLE
                    }

                    activity.calender.setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        val formattedDate =
                            DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(selectedDate)
                        dayOfWeekName = selectedDate.dayOfWeek.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        )
                        activity.updatedDate.setText(formattedDate)
                        activity.calender.visibility = View.GONE
                        activity.updateWorkoutCard.visibility = View.VISIBLE
                    }

                    activity.updateWorkOutButton.setOnClickListener {
                        Log.d("updateButton", "working")
                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedWorkout = Workout(
                                workout.workoutId,
                                activity.updatedWorkoutName.text.toString(),
                                dayOfWeekName,
                                activity.updatedDate.text.toString()
                            )
                            if (isInternetAvailable(itemView.context)) {
                                FireStoreCO().updateWorkoutInFirestore(updatedWorkout)
                            }

                            updateWorkoutInRoom(updatedWorkout, itemView.context)

                            withContext(Dispatchers.Main) {
                                activity.updateWorkoutCard.visibility = View.INVISIBLE
                                activity.updatedWorkoutName.setText("")
                                activity.updatedDate.setText("")
                                activity.workoutRV.visibility = View.VISIBLE
                            }
                        }
                    }
                    activity.cancelUpdateWorkOutButton.setOnClickListener {
                        activity.updateWorkoutCard.visibility = View.INVISIBLE
                        activity.workoutRV.visibility = View.VISIBLE
                        activity.updatedWorkoutName.setText("")
                        activity.updatedDate.setText("")

                    }
                }

                bottomSheetView.findViewById<TextView>(R.id.delete_text_tv).setOnClickListener {
                    activity.workoutDeleteConfirmationCard.visibility = View.VISIBLE

                    activity.workoutDeleteConfirmButton.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            deleteWorkoutInRoom(workout, itemView.context)
                            if (isInternetAvailable(itemView.context)) {
                                FireStoreCO().deleteWorkoutAndExerciseInFirestore(workout.workoutId)
                            }
                        }
                        activity.workoutDeleteConfirmationCard.visibility = View.GONE
                    }

                    activity.cancelWorkoutDeleteButton.setOnClickListener {
                        activity.workoutDeleteConfirmationCard.visibility = View.GONE
                    }
                    bottomSheetDialog.dismiss()
                }
                bottomSheetDialog.show()
                true
            }

        }
    }
}

private suspend fun updateWorkoutInRoom(workout: Workout,context: Context){
    val database = WorkoutAndExerciseDatabase.getDatabase(context)
    if (!isInternetAvailable(context)){
        updateWorkoutInOfflineWorkout(workout,context)
    }
    database.workoutDAO().updateWorkout(workout)
}

private suspend fun deleteWorkoutInRoom(workout: Workout, context: Context){
    val database = WorkoutAndExerciseDatabase.getDatabase(context).workoutDAO()
    database.deleteExerciseForWorkout(workout.workoutId)
    if (!isInternetAvailable(context)){
        updateWorkoutInOfflineWorkout(workout,context)
    }
    deleteWorkoutInOfflineWorkout(workout,context)
    database.deleteWorkout(workout)
}

private suspend fun updateWorkoutInOfflineWorkout(workout: Workout, context: Context) {
    val database = WorkoutAndExerciseDatabase.getDatabase(context).offlineWorkoutDAO()
    if (database.getOfflineWorkoutById(workout.workoutId) != null){
        val offlineWorkout = OfflineWorkout(workout.workoutId,workout.workoutName,workout.day,workout.date,isDeleted = false,isUpdated = false)
        database.updateOfflineWorkout(offlineWorkout)
    }else{
        val offlineWorkout = OfflineWorkout(workout.workoutId,workout.workoutName,workout.day,workout.date,isDeleted = false,isUpdated = true)
        database.insertOfflineWorkout(offlineWorkout)
    }
}

private suspend fun deleteWorkoutInOfflineWorkout(workout: Workout, context: Context) {
    val database = WorkoutAndExerciseDatabase.getDatabase(context).offlineWorkoutDAO()
    if (database.getOfflineWorkoutById(workout.workoutId) != null){
        //if the Workout exist in offlineWorkout DB than that means Workout was created when the device was offline
        // and the device was never online after it was created then just delete it
        val offlineWorkout = OfflineWorkout(workout.workoutId,workout.workoutName,workout.day,workout.date,isDeleted = false,isUpdated = false)
        database.deleteOfflineWorkout(offlineWorkout)
    }else{
        //if the Workout does not exist in offlineWorkout DB than that means Workout was created when the device was online
        //and now when the device is offline it is being deleted so we will just insert it delete value of true so it will be deleted by sync function
        val offlineWorkout = OfflineWorkout(workout.workoutId,workout.workoutName,workout.day,workout.date,isDeleted = true,isUpdated = false)
        database.insertOfflineWorkout(offlineWorkout)
    }
}