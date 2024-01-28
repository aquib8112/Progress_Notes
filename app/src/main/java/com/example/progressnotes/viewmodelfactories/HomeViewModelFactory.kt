package com.example.progressnotes.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.progressnotes.repository.HomeRepository
import com.example.progressnotes.viewmodel.HomeVeiwModel

class HomeViewModelFactory(private val repository: HomeRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeVeiwModel(repository) as T
    }
}