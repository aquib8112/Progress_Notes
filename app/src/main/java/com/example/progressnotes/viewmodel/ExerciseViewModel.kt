package com.example.progressnotes.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.data.entitie.WorkoutWithExercise
import com.example.progressnotes.data.fireStore.FireStoreCO
import com.example.progressnotes.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel(private val exerciseRepository: ExerciseRepository): ViewModel() {

    fun syncExercise(context: Context){
        viewModelScope.launch {
            FireStoreCO().syncExercise(context)
        }
    }

    fun insertExercise(exercise: Exercise,context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.insertExercise(exercise,context)
        }
    }

    fun getExercise(workoutId: Long): LiveData<WorkoutWithExercise> {
        return exerciseRepository.getAllExercise(workoutId)
    }

}