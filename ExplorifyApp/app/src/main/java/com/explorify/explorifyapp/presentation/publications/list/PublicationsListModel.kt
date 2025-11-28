package com.explorify.explorifyapp.presentation.publications.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorify.explorifyapp.data.remote.model.Publication
import com.explorify.explorifyapp.domain.usecase.publications.PublicationUseCases
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.explorify.explorifyapp.data.remote.publications.RetrofitComentariosInstance
import com.explorify.explorifyapp.domain.repository.ReportRepository
import com.explorify.explorifyapp.domain.repository.UnauthorizedException
import com.explorify.explorifyapp.domain.usecase.publications.CreateReportUseCase
import com.explorify.explorifyapp.data.remote.dto.publications.CreateReportRequest

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
    private val deleteUc: PublicationUseCases.DeletePublicationUseCase,
    private val reportUc: CreateReportUseCase
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
    /*
    fun getPublication(id: String, token: String, onResult: (Publication?) -> Unit) =
        viewModelScope.launch {
            runCatching { getById(id, token) }
                .onSuccess { onResult(it) }
                .onFailure { onResult(null) }
        }
    */
    fun delete(id: String, token: String) = viewModelScope.launch {
        uiState = uiState.copy(deletingId = id)
        runCatching { deleteUc(id, token) }
            .onSuccess { load(token) }
            .onFailure { e -> uiState = uiState.copy(error = e.message) }
        uiState = uiState.copy(deletingId = null)
    }

    suspend fun getCommentsCount(publicacionId: String, token: String): Int {
        return try {
            val response = RetrofitComentariosInstance.api
                .getCount(publicacionId, "Bearer $token")

            if (response.isSuccessful) {
                response.body()?.count ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun reportPublication(
        publicationId: String,
        userId: String,
        reason: String,
        description: String,
        token: String,
        onResult: (Boolean, String) -> Unit
    ) = viewModelScope.launch {

        val request = CreateReportRequest(
            publicationId = publicationId,
            reportedByUserId = userId,
            reason = reason,
            description = description
        )

        runCatching {
            reportUc(request, token)
        }
            .onSuccess { msg ->
                onResult(true, msg)
            }
            .onFailure { e ->
                onResult(false, e.message ?: "Error")
            }
    }

}
