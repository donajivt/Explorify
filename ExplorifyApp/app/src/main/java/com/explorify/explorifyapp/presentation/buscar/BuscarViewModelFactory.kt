package com.explorify.explorifyapp.presentation.buscar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.explorify.explorifyapp.domain.repository.PublicationsMapRepository

class BuscarViewModelFactory (
    private val repo: PublicationsMapRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BuscarViewModel::class.java)) {
            return BuscarViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}