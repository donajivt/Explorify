package com.example.explorifyapp.presentation.publicaciones

import androidx.lifecycle.ViewModelProvider
import com.example.explorifyapp.domain.repository.PublicationsRepository
import androidx.lifecycle.ViewModel

class MyPublicationsViewModelFactory(
    private val repo: PublicationsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPublicationsViewModel::class.java)) {
            return MyPublicationsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
