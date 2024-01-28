package com.example.progressnotes.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progressnotes.data.entitie.Workout
import com.example.progressnotes.data.fireStore.FireStoreCO
import com.example.progressnotes.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeVeiwModel(private val repository: HomeRepository):ViewModel() {

    fun syncWorkout(context: Context){
        viewModelScope.launch {
            FireStoreCO().syncWorkout(context)
        }
    }

    fun insertWorkout(workout: Workout,context: Context){
        viewModelScope.launch (Dispatchers.IO){
            repository.insertWorkout(workout,context)
        }
    }

    fun getWorkouts() : LiveData<List<Workout>> {
        return repository.getWorkouts()
    }

}