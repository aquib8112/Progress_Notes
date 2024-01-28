package com.example.progressnotes.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.progressnotes.repository.ExerciseRepository
import com.example.progressnotes.viewmodel.ExerciseViewModel

class ExerciseViewModelFactory(private val repository: ExerciseRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExerciseViewModel(repository) as T
    }
}