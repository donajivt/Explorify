package com.example.explorifyapp.presentation.buscar

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.explorifyapp.data.remote.dto.Publication
import com.example.explorifyapp.domain.repository.PublicationsMapRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.example.explorifyapp.data.remote.dto.PublicationMap


class BuscarViewModel (  private val repo: PublicationsMapRepository
) : ViewModel() {

    var publications by mutableStateOf<List<PublicationMap>>(emptyList())
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
                val response = repo.getMapsPublications(token)
                if (response.isSuccess) {
                    publications = response.result
                    Log.d("Publicaciones","Si llegan los resultados en Busqueda");
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