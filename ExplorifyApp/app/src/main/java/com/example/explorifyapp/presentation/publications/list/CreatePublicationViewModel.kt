package com.example.explorifyapp.presentation.publications.list

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorifyapp.data.remote.room.AppDatabase
import com.example.explorifyapp.domain.repository.PublicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class CreatePublicationUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class CreatePublicationViewModel(
    private val repo: PublicationRepository
) : ViewModel() {

    var uiState by mutableStateOf(CreatePublicationUiState())

    /**
     * Crea una publicación usando los datos ingresados.
     * Obtiene automáticamente el token guardado en Room.
     */
    fun createPublication(
        context: Context,
        imageUrl: String,
        title: String,
        description: String,
        location: String,
        userId: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, success = false, error = null)
            Log.d("PUBLICATION_VM", "Creando publicación...")

            try {
                // 🔹 Obtener token desde Room
                val token = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(context).authTokenDao().getToken()?.token
                }

                if (token.isNullOrEmpty()) {
                    uiState = uiState.copy(
                        loading = false,
                        error = "Error: No se encontró token de autenticación"
                    )
                    return@launch
                }

                // 🔹 Crear publicación en API
                repo.create(imageUrl, title, description, location, userId, token)

                uiState = uiState.copy(
                    loading = false,
                    success = true,
                    message = "Publicación creada correctamente"
                )

                onDone()
            } catch (e: Exception) {
                Log.e("PUBLICATION_VM", "Error al crear: ${e.message}")
                uiState = uiState.copy(
                    loading = false,
                    error = e.message ?: "Error del servidor"
                )
            }
        }
    }
}