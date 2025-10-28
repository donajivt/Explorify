package com.example.explorifyapp.presentation.publicaciones

import androidx.lifecycle.ViewModel
import com.example.explorifyapp.data.remote.model.Publication
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.explorifyapp.domain.repository.PublicationRepository
class MyPublicationsViewModel(
    private val repo: PublicationRepository
) : ViewModel() {

    var publications by mutableStateOf<List<Publication>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadPublications(userId: String, token: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val all = repo.getUserPublications(userId, token)
                publications = all.filter { it.userId == userId }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun deletePublication(publicationId: String, token: String) {
        viewModelScope.launch {
            try {
                repo.delete(publicationId, token)
                publications = publications.filterNot { it.id == publicationId } // quita de la lista
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al eliminar publicación"
            }
        }
    }

    fun updatePublication(
        id: String,
        imageUrl: String,
        title: String,
        description: String,
        location: String,
        userId: String,
        token: String
    ) {
        viewModelScope.launch {
            try {
                val updated = repo.update(id, imageUrl, title, description, location, userId, token)
                publications = publications.map { if (it.id == id) updated else it }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }


}
