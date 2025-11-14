package com.explorify.explorifyapp.presentation.publicaciones

import androidx.lifecycle.ViewModelProvider
import com.explorify.explorifyapp.domain.repository.PublicationRepository
import androidx.lifecycle.ViewModel

class MyPublicationsViewModelFactory(
    private val repo: PublicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPublicationsViewModel::class.java)) {
            return MyPublicationsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
