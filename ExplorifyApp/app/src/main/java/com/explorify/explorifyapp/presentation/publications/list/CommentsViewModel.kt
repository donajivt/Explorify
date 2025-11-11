package com.explorify.explorifyapp.presentation.publications.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorify.explorifyapp.data.remote.model.Comentario
import com.explorify.explorifyapp.data.remote.publications.RetrofitComentariosInstance
import com.explorify.explorifyapp.domain.repository.ComentarioRepositoryImpl

data class CommentsUiState(
    val comentarios: List<Comentario> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class CommentsViewModel : ViewModel() {

    private val repo = ComentarioRepositoryImpl(RetrofitComentariosInstance.api)
    var uiState by mutableStateOf(CommentsUiState())

    fun load(publicacionId: String, token: String) {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true)
            try {
                val comentarios = repo.getComentarios(publicacionId, token)
                uiState = uiState.copy(comentarios = comentarios, loading = false, error = null)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, loading = false)
            }
        }
    }

    suspend fun addComentario(publicacionId: String, texto: String, token: String): Comentario? {
        return try {
            val newComment = repo.createComentario(publicacionId, texto, token)
            uiState = uiState.copy(comentarios = uiState.comentarios + newComment)
            newComment
        } catch (e: Exception) {
            uiState = uiState.copy(error = e.message)
            null
        }
    }

    fun deleteComentario(id: String, token: String) {
        viewModelScope.launch {
            try {
                repo.deleteComentario(id, token)
                // ✅ Cambiado a it.id (coincide con tu modelo)
                uiState = uiState.copy(
                    comentarios = uiState.comentarios.filterNot { it.id == id }
                )
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }
}
