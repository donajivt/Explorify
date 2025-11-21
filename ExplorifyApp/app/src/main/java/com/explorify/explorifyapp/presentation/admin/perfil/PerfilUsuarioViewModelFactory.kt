package com.explorify.explorifyapp.presentation.admin.perfil

import com.explorify.explorifyapp.domain.repository.UserRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.explorify.explorifyapp.domain.repository.PublicationRepository

class PerfilUsuarioViewModelFactory(
    private val userRepository: UserRepository,
    private val publicationRepository: PublicationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilUsuarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilUsuarioViewModel(userRepository,publicationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
