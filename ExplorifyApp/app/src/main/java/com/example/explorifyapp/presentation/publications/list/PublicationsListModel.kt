package com.example.explorifyapp.presentation.publications.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorifyapp.data.remote.model.Publication
import com.example.explorifyapp.domain.usecase.publications.PublicationUseCases
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.explorifyapp.domain.repository.UnauthorizedException

data class PublicationListState(
    val loading: Boolean = false,
    val items: List<Publication> = emptyList(),
    val error: String? = null,
    val unauthorized: Boolean = false,
    val deletingId: String? = null,
)

class PublicationsListModel(
    private val getAll: PublicationUseCases.GetPublicationsUseCase,
    private val getById: PublicationUseCases.GetPublicationByIdUseCase,
    private val deleteUc: PublicationUseCases.DeletePublicationUseCase
) : ViewModel() {

    var uiState by mutableStateOf(PublicationListState())
        private set

    // 🔹 ahora requiere token
    fun load(token: String) = viewModelScope.launch {
        uiState = uiState.copy(loading = true, error = null, unauthorized = false)
        runCatching { getAll(token) }
            .onSuccess { pubs ->
                val sorted = pubs.sortedByDescending { it.createdAt }
                uiState = uiState.copy(loading = false, items = sorted)
            }
            .onFailure { e ->
                uiState = when (e) {
                    is UnauthorizedException -> uiState.copy(loading = false, unauthorized = true)
                    else -> uiState.copy(loading = false, error = e.message)
                }
            }
    }

    fun refresh(token: String) = load(token)

    fun getPublication(id: String, token: String, onResult: (Publication?) -> Unit) =
        viewModelScope.launch {
            runCatching { getById(id, token) }
                .onSuccess { onResult(it) }
                .onFailure { onResult(null) }
        }

    fun delete(id: String, token: String) = viewModelScope.launch {
        uiState = uiState.copy(deletingId = id)
        runCatching { deleteUc(id, token) }
            .onSuccess { load(token) }
            .onFailure { e -> uiState = uiState.copy(error = e.message) }
        uiState = uiState.copy(deletingId = null)
    }
}
