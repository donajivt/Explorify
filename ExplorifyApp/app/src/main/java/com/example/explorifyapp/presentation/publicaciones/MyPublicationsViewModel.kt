package com.example.explorifyapp.presentation.publicaciones

import androidx.lifecycle.ViewModel
import com.example.explorifyapp.data.remote.dto.Publication
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.explorifyapp.domain.repository.PublicationsRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log

class MyPublicationsViewModel(
    private val repo: PublicationsRepository
) : ViewModel() {

    var publications by mutableStateOf<List<Publication>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadPublications(userId: String,token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repo.getUserPublications(userId, token)
                if (response.isSuccess) {
                    publications = response.result
                    Log.d("Publicaciones","Si llegan los resultados");
                } else {
                    Log.d("Publicaciones","Error en los resultados de las publicaciones");
                    errorMessage = response.message
                }
            } catch(e: Exception) {
                errorMessage = e.localizedMessage
            }
            isLoading = false
        }
    }
}
